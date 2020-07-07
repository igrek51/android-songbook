package igrek.songbook.room

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import igrek.songbook.info.logger.LoggerFactory.logger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.channels.sendBlocking
import java.io.IOException
import java.util.*

class HostRoomServer(
        private val bluetoothAdapter: BluetoothAdapter,
        private var hostRoomPassword: String = "",
) : Thread() {

    val initChannel = Channel<Result<Unit>>(1)
    val closeChannel = Channel<Result<Unit>>(1)
    private var serverSocket: BluetoothServerSocket? = null
    private var clientStreams: MutableList<RoomStream> = mutableListOf()

    companion object {
        val BT_APP_UUID: UUID = UUID.fromString("eb5d5f8c-8a33-465d-5151-3c2e36cb5490")
    }

    override fun run() {
        logger.debug("hosting BT room...")

        try {
            try {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Songbook", BT_APP_UUID)
                logger.debug("sending success to init channel")
                initChannel.sendBlocking(Result.success(Unit))
                logger.debug("initChannel: ${initChannel.isEmpty}")
            } catch (e: Throwable) {
                logger.error("creating Rfcomm server socket", e)
                initChannel.sendBlocking(Result.failure(e))
                throw e
            }

            while (true) {
                logger.debug("listening for Bluetooth connections")

                val socket: BluetoothSocket = serverSocket!!.accept() // This will block until there is a connection

                try {
                    val macAddress = socket.remoteDevice.address
                    logger.debug("socket accepted for $macAddress")

                    val clientStream = RoomStream(socket).apply {
                        start()
                    }
                    clientStreams.add(clientStream)

                } catch (connectException: IOException) {
                    logger.error("Failed to start a Bluetooth connection as a server", connectException)
                    socket.close()
                }
            }

        } catch (e: Throwable) {
            logger.error("Server socket error", e)
            closeChannel.sendBlocking(Result.failure(e))
        }
        closeChannel.sendBlocking(Result.success(Unit))

        closeChannel.close()
        initChannel.close()
    }

    fun isInitialized(): Deferred<Result<Unit>> {
        return GlobalScope.async {
            initChannel.receiveOrNull()
                    ?: Result.failure(ClosedReceiveChannelException("init channel closed"))
        }
    }

    fun hasPasswordSet(): Boolean = hostRoomPassword.isNotEmpty()

    fun close() {
        clientStreams.forEach { it.close() }
        serverSocket?.close()
    }
}