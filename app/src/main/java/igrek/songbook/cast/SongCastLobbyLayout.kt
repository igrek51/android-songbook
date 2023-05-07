package igrek.songbook.cast

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import igrek.songbook.R
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

@OptIn(DelicateCoroutinesApi::class)
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

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        if (!songCastService.isInRoom()) {
            GlobalScope.launch(Dispatchers.Main) {
                layoutController.showLayout(SongCastLayout::class, disableReturn = true)
            }
            return
        }

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

        layout.post {
            updateSessionDetails()
            refreshSessionDetails()
        }
    }

    private fun refreshSessionDetails() {
        GlobalScope.launch {
            val result = songCastService.getSessionDetailsAsync().await()
            result.fold(onSuccess = {
                GlobalScope.launch(Dispatchers.Main) {
                    updateSessionDetails()
                }
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    private fun sendChatMessage() {
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
        selectedSongText?.text = uiInfoService.resString(R.string.songcast_current_song, songName)
    }

    private fun onSessionUpdated() {
        if (isLayoutVisible()) {
            updateSessionDetails()
        }
    }

    private fun copySessionCode() {
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