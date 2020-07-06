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
import igrek.songbook.R
import igrek.songbook.info.errorcheck.LocalizedError
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.util.waitUntil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class BluetoothService(
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
) {
    private val activity by LazyExtractor(appCompatActivity)

    companion object {
        private val BT_APP_UUID = UUID.fromString("eb5d5f8c-8a33-465d-5151-3c2e36cb5490")

        private const val REQUEST_ENABLE_BT = 20
    }

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBluetoothSocketStream: BluetoothSocketStream? = null

    private val discoveredRoomDevices: ConcurrentHashMap<String, BluetoothDevice> = ConcurrentHashMap()
    private var discoveryJobs: MutableList<Job> = mutableListOf()
    private var roomChannel: Channel<Room> = Channel()

    fun deviceName(): String {
        return bluetoothAdapter.name.orEmpty()
    }

    fun scanRooms(): Deferred<Result<Channel<Room>>> = GlobalScope.async {
        return@async runCatching {
            ensureBluetoothEnabled()

            roomChannel.close()
            roomChannel = Channel(8)
            discoveredRoomDevices.clear()
            discoveryJobs.clear()

            launch {
                startDiscovery()
                // scanPairedDevices()
            }

            return@runCatching roomChannel
        }
    }

    private fun scanPairedDevices() {
        bluetoothAdapter.bondedDevices.forEach { pairedDevice ->
            detectRoomOnDevice(pairedDevice)
        }
    }

    private fun startDiscovery() {
        bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.startDiscovery()

        activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        activity.registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
    }

    private val discoveryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    onDeviceDiscovered(intent)
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    logger.debug("Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    logger.debug("Discovery finished")
                    onDiscoveryFinished()
                }
            }
        }
    }

    private fun onDiscoveryFinished() {
        GlobalScope.launch(Dispatchers.IO) {
            for (job in discoveryJobs) {
                job.join()
            }
            roomChannel.close()
        }
    }

    private fun onDeviceDiscovered(intent: Intent) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        logger.debug("BT device discovered: ${device.name} (${device.address})")
        detectRoomOnDevice(device)
    }

    private fun detectRoomOnDevice(device: BluetoothDevice) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            if (discoveredRoomDevices.containsKey(device.address))
                return@launch

            try {
                scanDeviceSocket(device.address)
            } catch (e: Throwable) {
                logger.warn("device ${device.address} room is unavailable: ${e.message}")
                return@launch
            }

            discoveredRoomDevices[device.address] = device
            try {
                roomChannel.send(Room(
                        name = device.name.orEmpty(),
                        hostAddress = device.address,
                ))
            } catch (e: ClosedSendChannelException) {
            }
        }
        discoveryJobs.add(job)
    }

    private fun scanDeviceSocket(address: String) {
        val device = bluetoothAdapter.getRemoteDevice(address)
        logger.debug("Connecting to ${device.name} ($address)")

        val socket: BluetoothSocket = try {
            device.createInsecureRfcommSocketToServiceRecord(BT_APP_UUID)
        } catch (e: Exception) {
            logger.warn("Could not create Insecure RFComm Connection", e)
            device.createRfcommSocketToServiceRecord(BT_APP_UUID)
        }

        socket.connect()

        logger.debug("room found on ${device.name} ($address)")

        socket.close()
    }

    private fun ensureBluetoothEnabled() {
        // Coarse Location permission required to discover devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            }
        }

        if (bluetoothAdapter.isEnabled)
            return

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        val turnOnResult = waitUntil(retries = 20, delayMs = 500) {
            bluetoothAdapter.isEnabled
        }
        if (!turnOnResult)
            throw LocalizedError(R.string.error_bluetooth_not_enabled)
    }

    fun connectToAll() {
        logger.debug("Connecting to all ${discoveredRoomDevices.size} devices")
        discoveredRoomDevices.forEach { (macAddress, device) ->
            connect(macAddress)
        }
    }

    private fun connect(macAddress: String) {
        bluetoothAdapter.cancelDiscovery()

        GlobalScope.launch(Dispatchers.IO) {
            val mBTSocket: BluetoothSocket

            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            logger.debug("Connecting to $macAddress - ${device.name}")
            try {
                mBTSocket = createBluetoothSocket(device)
            } catch (e: IOException) {
                logger.error("Socket creation failed to ${device.name}", e)
                return@launch
            }
            logger.debug("socket created to ${device.name}")

            try {
                mBTSocket.connect()
                logger.debug("socket connected to ${device.name}")
            } catch (e: IOException) {
                try {
                    logger.error("socket connection failed to ${device.name}", e)
                    mBTSocket.close()
                    return@launch
                } catch (e2: IOException) {
                    //insert code to deal with this
                    logger.debug("Socket closing failed")
                }
            }
            mBluetoothSocketStream = BluetoothSocketStream(mBTSocket)
            mBluetoothSocketStream?.start()
        }
    }

    fun send() {
        logger.debug("Sending datagram")
        mBluetoothSocketStream?.write("Hello dupa!")
    }

    inner class BluetoothListenServer(private val uuid: UUID?, private val bluetoothAdapter: BluetoothAdapter) : Thread() {
        override fun run() {
            val serverSocket: BluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Songbook", uuid)
            var socket: BluetoothSocket? = null
            while (true) {
                var macAddress: String?
                try {
                    logger.debug("Bluetooth server is listening for a client")
                    try {
                        // This will block until there is a connection
                        socket = serverSocket.accept()
                    } catch (connectException: IOException) {
                        logger.error("Failed to accept Bluetooth connection", connectException)
                        break
                    }

                    logger.debug("socket accepted")
                    macAddress = socket.remoteDevice.address
                    logger.debug("accepted $macAddress")
                    mBluetoothSocketStream = BluetoothSocketStream(socket)
                    mBluetoothSocketStream?.start()
                } catch (connectException: IOException) {
                    logger.error("Failed to start a Bluetooth connection as a server", connectException)
                    socket?.close()
                }
            }
        }
    }

    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return try {
            device.createInsecureRfcommSocketToServiceRecord(BT_APP_UUID)
        } catch (e: Exception) {
            logger.error("Could not create Insecure RFComm Connection", e)
            device.createRfcommSocketToServiceRecord(BT_APP_UUID)
        }
    }

    fun hostServer() {
        bluetoothAdapter.cancelDiscovery()
        logger.debug("starting BT server...")
        BluetoothListenServer(BT_APP_UUID, bluetoothAdapter).start()
    }

    fun makeDiscoverable() {
        if (bluetoothAdapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            logger.warn("already discoverable")
            return
        }
        logger.warn("asking for discoverability")
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        activity.startActivity(discoverableIntent)
    }
}