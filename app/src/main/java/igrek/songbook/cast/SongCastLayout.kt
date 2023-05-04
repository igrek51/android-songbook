package igrek.songbook.cast

import android.view.View
import android.widget.Button
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import kotlinx.coroutines.*
import com.google.android.material.textfield.TextInputLayout
import igrek.songbook.admin.ApiResponseError
import igrek.songbook.info.errorcheck.LocalizedError
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeExecute

@OptIn(DelicateCoroutinesApi::class)
class SongCastLayout(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_cast
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songCastService by LazyExtractor(songCastService)

    private var nameInput: TextInputLayout? = null
    private var roomCodeInput: TextInputLayout? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        if (songCastService.isInRoom()) {
            GlobalScope.launch(Dispatchers.Main) {
                layoutController.showLayout(SongCastLobbyLayout::class, disableReturn = true)
            }
            return
        }

        nameInput = layout.findViewById<TextInputLayout?>(R.id.nameInput)?.also {
            val newName = AnimalNameFeeder().generateName()
            it.editText?.setText(newName)

            it.setEndIconOnClickListener {
                randomizeName()
            }
        }
        roomCodeInput = layout.findViewById(R.id.roomCodeInput)

        layout.findViewById<Button>(R.id.createNewRoomButton)
            ?.setOnClickListener {
                GlobalScope.launch {
                    safeExecute {
                        createRoom()
                    }
                }
            }

        layout.findViewById<Button>(R.id.joinRoomButton)
            ?.setOnClickListener(SafeClickListener {
                GlobalScope.launch {
                    safeExecute {
                        joinRoom()
                    }
                }
            })
    }

    private fun randomizeName() {
        nameInput?.editText?.setText(AnimalNameFeeder().generateName())
    }

    private fun getMemberName(): String {
        return nameInput?.editText?.text.toString().takeIf { it.isNotBlank() }
            ?: AnimalNameFeeder().generateName()
    }

    private suspend fun createRoom() {
        uiInfoService.showInfo(R.string.songcast_creating_room, indefinite = true)
        val result = songCastService.createSessionAsync(getMemberName()).await()
        result.fold(onSuccess = { response: CastSessionJoined ->
            layoutController.showLayout(SongCastLobbyLayout::class) {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(100) // WTF: Android hackaround
                    when (response.rejoined) {
                        true -> uiInfoService.showInfo(R.string.songcast_room_created_rejoined, response.member_name)
                        false -> uiInfoService.showInfo(R.string.songcast_room_created, response.member_name)
                    }
                }
            }
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private suspend fun joinRoom() {
        val roomCode = roomCodeInput?.editText?.text.toString().replace(" ", "").trim()
        if (roomCode.isBlank()) {
            throw LocalizedError(R.string.songcast_room_code_empty)
        }
        if (!Regex("[0-9]{6}").matches(roomCode)) {
            throw LocalizedError(R.string.songcast_room_code_invalid)
        }

        uiInfoService.showInfo(R.string.songcast_joining_room, indefinite = true)
        val result = songCastService.joinSessionAsync(roomCode, getMemberName()).await()
        result.fold(onSuccess = { response: CastSessionJoined ->
            layoutController.showLayout(SongCastLobbyLayout::class) {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(100) // WTF: Android hackaround
                    when (response.rejoined) {
                        true -> uiInfoService.showInfo(R.string.songcast_room_joined_rejoined, response.member_name)
                        false -> uiInfoService.showInfo(R.string.songcast_room_joined, response.member_name)
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

}