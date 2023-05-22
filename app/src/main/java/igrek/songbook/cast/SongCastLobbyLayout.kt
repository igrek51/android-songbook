package igrek.songbook.cast

import android.view.View
import android.widget.ImageButton
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.compose.DarkColors
import igrek.songbook.compose.RichText
import igrek.songbook.compose.SwitchWithLabel
import igrek.songbook.compose.md_theme_light_primaryContainer
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeAsyncExecutor
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.system.ClipboardManager
import igrek.songbook.util.formatTimestampKitchen
import kotlinx.coroutines.*

class SongCastLobbyLayout(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
    clipboardManager: LazyInject<ClipboardManager> = appFactory.clipboardManager,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_cast_lobby
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    val songCastService by LazyExtractor(songCastService)
    private val clipboardManager by LazyExtractor(clipboardManager)

    val state = SongCastLobbyState()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        if (!songCastService.isInRoom()) {
            GlobalScope.launch(Dispatchers.Main) {
                layoutController.showLayout(SongCastMenuLayout::class, disableReturn = true)
            }
            return
        }

        val sessionShortId = songCastService.sessionCode ?: ""
        val splittedCode = sessionShortId.take(3) + " " + sessionShortId.drop(3)
        state.roomCode = splittedCode

        layout.findViewById<ImageButton>(R.id.moreActionsButton)
            ?.setOnClickListener(SafeClickListener {
                showMoreActions()
            })

        songCastService.onSessionUpdated = ::onSessionUpdated

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }

        layout.post {
            appFactory.softKeyboardService.get().hideSoftKeyboard()
            updateSessionDetails()
        }
    }

    private fun refreshSessionDetails() {
        GlobalScope.launch {
            songCastService.refreshSessionDetails()
        }
    }

    fun openCurrentSong() {
        songCastService.openCastSong()
    }

    fun sendChatMessage() {
        uiInfoService.showInfo(R.string.songcast_chat_message_sending)
        val text = state.currentChat.trim()
        if (text.isBlank()) {
            uiInfoService.showInfo(R.string.songcast_chat_message_empty)
            return
        }

        GlobalScope.launch {
            val payload = CastChatMessageSent(text = text)
            val result = songCastService.postChatMessageAsync(payload).await()
            result.fold(onSuccess = {
                uiInfoService.showInfo(R.string.songcast_chat_message_sent)
                withContext(Dispatchers.Main) {
                    state.currentChat = ""
                }
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    private fun updateSessionDetails() {
        val songName = when (songCastService.sessionState.castSongDto) {
            null -> uiInfoService.resString(R.string.songcast_songname_none)
            else -> "${songCastService.sessionState.castSongDto?.title} - ${songCastService.sessionState.castSongDto?.artist}"
        }
        state.currentSongName = songName

        state.isPresenter = songCastService.isPresenter()

        state.presenters.clear()
        state.presenters.addAll(songCastService.presenters)

        state.spectators.clear()
        state.spectators.addAll(songCastService.spectators)

        state.logEventData.clear()
        state.logEventData.addAll(songCastService.generateChatEvents())
    }

    fun formatMember(member: CastMember): String {
        return when (member.public_member_id) {
            songCastService.myMemberPublicId -> {
                val youName = uiInfoService.resString(R.string.songcast_you)
                "${member.name} ($youName)"
            }
            else -> member.name
        }
    }

    private fun onSessionUpdated() {
        if (isLayoutVisible()) {
            updateSessionDetails()
        }
    }

    fun copySessionCode() {
        clipboardManager.copyToSystemClipboard(songCastService.sessionCode.orEmpty())
        uiInfoService.showInfo(R.string.songcast_code_copied)
    }

    private fun showMoreActions() {
        val actions = mutableListOf(
            ContextMenuBuilder.Action(R.string.songcast_exit_room) {
                leaveRoomConfirm()
            },
            ContextMenuBuilder.Action(R.string.songcast_refresh_session_details) {
                refreshSessionDetails()
            },
        )
        if (songCastService.isPresenter()) {
            actions.add(
                ContextMenuBuilder.Action(R.string.songcast_promote_member_to_presenter) {
                    choosePromotedUser()
                },
            )
        }
        ContextMenuBuilder().showContextMenu(actions)
    }

    private fun choosePromotedUser() {
        if (songCastService.spectators.isEmpty()) return

        val actions = songCastService.spectators.map { member ->
            ContextMenuBuilder.Action(member.name) {
                promoteUser(member)
            }
        }
        ContextMenuBuilder().showContextMenu(actions)
    }

    private fun promoteUser(member: CastMember) {
        GlobalScope.launch {
            val result = songCastService.promoteMemberAsync(member.public_member_id).await()
            result.fold(onSuccess = {
                uiInfoService.showInfo(R.string.songcast_member_promoted, member.name)
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    fun openLastSong() {
        appFactory.songOpener.get().openLastSong()
    }

    fun waitingForPresenter(): Boolean {
        return songCastService.isPresenter() && !songCastService.isSongSelected()
    }

    private fun leaveRoomConfirm() {
        ConfirmDialogBuilder().confirmAction(R.string.songcast_confirm_leave_room) {
            GlobalScope.launch {
                exitRoom()
            }
        }
    }

    private fun leaveOrMinimizeConfirm() {
        uiInfoService.dialogThreeChoices(
            titleResId = R.string.songcast_leave_or_stay_title,
            messageResId = R.string.songcast_leave_or_minimize_room,
            negativeButton = R.string.songcast_action_leave, negativeAction = {
                GlobalScope.launch {
                    exitRoom()
                }
            },
            positiveButton = R.string.songcast_action_stay, positiveAction = {
                uiInfoService.showInfo(R.string.songcast_room_is_still_open)
                super.onBackClicked()
            }
        )
    }

    private suspend fun exitRoom() {
        uiInfoService.showInfo(R.string.songcast_you_left_the_room)
        val result = songCastService.dropSessionAsync().await()
        result.fold(onSuccess = {
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
        layoutController.showLayout(SongCastMenuLayout::class, disableReturn = true)
    }

    override fun onBackClicked() {
        when (songCastService.isInRoom()) {
            true -> leaveOrMinimizeConfirm()
            else -> super.onBackClicked()
        }
    }
}

class SongCastLobbyState {
    var isPresenter: Boolean by mutableStateOf(false)
    var roomCode: String by mutableStateOf("")
    var currentSongName: String by mutableStateOf("")
    var currentChat: String by mutableStateOf("")
    var presenters: MutableList<CastMember> = mutableStateListOf()
    var spectators: MutableList<CastMember> = mutableStateListOf()
    var logEventData: MutableList<LogEvent> = mutableStateListOf()
}

@Composable
private fun MainComponent(controller: SongCastLobbyLayout) {
    Column(modifier = Modifier.padding(horizontal = 6.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            when (controller.state.isPresenter) {
                true -> RichText(R.string.songcast_lobby_text_presenter_hint)
                else -> RichText(R.string.songcast_lobby_text_guest_hint)
            }
        }
        RoomCodeField(controller)
        MembersLists(controller)

        SwitchWithLabel(stringResource(R.string.songcast_open_presented_song_automatically),
            controller.songCastService.clientOpenPresentedSongs) {
            controller.songCastService.clientOpenPresentedSongs = it
        }

        if (controller.waitingForPresenter())
            OpenLastSongButton(controller)
        EventsLog(controller)
    }
}

@Composable
private fun RoomCodeField(controller: SongCastLobbyLayout) {
    Row(Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = controller.state.roomCode,
            onValueChange = { },
            label = { Text(stringResource(R.string.songcast_room_code_hint)) },
            singleLine = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 24.sp,
                letterSpacing = 6.sp,
            ),
            trailingIcon = {
                IconButton(
                    onClick = { controller.copySessionCode() },
                ) {
                    Icon(
                        painterResource(id = R.drawable.copy),
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        contentDescription = stringResource(R.string.copy_to_clipboard),
                    )
                }
            },
        )
    }
}

@Composable
private fun MembersLists(controller: SongCastLobbyLayout) {
    if (controller.state.presenters.isNotEmpty()) {
        TextHeader(stringResource(R.string.songcast_members_presenters))
        MembersList(controller, controller.state.presenters)
    }
    if (controller.state.spectators.isNotEmpty()) {
        TextHeader(stringResource(R.string.songcast_members_spectators))
        MembersList(controller, controller.state.spectators)
    }
}

@Composable
private fun MembersList(controller: SongCastLobbyLayout, members: List<CastMember>) {
    members.forEach { member ->
        Row(Modifier.padding(horizontal = 8.dp)) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = controller.formatMember(member),
                style = MaterialTheme.typography.labelMedium,
                color = DarkColors.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventsLog(controller: SongCastLobbyLayout) {
    controller.state.logEventData.forEach {
        CLogEvent(it, controller)
    }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
    ) {
        OutlinedTextField(
            value = controller.state.currentChat,
            onValueChange = { controller.state.currentChat = it },
            label = { Text(stringResource(R.string.songcast_hint_chat_message)) },
            singleLine = true,
            modifier = Modifier.weight(1f).onFocusChanged {
                if (it.isFocused) {
                    coroutineScope.launch {
                        delay(400) // wait for keyboard to appear
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                GlobalScope.launch {
                    safeExecute {
                        controller.sendChatMessage()
                    }
                }
            }),
        )
        IconButton(
            modifier = Modifier
                .size(36.dp)
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            onClick = safeAsyncExecutor {
                controller.sendChatMessage()
            }
        ) {
            Icon(
                painterResource(id = R.drawable.send),
                contentDescription = stringResource(R.string.songcast_send_chat_icon_description),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun CLogEvent(event: LogEvent, controller: SongCastLobbyLayout) {
    when (event) {
        is SystemLogEvent -> {
            TimeHeader(event.timestampMs)
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = event.text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is MessageLogEvent -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 8.dp)) {
                    Text(
                        textAlign = TextAlign.Left,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        text = event.author,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = formatTimestampKitchen(event.timestampMs / 1000),
                    )
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 8.dp),
                    textAlign = TextAlign.Left,
                    text = event.text,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        is SongLogEvent -> {
            TimeHeader(event.timestampMs)
            val songName = event.song.displayName()
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.songcast_song_selected, event.author, songName),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val isLastSong = event == controller.state.logEventData.last { it is SongLogEvent }
            if (isLastSong) {
                Row(Modifier.padding(vertical = 8.dp)) {
                    Button(
                        onClick = safeAsyncExecutor {
                            controller.openCurrentSong()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
                    ) {
                        Icon(
                            painterResource(id = R.drawable.note),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = md_theme_light_primaryContainer,
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.songcast_open_current_song, songName))
                    }
                }
            }
        }
    }
}

@Composable
fun TimeHeader(timestampMs: Long) {
    val timeFormatted = formatTimestampKitchen(timestampMs / 1000)
    TextHeader(timeFormatted)
}

@Composable
fun TextHeader(text: String) {
    Row(modifier = Modifier
        .padding(vertical = 8.dp, horizontal = 16.dp)
        .height(12.dp)) {
        Divider(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Divider(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        )
    }
}

@Composable
fun OpenLastSongButton(controller: SongCastLobbyLayout) {
    Row(Modifier.padding(vertical = 8.dp)) {
        Button(
            onClick = safeAsyncExecutor {
                controller.openLastSong()
            },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        ) {
            Icon(
                painterResource(id = R.drawable.note),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = md_theme_light_primaryContainer,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.songcast_open_last_song))
        }
    }
}
