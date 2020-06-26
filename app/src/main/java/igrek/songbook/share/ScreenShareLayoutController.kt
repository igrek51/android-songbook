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

        layout.findViewById<Button>(R.id.onButton)?.setOnClickListener {
            btService.bluetoothOn()
        }

        layout.findViewById<Button>(R.id.discoverableButton)?.setOnClickListener {
            btService.makeDiscoverable()
        }

        layout.findViewById<Button>(R.id.discoverButton)?.setOnClickListener {
            btService.discover()
        }

        layout.findViewById<Button>(R.id.listButton)?.setOnClickListener {
            btService.listPairedDevices()
        }

        layout.findViewById<Button>(R.id.listenButton)?.setOnClickListener {
            btService.listen()
        }

        layout.findViewById<Button>(R.id.connectButton)?.setOnClickListener {
            btService.connectToAll()
        }

        layout.findViewById<Button>(R.id.sendButton)?.setOnClickListener {
            btService.send()
        }
    }

}