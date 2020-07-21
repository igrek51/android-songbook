package igrek.songbook.room

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.room.protocol.GtrProtocol.Companion.BT_APP_UUID
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch
import java.io.IOException

class NewClientListener(
        private val bluetoothAdapter: BluetoothAdapter,
        private val newClientChannel: SendChannel<PeerStream>,
) : Thread() {

    val initChannel = Channel<Result<Unit>>(1)
    val closeChannel = Channel<Result<Unit>>(1)
    private var serverSocket: BluetoothServerSocket? = null

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

                    val receivedClientMsgCh = Channel<String>(Channel.UNLIMITED)
                    val clientStream = PeerStream(socket, receivedClientMsgCh).apply {
                        start()
                    }
                    GlobalScope.launch {
                        newClientChannel.send(clientStream)
                    }

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

    fun close() {
        serverSocket?.close()
    }
}