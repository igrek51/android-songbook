package igrek.songbook.share

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import igrek.songbook.info.logger.LoggerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class BluetoothService(private val activity: Activity) {

    companion object {
        private val BT_MODULE_UUID = UUID.fromString("eb5d5f8c-8a33-465d-aaec-3c2e36cb5490")

        private const val REQUEST_ENABLE_BT = 20
    }

    var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBluetoothSocketStream // bluetooth background worker thread to send and receive data
            : BluetoothSocketStream? = null

    fun bluetoothOn() {
        // Ask for location permission if not already allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            }
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            LoggerFactory.logger.debug("Bluetooth turned on")
        } else {
            LoggerFactory.logger.debug("Bluetooth is already on")
        }
    }

    fun discover() {
        if (bluetoothAdapter.isDiscovering) {
            LoggerFactory.logger.debug("Discovery already started")
        }

        if (bluetoothAdapter.isEnabled) {
            LoggerFactory.logger.debug("Starting discovery")
            bluetoothAdapter.startDiscovery()
            activity.registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            activity.registerReceiver(blReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
            activity.registerReceiver(blReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        } else {
            LoggerFactory.logger.debug("Bluetooth not on")
        }
    }

    private val blReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // add the name to the list
                    LoggerFactory.logger.debug("""new device discovered
                        ${device.name}
                        ${device.address}
                        """.trimIndent())
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    LoggerFactory.logger.debug("Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    LoggerFactory.logger.debug("Discovery finished")
                }
            }
        }
    }

    fun listPairedDevices() {
        val mPairedDevices = bluetoothAdapter.bondedDevices
        if (bluetoothAdapter.isEnabled) {
            LoggerFactory.logger.debug("Show Paired Devices")
            for (device in mPairedDevices!!) {
                LoggerFactory.logger.debug("listing device " + device.name + "\n" + device.address)
            }
        } else {
            LoggerFactory.logger.debug("Bluetooth not on")
        }
    }

    fun connect() {
        bluetoothAdapter.cancelDiscovery()

        val address = "14:DD:A9:66:6C:61"
        LoggerFactory.logger.debug("Connecting")

        GlobalScope.launch(Dispatchers.IO) {
            val mBTSocket: BluetoothSocket

            val device = bluetoothAdapter.getRemoteDevice(address)
            try {
                mBTSocket = createBluetoothSocket(device)
            } catch (e: IOException) {
                LoggerFactory.logger.error("Socket creation failed", e)
                return@launch
            }
            LoggerFactory.logger.debug("socket created")

            try {
                mBTSocket.connect()
                LoggerFactory.logger.debug("socket connected")
            } catch (e: IOException) {
                try {
                    LoggerFactory.logger.error("socket connection failed", e)
                    mBTSocket.close()
                    return@launch
                } catch (e2: IOException) {
                    //insert code to deal with this
                    LoggerFactory.logger.debug("Socket closing failed")
                }
            }
            mBluetoothSocketStream = BluetoothSocketStream(mBTSocket)
            mBluetoothSocketStream?.start()
        }
    }

    fun send() {
        mBluetoothSocketStream?.write("Hello dupa!")
    }

    inner class BluetoothListenServer(private val uuid: UUID?, private val bluetoothAdapter: BluetoothAdapter) : Thread() {
        override fun run() {
            val serverSocket: BluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Songbook", uuid)
            var socket: BluetoothSocket? = null
            while (true) {
                var macAddress: String?
                try {
                    LoggerFactory.logger.debug("Bluetooth server is listening for a client")
                    // This will block until there is a connection
                    socket = serverSocket.accept()

                    LoggerFactory.logger.debug("socket accepted")
                    macAddress = socket.remoteDevice.address
                    LoggerFactory.logger.debug("accepted $macAddress")
                    mBluetoothSocketStream = BluetoothSocketStream(socket)
                    mBluetoothSocketStream?.start()
                } catch (connectException: IOException) {
                    LoggerFactory.logger.error("Failed to start a Bluetooth connection as a server", connectException)
                    socket?.close()
                    break
                }
            }
        }
    }

    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return try {
            device.createInsecureRfcommSocketToServiceRecord(BT_MODULE_UUID)
        } catch (e: Exception) {
            LoggerFactory.logger.error("Could not create Insecure RFComm Connection", e)
            device.createRfcommSocketToServiceRecord(BT_MODULE_UUID)
        }
    }

    fun listen() {
        bluetoothAdapter.cancelDiscovery()
        LoggerFactory.logger.debug("starting BT server...")
        BluetoothListenServer(BT_MODULE_UUID, bluetoothAdapter).start()
    }
}