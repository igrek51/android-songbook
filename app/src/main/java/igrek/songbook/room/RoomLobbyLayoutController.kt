package igrek.songbook.room

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RoomLobbyLayoutController(
        roomLobby: LazyInject<RoomLobby> = appFactory.roomLobby,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        songOpener: LazyInject<SongOpener> = appFactory.songOpener,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_room_lobby
) {
    private val roomLobby by LazyExtractor(roomLobby)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songOpener by LazyExtractor(songOpener)

    private var chatListView: RoomChatListView? = null
    private var chatMessageEdit: EditText? = null
    private var membersTextView: TextView? = null
    private var selectedSongTextView: TextView? = null
    private var openSelectedSongButton: Button? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.setOnClickListener(SafeClickListener {
            showMoreActions()
        })

        openSelectedSongButton = layout.findViewById<Button>(R.id.openSelectedSongButton)?.also {
            it.setOnClickListener(SafeClickListener {
                GlobalScope.launch {
                    roomLobby.currentSong?.let { currentSong ->
                        songOpener.openSongPreview(currentSong)
                    }
                }
            })
        }
        selectedSongTextView = layout.findViewById(R.id.selectedSongTextView)
        updateOpenSelectedSongWidgets()

        chatListView = layout.findViewById<RoomChatListView>(R.id.itemsListView)?.also {
            it.onClickCallback = {}
            it.items = listOf()
            it.enableNestedScrolling()
        }
        chatMessageEdit = layout.findViewById(R.id.chatMessageEdit)
        layout.findViewById<Button>(R.id.chatSendButton)?.setOnClickListener(SafeClickListener {
            sendChatMessage()
        })

        roomLobby.newChatMessageCallback = { chatMessage: ChatMessage ->
            chatListView?.add(chatMessage)
            chatListView?.scrollToBottom()
        }
        updateChatMessages(roomLobby.chatHistory)

        membersTextView = layout.findViewById(R.id.membersTextView)
        updateMembers(roomLobby.clients)
        roomLobby.updateMembersCallback = ::updateMembers

        roomLobby.onDroppedCallback = ::onDropped
        roomLobby.onOpenSong = ::onOpenSong
        roomLobby.onModelChanged = {
            if (isLayoutVisible()) {
                updateOpenSelectedSongWidgets()
                updateMembers(roomLobby.clients)
            }
        }
    }

    private fun updateChatMessages(chatHistory: List<ChatMessage>) {
        chatListView?.let { chatListView ->
            chatListView.items = chatHistory
            chatListView.scrollToBottom()
        }
    }

    private fun sendChatMessage() {
        val message = chatMessageEdit?.text?.toString().orEmpty()
        if (message.isEmpty()) {
            uiInfoService.showInfo(R.string.room_empty_message_given)
            return
        }
        roomLobby.sendChatMessage(message)
        chatMessageEdit?.setText("")
    }

    private fun updateOpenSelectedSongWidgets() {
        openSelectedSongButton?.visibility = when (roomLobby.currentSong) {
            null -> View.INVISIBLE
            else -> View.VISIBLE
        }
        selectedSongTextView?.let { selectedSongTextView ->
            val defaultNoSongMessage = uiInfoService.resString(when (roomLobby.peerStatus) {
                PeerStatus.Master -> R.string.room_current_song_waiting_master
                else -> R.string.room_current_song_waiting
            })
            val currentSongName = roomLobby.currentSong?.displayName() ?: defaultNoSongMessage
            selectedSongTextView.text = uiInfoService.resString(R.string.room_current_song, currentSongName)
        }
    }

    private fun onDropped() {
        uiInfoService.showInfo(R.string.room_dropped_from_host)
        if (isLayoutVisible()) {
            layoutController.showLayout(RoomListLayoutController::class)
        }
    }

    private fun updateMembers(members: List<PeerClient>) {
        val membersStr = members.map { it.displayMember() }.joinToString("\n") { "- $it" }
        membersTextView?.text = uiInfoService.resString(R.string.room_members, membersStr)
    }

    private fun PeerClient.displayMember(): String {
        val hostname = uiInfoService.resString(R.string.room_host)
        val role = when (this.status) {
            PeerStatus.Master -> " ($hostname)"
            else -> ""
        }
        return this.username + role
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(mutableListOf(
                ContextMenuBuilder.Action(R.string.room_close_room) {
                    GlobalScope.launch {
                        closeAndReturn()
                    }
                },
                ContextMenuBuilder.Action(R.string.room_make_discoverable) {
                    GlobalScope.launch {
                        roomLobby.makeDiscoverable()
                    }
                },
        ))
    }

    private fun onOpenSong(song: Song) {
        if (isLayoutVisible() || layoutController.isState(SongPreviewLayoutController::class)) {
            GlobalScope.launch {
                songOpener.openSongPreview(song)
            }
        }
    }

    override fun onBackClicked() {
        when (roomLobby.peerStatus) {
            PeerStatus.Master, PeerStatus.Slave -> {
                ConfirmDialogBuilder().confirmAction(R.string.room_confirm_leave_lobby) {
                    GlobalScope.launch {
                        closeAndReturn()
                    }
                }
            }
            PeerStatus.Disconnected -> super.onBackClicked()
        }
    }

    private suspend fun closeAndReturn() {
        roomLobby.close()
        layoutController.showLayout(RoomListLayoutController::class, disableReturn = true)
    }
}