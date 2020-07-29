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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoomLobbyLayoutController(
        roomLobby: LazyInject<RoomLobby> = appFactory.roomLobby,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_room_lobby
) {
    private val roomLobby by LazyExtractor(roomLobby)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private var chatListView: RoomChatListView? = null
    private var chatMessageEdit: EditText? = null
    private var membersTextView: TextView? = null

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

        roomLobby.newChatMessageCallback = { chatMessage: ChatMessage ->
            chatListView?.add(chatMessage)
        }

        membersTextView = layout.findViewById(R.id.membersTextView)
        updateUsernames(roomLobby.usernames)
        roomLobby.updateUsersCallback = ::updateUsernames

        roomLobby.onDisconnectCallback = ::onDisconnected
    }

    private fun onDisconnected() {
        uiInfoService.showInfo("dropped from host")
        if (isLayoutVisible()) {
            layoutController.showLayout(RoomListLayoutController::class)
        }
    }

    private fun updateUsernames(usernames: List<String>) {
        val usernamesStr = usernames.joinToString("\n") { "- $it" }
        membersTextView?.text = "Members:\n$usernamesStr"
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(mutableListOf(
                ContextMenuBuilder.Action(R.string.room_close_room) {
                    GlobalScope.launch {
                        roomLobby.close()
                        layoutController.showLayout(RoomListLayoutController::class)
                    }
                },
                ContextMenuBuilder.Action(R.string.room_make_discoverable) {
                    roomLobby.makeDiscoverable()
                },
        ))
    }

}