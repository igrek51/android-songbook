@file:OptIn(DelicateCoroutinesApi::class)

package igrek.songbook.cast

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.admin.ApiResponseError
import igrek.songbook.compose.AppTheme
import igrek.songbook.compose.LabelText
import igrek.songbook.compose.RichText
import igrek.songbook.compose.colorLightBackground
import igrek.songbook.compose.md_theme_light_primaryContainer
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.LocalizedError
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import kotlinx.coroutines.*

class SongCastMenuLayout(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_cast
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songCastService by LazyExtractor(songCastService)

    val state = SongCastMenuState()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        if (songCastService.isInRoom()) {
            GlobalScope.launch(Dispatchers.Main) {
                layoutController.showLayout(SongCastLobbyLayout::class, disableReturn = true)
            }
            return
        }

        if (state.myName.isBlank()) {
            randomizeName()
        }

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }
    }

    fun randomizeName() {
        state.myName = AnimalNameFeeder().generateName()
    }

    private fun getMemberName(): String {
        if (state.myName.isBlank()) {
            state.myName = AnimalNameFeeder().generateName()
        }
        return state.myName
    }

    suspend fun createRoom() {
        uiInfoService.showInfo(R.string.songcast_creating_room, indefinite = true)
        val result = songCastService.createSessionAsync(getMemberName()).await()
        result.fold(onSuccess = { response: CastSessionJoined ->
            layoutController.showLayout(SongCastLobbyLayout::class, disableReturn = true) {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(100) // WTF: Android hackaround
                    when (response.rejoined) {
                        true -> uiInfoService.showInfo(
                            R.string.songcast_room_created_rejoined,
                            response.member_name
                        )

                        false -> uiInfoService.showInfo(
                            R.string.songcast_room_created,
                            response.member_name
                        )
                    }
                }
            }
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    suspend fun joinRoom() {
        val roomCode = state.roomCode.replace(" ", "").trim()
        if (roomCode.isBlank()) {
            throw LocalizedError(R.string.songcast_room_code_empty)
        }
        if (!Regex("[0-9]{6}").matches(roomCode)) {
            throw LocalizedError(R.string.songcast_room_code_invalid)
        }

        uiInfoService.showInfo(R.string.songcast_joining_room, indefinite = true)
        val result = songCastService.joinSessionAsync(roomCode, getMemberName()).await()
        result.fold(onSuccess = { response: CastSessionJoined ->
            layoutController.showLayout(SongCastLobbyLayout::class, disableReturn = true) {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(100) // WTF: Android hackaround
                    when (response.rejoined) {
                        true -> uiInfoService.showInfo(
                            R.string.songcast_room_joined_rejoined,
                            response.member_name
                        )

                        false -> uiInfoService.showInfo(
                            R.string.songcast_room_joined,
                            response.member_name
                        )
                    }
                }
            }
        }, onFailure = { e ->
            if (e is ApiResponseError) {
                if (e.response.code() == 404) {
                    throw LocalizedError(R.string.songcast_room_not_found)
                }
            }
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    suspend fun restoreRoom() {
        uiInfoService.showInfo(R.string.songcast_restoring_room, indefinite = true)
        val result = songCastService.restoreSessionAsync().await()
        result.fold(onSuccess = { response: CastSessionJoined ->
            layoutController.showLayout(SongCastLobbyLayout::class, disableReturn = true) {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(100) // WTF: Android hackaround
                    uiInfoService.showInfo(R.string.songcast_room_joined_rejoined, response.member_name)
                }
            }
        }, onFailure = { e ->
            if (e is ApiResponseError) {
                if (e.response.code() == 404) {
                    throw LocalizedError(R.string.songcast_restoring_room_not_found)
                }
            }
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

}

class SongCastMenuState {
    var myName: String by mutableStateOf("")
    var roomCode: String by mutableStateOf("")
}

@Composable
private fun MainComponent(controller: SongCastMenuLayout) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabNames = listOf("Host", "Join", "Restore")

    Column {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            RichText(R.string.songcast_feature_hint)
        }

        Spacer(modifier = Modifier.size(16.dp))

        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = colorLightBackground,
        ) {
            tabNames.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> TabCreateRoom(controller)
            1 -> TabJoinRoom(controller)
            2 -> TabRestoreRoom(controller)
        }
    }
}

@Composable
private fun NameTextField(controller: SongCastMenuLayout) {
    OutlinedTextField(
        value = controller.state.myName,
        onValueChange = { controller.state.myName = it },
        label = { Text(stringResource(R.string.songcast_hint_my_name)) },
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    controller.randomizeName()
                },
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.songcast_randomize_name),
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TabCreateRoom(controller: SongCastMenuLayout) {
    Column {
        LabelText(R.string.songcast_create_room_hint)
        NameTextField(controller)
        Button(
            onClick = {
                GlobalScope.launch {
                    safeExecute {
                        controller.createRoom()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.songcast_create_new_room))
        }
    }
}

@Composable
private fun TabJoinRoom(controller: SongCastMenuLayout) {
    Column {
        NameTextField(controller)
        LabelText(R.string.songcast_enter_room_number_to_join)
        OutlinedTextField(
            value = controller.state.roomCode,
            onValueChange = { controller.state.roomCode = it },
            label = { Text(stringResource(R.string.songcast_room_code_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                GlobalScope.launch {
                    safeExecute {
                        controller.joinRoom()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.songcast_join_room))
        }
    }
}

@Composable
private fun TabRestoreRoom(controller: SongCastMenuLayout) {
    Column {
        LabelText(R.string.songcast_restore_room_hint)
        Button(
            onClick = {
                GlobalScope.launch {
                    safeExecute {
                        controller.restoreRoom()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.songcast_restore_a_room))
        }
    }
}