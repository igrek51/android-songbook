package igrek.songbook.room

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import igrek.songbook.room.HostRoomServer.Companion.BT_APP_UUID
import igrek.songbook.util.waitUntil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


class BluetoothService(
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
) {
    private val activity by LazyExtractor(appCompatActivity)

    companion object {
        private const val REQUEST_ENABLE_BT = 20
    }

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mRoomStream: RoomStream? = null

    private val discoveredRoomDevices: ConcurrentHashMap<String, BluetoothDevice> = ConcurrentHashMap()
    private var discoveryJobs: MutableList<Job> = mutableListOf()
    private var roomsChannel: Channel<Room> = Channel()

    private var hostRoomServer: HostRoomServer? = null

    fun deviceName(): String {
        return bluetoothAdapter.name.orEmpty()
    }

    fun scanRooms(): Deferred<Result<Channel<Room>>> {
        return GlobalScope.async {
            return@async runCatching {
                ensureBluetoothEnabled()

                roomsChannel.close()
                roomsChannel = Channel(8)
                discoveredRoomDevices.clear()
                discoveryJobs.clear()

                launch {
                    startDiscovery()
                    // scanPairedDevices()
                }

                return@runCatching roomsChannel
            }
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
                    logger.debug("BT Discovery started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    logger.debug("BT Discovery finished")
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
            roomsChannel.close()
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
                detectDeviceSocket(device.address)
            } catch (e: Throwable) {
                logger.warn("device ${device.address} room is unavailable: ${e.message}")
                return@launch
            }

            discoveredRoomDevices[device.address] = device
            try {
                roomsChannel.send(Room(
                        name = device.name.orEmpty(),
                        hostAddress = device.address,
                ))
            } catch (e: ClosedSendChannelException) {
            }
        }
        discoveryJobs.add(job)
    }

    private fun detectDeviceSocket(address: String) {
        val device = bluetoothAdapter.getRemoteDevice(address)
        logger.debug("Detecting socket on ${device.name} ($address)")

        val socket: BluetoothSocket = try {
            device.createInsecureRfcommSocketToServiceRecord(HostRoomServer.BT_APP_UUID)
        } catch (e: Exception) {
            logger.warn("Could not create Insecure RFComm Connection", e)
            device.createRfcommSocketToServiceRecord(HostRoomServer.BT_APP_UUID)
        }

        socket.connect()

        logger.debug("Room found on ${device.name} ($address)")

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

    fun hostRoom(password: String): Deferred<Result<Unit>> {
        ensureBluetoothEnabled()
        bluetoothAdapter.cancelDiscovery()
        makeDiscoverable()
        hostRoomServer = HostRoomServer(bluetoothAdapter, password).apply {
            start()
        }
        return hostRoomServer!!.isInitialized()
    }

    fun connectToRoom(room: Room) {
        bluetoothAdapter.cancelDiscovery()

        GlobalScope.launch(Dispatchers.IO) {
            val mBTSocket: BluetoothSocket

            val device = bluetoothAdapter.getRemoteDevice(room.hostAddress)
            logger.debug("Connecting to ${room.hostAddress} - ${device.name}")
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
                    logger.debug("Socket closing failed")
                }
            }
            mRoomStream = RoomStream(mBTSocket)
            mRoomStream?.start()

            mRoomStream?.write("Hello dupa!")
        }
    }

    fun send() {
        logger.debug("Sending datagram")
        mRoomStream?.write("Hello dupa!")
    }

    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return try {
            device.createInsecureRfcommSocketToServiceRecord(BT_APP_UUID)
        } catch (e: Exception) {
            logger.error("Could not create Insecure RFComm Connection", e)
            device.createRfcommSocketToServiceRecord(BT_APP_UUID)
        }
    }

    fun makeDiscoverable() {
        if (bluetoothAdapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            logger.debug("already discoverable")
            return
        }
        logger.debug("asking for discoverability")
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        activity.startActivity(discoverableIntent)
    }

}