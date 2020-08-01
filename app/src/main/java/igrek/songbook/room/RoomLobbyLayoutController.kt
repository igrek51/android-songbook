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
import igrek.songbook.persistence.general.model.SongIdentifier
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

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.setOnClickListener(SafeClickListener {
            showMoreActions()
        })

        chatListView = layout.findViewById<RoomChatListView>(R.id.itemsListView)?.also {
            it.onClickCallback = { message ->
                logger.debug("click: $message")
            }
            it.items = listOf()
        }

        chatMessageEdit = layout.findViewById(R.id.chatMessageEdit)

        layout.findViewById<Button>(R.id.chatSendButton)?.setOnClickListener(SafeClickListener {
            roomLobby.sendChatMessage(chatMessageEdit?.text?.toString().orEmpty())
            chatMessageEdit?.setText("")
        })

        layout.findViewById<Button>(R.id.openSelectedSongButton)?.setOnClickListener(SafeClickListener {
            GlobalScope.launch {
                roomLobby.currentSongId?.let { currentSongId ->
                    songOpener.openSongIdentifier(currentSongId)
                }
            }
        })

        roomLobby.newChatMessageCallback = { chatMessage: ChatMessage ->
            chatListView?.add(chatMessage)
        }

        membersTextView = layout.findViewById(R.id.membersTextView)
        selectedSongTextView = layout.findViewById(R.id.membersTextView)
        updateMembers(roomLobby.clients)
        roomLobby.updateMembersCallback = ::updateMembers

        roomLobby.onDisconnectCallback = ::onDisconnected
        roomLobby.onSelectedSongChange = ::onSelectedSongChange
    }

    private fun onDisconnected() {
        uiInfoService.showInfo("dropped from host")
        if (isLayoutVisible()) {
            layoutController.showLayout(RoomListLayoutController::class)
        }
    }

    private fun updateMembers(members: List<PeerClient>) {
        val membersStr = members.map { it.displayMember() }.joinToString("\n") { "- $it" }
        membersTextView?.text = "Members:\n$membersStr"
    }

    private fun PeerClient.displayMember(): String {
        val role = when (this.status) {
            PeerStatus.Master -> " (Host)"
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
                    roomLobby.makeDiscoverable()
                },
        ))
    }

    private fun onSelectedSongChange(songId: SongIdentifier) {
        if (isLayoutVisible() || layoutController.isState(SongPreviewLayoutController::class)) {
            GlobalScope.launch {
                val result = songOpener.openSongIdentifier(songId)
                if (!result) {
                    logger.error("cant find selected song locally: $songId")
                }
            }
        }
    }

    override fun onBackClicked() {
        when (roomLobby.peerStatus) {
            PeerStatus.Master, PeerStatus.Slave -> {
                ConfirmDialogBuilder().confirmAction("You are about to leave lobby. Do you really want to disconnect?") {
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