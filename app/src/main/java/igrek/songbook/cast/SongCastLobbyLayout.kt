@file:OptIn(DelicateCoroutinesApi::class)

package igrek.songbook.cast

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.textfield.TextInputLayout
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.compose.LabelText
import igrek.songbook.compose.RichText
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.list.StringListView
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
    private val songCastService by LazyExtractor(songCastService)
    private val clipboardManager by LazyExtractor(clipboardManager)

    private var roomCodeInput: TextInputLayout? = null
    private var membersListView: StringListView? = null
    private var membersListView2: StringListView? = null
    private var chatListView: StringListView? = null
    private var songcastLobbyHint: TextView? = null
    private var selectedSongText: TextView? = null
    private var openSelectedSongButton: Button? = null
    private var chatMessageInput: TextInputLayout? = null

    val state = SongCastLobbyState()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        if (!songCastService.isInRoom()) {
            GlobalScope.launch(Dispatchers.Main) {
                layoutController.showLayout(SongCastLayout::class, disableReturn = true)
            }
            return
        }

        val sessionShortId = songCastService.sessionCode ?: ""
        val splittedCode = sessionShortId.take(3) + " " + sessionShortId.drop(3)
        state.roomCode = splittedCode

        roomCodeInput = layout.findViewById<TextInputLayout?>(R.id.roomCodeInput)?.also {
            it.editText?.setText(songCastService.sessionCode.orEmpty())
            it.editText?.setOnClickListener {
                copySessionCode()
            }
            it.setEndIconOnClickListener {
                copySessionCode()
            }
        }

        songcastLobbyHint = layout.findViewById(R.id.songcastLobbyHint)

        selectedSongText = layout.findViewById(R.id.selectedSongText)

        openSelectedSongButton = layout.findViewById<Button?>(R.id.openSelectedSongButton)?.also {
            it.setOnClickListener {
                safeExecute {
                    songCastService.openCurrentSong()
                }
            }
        }

        layout.findViewById<ImageButton>(R.id.moreActionsButton)
            ?.setOnClickListener(SafeClickListener {
                showMoreActions()
            })

        membersListView = layout.findViewById<StringListView>(R.id.membersListView)?.also {
            it.init()
        }
        membersListView2 = layout.findViewById<StringListView>(R.id.membersListView2)?.also {
            it.init()
        }
        chatListView = layout.findViewById<StringListView>(R.id.chatListView)?.also {
            it.init()
        }

        chatMessageInput = layout.findViewById(R.id.chatMessageInput)
        layout.findViewById<ImageButton>(R.id.chatSendButton)
            ?.setOnClickListener(SafeClickListener {
                sendChatMessage()
            })

        songCastService.onSessionUpdated = ::onSessionUpdated

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    MainPage(thisLayout)
                }
            }
        }

        layout.post {
            appFactory.softKeyboardService.get().hideSoftKeyboard()
            updateSessionDetails()
            refreshSessionDetails()
        }
    }

    private fun refreshSessionDetails() {
        GlobalScope.launch {
            songCastService.refreshSessionDetails()
        }
    }

    fun openCurrentSong() {
        songCastService.openCurrentSong()
    }

    fun sendChatMessage() {
        uiInfoService.showInfo(R.string.songcast_chat_message_sending)
        val text = chatMessageInput?.editText?.text.toString()
        GlobalScope.launch {
            val payload = CastChatMessageSent(
                text = text,
            )
            val result = songCastService.postChatMessageAsync(payload).await()
            result.fold(onSuccess = {
                uiInfoService.showInfo(R.string.songcast_chat_message_sent)
                withContext(Dispatchers.Main) {
                    chatMessageInput?.editText?.setText("")
                }
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    private fun formatMember(member: CastMember): String {
        if (member.public_member_id == songCastService.myMemberPublicId) {
            return "- ${member.name} (You)"
        }
        return "- ${member.name}"
    }

    private fun updateSessionDetails() {
        val sessionShortId = songCastService.sessionCode ?: ""
        val splittedCode = sessionShortId.take(3) + " " + sessionShortId.drop(3)
        roomCodeInput?.editText?.setText(splittedCode)

        membersListView?.items = songCastService.presenters.map { formatMember(it) }

        membersListView2?.items = songCastService.spectators.map { formatMember(it) }

        chatListView?.items = songCastService.sessionState.chatMessages.map {
            val timeFormatted = formatTimestampKitchen(it.timestamp)
            "[$timeFormatted] ${it.author}: ${it.text}"
        }

        val textRestId = if (songCastService.isPresenter()) {
            R.string.songcast_lobby_text_presenter_hint
        } else {
            R.string.songcast_lobby_text_guest_hint
        }
        songcastLobbyHint?.text = uiInfoService.resRichString(textRestId)

        val songName = when (songCastService.sessionState.castSongDto) {
            null -> "None"
            else -> "${songCastService.sessionState.castSongDto?.title} - ${songCastService.sessionState.castSongDto?.artist}"
        }
        state.currentSongName = songName

        state.presenters.clear()
        state.presenters.addAll(songCastService.presenters.map { formatMember(it) })

        state.spectators.clear()
        state.spectators.addAll(songCastService.spectators.map { formatMember(it) })

        state.chatMessages = songCastService.sessionState.chatMessages.map {
            val timeFormatted = formatTimestampKitchen(it.timestamp)
            "[$timeFormatted] ${it.author}: ${it.text}"
        }.toMutableList()

        state.isPresenter = songCastService.isPresenter()
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
        ContextMenuBuilder().showContextMenu(
            mutableListOf(
                ContextMenuBuilder.Action(R.string.songcast_exit_room) {
                    leaveRoomConfirm()
                },
                ContextMenuBuilder.Action(R.string.songcast_refresh_session_details) {
                    refreshSessionDetails()
                },
            )
        )
    }

    private fun leaveRoomConfirm() {
        ConfirmDialogBuilder().confirmAction(R.string.songcast_confirm_leave_room) {
            GlobalScope.launch {
                exitRoom()
            }
        }
    }

    private suspend fun exitRoom() {
        uiInfoService.showInfo(R.string.songcast_you_left_the_room)
        val result = songCastService.dropSessionAsync().await()
        result.fold(onSuccess = {
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
        layoutController.showLayout(SongCastLayout::class, disableReturn = true)
    }

    override fun onBackClicked() {
        when (songCastService.isInRoom()) {
            true -> leaveRoomConfirm()
            else -> super.onBackClicked()
        }
    }

}

class SongCastLobbyState {
    var isPresenter: Boolean by mutableStateOf(false)
    var roomCode: String by mutableStateOf("")
    var currentSongName: String by mutableStateOf("")
    var currentChat: String by mutableStateOf("")
    var presenters: MutableList<String> = mutableStateListOf()
    var spectators: MutableList<String> = mutableStateListOf()
    var chatMessages: MutableList<String> = mutableStateListOf()
}

@Composable
private fun MainPage(layout: SongCastLobbyLayout) {
//    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
    Column {
        when (layout.state.isPresenter) {
            true -> RichText(R.string.songcast_lobby_text_presenter_hint)
            else -> RichText(R.string.songcast_lobby_text_guest_hint)
        }

        OutlinedTextField(
            value = layout.state.roomCode,
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
                fontSize = 26.sp,
                letterSpacing = 5.sp,
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        layout.copySessionCode()
                    },
                ) {
                    Icon(
                        painterResource(id = R.drawable.copy),
                        contentDescription = stringResource(R.string.copy_to_clipboard),
                    )
                }
            },
        )

        LabelText(R.string.songcast_current_song, layout.state.currentSongName)

        Button(
            onClick = {
                GlobalScope.launch {
                    safeExecute {
                        layout.openCurrentSong()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.0.dp),
        ) {
            Text(stringResource(R.string.songcast_open_current_song))
        }

        Text(stringResource(R.string.songcast_members_presenters))
        layout.state.presenters.forEach {
            Text(text = it)
        }

        Text(stringResource(R.string.songcast_members_spectators))
        layout.state.spectators.forEach {
            Text(text = it)
        }

        Text(stringResource(R.string.songcast_chat))
        layout.state.chatMessages.forEach {
            Text(text = it)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = layout.state.currentChat,
                onValueChange = { layout.state.currentChat = it },
                label = { Text(stringResource(R.string.songcast_hint_chat_message)) },
                singleLine = false,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                modifier = Modifier.size(36.dp).padding(4.dp),
                onClick = {
                    GlobalScope.launch {
                        safeExecute {
                            layout.sendChatMessage()
                        }
                    }
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
}