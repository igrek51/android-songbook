package igrek.songbook.share

import android.Manifest
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.util.waitUntil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.io.IOException
import java.util.*


class BluetoothService(
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
) {
    private val activity by LazyExtractor(appCompatActivity)

    companion object {
        private val BT_APP_UUID = UUID.fromString("eb5d5f8c-8a33-465d-5151-3c2e36cb5490")

        private const val REQUEST_ENABLE_BT = 20
    }

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBluetoothSocketStream // bluetooth background worker thread to send and receive data
            : BluetoothSocketStream? = null
    private val discoveredDevices: HashMap<String, BluetoothDevice> = hashMapOf()

    private var roomChannel: Channel<Room> = Channel()

    fun deviceName(): String {
        return bluetoothAdapter.name.orEmpty()
    }

    fun rescanRooms(): Deferred<Result<Channel<Room>>> {
        return GlobalScope.async {
            return@async runCatching {
                ensureBluetoothEnabled()

                roomChannel.close()
                roomChannel = Channel()

                bluetoothAdapter.cancelDiscovery()
                bluetoothAdapter.startDiscovery()

                activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
                activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
                activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

                return@runCatching roomChannel
            }
        }
    }

    private val discoveryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // add the name to the list
                    LoggerFactory.logger.debug("""new device discovered
                        ${device.name}
                        ${device.address}
                        """.trimIndent())
                    discoveredDevices[device.address] = device

                    GlobalScope.launch {
                        try {
                            roomChannel.send(Room(name = device.name, hostAddress = device.address))
                        } catch (e: ClosedSendChannelException) {
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    LoggerFactory.logger.debug("Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    roomChannel.close()
                    LoggerFactory.logger.debug("Discovery finished")
                }
            }
        }
    }

    fun ensureBluetoothEnabled() {
        // Coarse Location permission required to discover devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            }
        }

        if (bluetoothAdapter.isEnabled) {
            return
        }

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

        val turnOnResult = waitUntil(retries = 20, delayMs = 250) {
            bluetoothAdapter.isEnabled
        }
        if (!turnOnResult)
            throw RuntimeException("Bluetooth not accessible. Try again.")
    }

    fun discover() {
        if (bluetoothAdapter.isDiscovering) {
            LoggerFactory.logger.debug("Discovery already started")
            return
        }

        if (bluetoothAdapter.isEnabled) {
            LoggerFactory.logger.debug("Starting discovery")
            bluetoothAdapter.startDiscovery()
            activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
            activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        } else {
            LoggerFactory.logger.debug("Bluetooth not on")
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

    fun connectToAll() {
        LoggerFactory.logger.debug("Connecting to all ${discoveredDevices.size} devices")
        discoveredDevices.forEach { (macAddress, device) ->
            connect(macAddress)
        }
    }

    private fun connect(macAddress: String) {
        bluetoothAdapter.cancelDiscovery()

        GlobalScope.launch(Dispatchers.IO) {
            val mBTSocket: BluetoothSocket

            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            LoggerFactory.logger.debug("Connecting to $macAddress - ${device.name}")
            try {
                mBTSocket = createBluetoothSocket(device)
            } catch (e: IOException) {
                LoggerFactory.logger.error("Socket creation failed to ${device.name}", e)
                return@launch
            }
            LoggerFactory.logger.debug("socket created to ${device.name}")

            try {
                mBTSocket.connect()
                LoggerFactory.logger.debug("socket connected to ${device.name}")
            } catch (e: IOException) {
                try {
                    LoggerFactory.logger.error("socket connection failed to ${device.name}", e)
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
        LoggerFactory.logger.debug("Sending datagram")
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
                    try {
                        // This will block until there is a connection
                        socket = serverSocket.accept()
                    } catch (connectException: IOException) {
                        LoggerFactory.logger.error("Failed to accept Bluetooth connection", connectException)
                        break
                    }

                    LoggerFactory.logger.debug("socket accepted")
                    macAddress = socket.remoteDevice.address
                    LoggerFactory.logger.debug("accepted $macAddress")
                    mBluetoothSocketStream = BluetoothSocketStream(socket)
                    mBluetoothSocketStream?.start()
                } catch (connectException: IOException) {
                    LoggerFactory.logger.error("Failed to start a Bluetooth connection as a server", connectException)
                    socket?.close()
                }
            }
        }
    }

    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return try {
            device.createInsecureRfcommSocketToServiceRecord(BT_APP_UUID)
        } catch (e: Exception) {
            LoggerFactory.logger.error("Could not create Insecure RFComm Connection", e)
            device.createRfcommSocketToServiceRecord(BT_APP_UUID)
        }
    }

    fun listen() {
        bluetoothAdapter.cancelDiscovery()
        LoggerFactory.logger.debug("starting BT server...")
        BluetoothListenServer(BT_APP_UUID, bluetoothAdapter).start()
    }

    fun makeDiscoverable() {
        if (bluetoothAdapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            LoggerFactory.logger.warn("already discoverable")
            return
        }
        LoggerFactory.logger.warn("asking for discoverability")
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        activity.startActivity(discoverableIntent)
    }
}