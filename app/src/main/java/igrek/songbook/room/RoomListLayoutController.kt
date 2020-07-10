package igrek.songbook.room

import android.os.Handler
import android.os.Looper
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

class RoomListLayoutController(
        bluetoothService: LazyInject<BluetoothService> = appFactory.bluetoothService,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_rooms_list
) {
    private val bluetoothService by LazyExtractor(bluetoothService)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private var joinRoomListView: JoinRoomListView? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.setOnClickListener(SafeClickListener {
            showMoreActions()
        })

        layout.findViewById<EditText>(R.id.myNameEditText)?.also {
            it.setText(bluetoothService.deviceName())
        }

        layout.findViewById<Button>(R.id.hostNewRoomButton)?.setOnClickListener {
            hostRoom()
        }

        joinRoomListView = layout.findViewById<JoinRoomListView>(R.id.itemsListView)?.also {
            it.onClickCallback = { room ->
                logger.debug("connecting: ${room.name}")
                bluetoothService.connectToRoom(room)
                Handler(Looper.getMainLooper()).post {
                    layoutController.showLayout(RoomLobbyLayoutController::class)
                }
            }
        }

        layout.findViewById<Button>(R.id.scanRoomsButtton)?.setOnClickListener {
            scanRooms()
        }
    }

    private fun hostRoom() {
        InputDialogBuilder().input(R.string.screen_share_set_room_password, null) { password ->
            GlobalScope.launch {
                bluetoothService.hostRoom(password).await().fold(onSuccess = {
                    uiInfoService.showInfo("room created")
                    layoutController.showLayout(RoomLobbyLayoutController::class)
                }, onFailure = { e ->
                    logger.error(e)
                    uiInfoService.showInfoIndefinite(R.string.error_communication_breakdown, e.message.orEmpty())
                })

            }
        }
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(mutableListOf(
                ContextMenuBuilder.Action(R.string.screen_share_scan_rooms) {
                    scanRooms()
                },
        ))
    }

    private fun scanRooms() {
        joinRoomListView?.items = emptyList()
        uiInfoService.showInfoIndefinite(R.string.screen_share_scanning_devices)

        GlobalScope.launch(Dispatchers.Main) {
            bluetoothService.scanRooms().await().fold(onSuccess = { roomCh ->
                for (room in roomCh) {
                    joinRoomListView?.items = joinRoomListView?.items.orEmpty() + room
                }
                uiInfoService.showInfo("scanning completed")
            }, onFailure = { e ->
                logger.error(e)
                uiInfoService.showInfoIndefinite(R.string.error_communication_breakdown, e.message.orEmpty())
            })
        }
    }

}