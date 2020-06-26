package igrek.songbook.share

import android.view.View
import android.widget.Button
import igrek.songbook.R
import igrek.songbook.layout.InflatedLayout

class ScreenShareLayoutController(
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_screen_share
) {

    private val btService: BluetoothService = BluetoothService(activity)

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<Button>(R.id.hostNewRoomButton)?.setOnClickListener {
            btService.bluetoothOn()
        }

        layout.findViewById<JoinRoomListView>(R.id.itemsListView)?.also {
            it.onClickCallback = { item ->
                logger.debug("click: ${item.name}")
            }
            it.items = listOf(JoinRoom("dupa"), JoinRoom("two"))
        }
    }

}