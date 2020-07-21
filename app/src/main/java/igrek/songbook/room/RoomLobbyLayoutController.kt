package igrek.songbook.room

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import igrek.songbook.R
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder

class RoomLobbyLayoutController(
        roomLobby: LazyInject<RoomLobby> = appFactory.roomLobby,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_room_lobby
) {
    private val roomLobby by LazyExtractor(roomLobby)

    private var chatListView: RoomChatListView? = null
    private var chatMessageEdit: EditText? = null

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

        roomLobby.newMessageListener = { chatMessage: ChatMessage ->
            chatListView?.add(chatMessage)
        }
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(mutableListOf(
                ContextMenuBuilder.Action(R.string.room_close_room) {

                },
        ))
    }

}