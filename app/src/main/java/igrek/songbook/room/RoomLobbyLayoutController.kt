package igrek.songbook.room

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.InputDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class RoomLobbyLayoutController(
        bluetoothService: LazyInject<BluetoothService> = appFactory.bluetoothService,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_room_lobby
) {
    private val bluetoothService by LazyExtractor(bluetoothService)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private var chatListView: RoomChatListView? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.setOnClickListener(SafeClickListener {
            showMoreActions()
        })

        chatListView = layout.findViewById<RoomChatListView>(R.id.itemsListView)?.also {
            it.onClickCallback = { message ->
                logger.debug("click: $message")
            }
            it.items = listOf(
                    ChatMessage("igrek51", "elo mordo", Date()),
                    ChatMessage("snsv", "no elo", Date()),
            )
        }

    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(mutableListOf(
                ContextMenuBuilder.Action(R.string.room_close_room) {

                },
        ))
    }

}