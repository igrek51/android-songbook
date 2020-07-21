package igrek.songbook.room

import android.bluetooth.BluetoothSocket
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*

class RoomLobby(
        bluetoothService: LazyInject<BluetoothService> = appFactory.bluetoothService,
) {
    private val bluetoothService by LazyExtractor(bluetoothService)

    private var roomPeer: RoomPeer = RoomPeer.Disconnected

    private var hostStream: RoomPeerStream? = null

    private var clientStreams: MutableList<RoomPeerStream> = mutableListOf()
    private var hostRoomServer: HostRoomServer? = null
    private var roomPassword: String = ""

    private val newClientChannel = Channel<RoomPeerStream>(Channel.UNLIMITED)
    private val hostMessagesChannel = Channel<String>(Channel.UNLIMITED)
    var newMessageListener: (ChatMessage) -> Unit = {}

    init {
        watch()
    }

    fun close() {
        roomPeer = RoomPeer.Disconnected
        hostStream?.close()
        clientStreams.forEach { it.close() }
        hostRoomServer?.takeIf { it.isAlive }?.close()
    }

    fun hostRoom(password: String): Deferred<Result<Unit>> {
        bluetoothService.ensureBluetoothEnabled()
        bluetoothService.cancelDiscovery()
        bluetoothService.makeDiscoverable()
        roomPassword = password
        hostRoomServer = HostRoomServer(bluetoothService.bluetoothAdapter, newClientChannel).apply {
            start()
        }
        roomPeer = RoomPeer.Host
        return hostRoomServer!!.isInitialized()
    }

    fun joinRoom(room: Room): Result<Unit> {
        return runBlocking {
            val result = bluetoothService.connectToRoomSocket(room).await()
            return@runBlocking result.onSuccess { btSocket: BluetoothSocket ->
                hostStream = RoomPeerStream(btSocket, hostMessagesChannel).apply {
                    start()
                }
                roomPeer = RoomPeer.Client
            }.map { Unit }
        }
    }

    fun sendMessage(message: String) {
        logger.debug("Sending datagram")
        when (roomPeer) {
            RoomPeer.Host -> broadcastAll(message)
            RoomPeer.Client -> hostStream?.write(message)
        }
    }

    fun onReceivedFromClient(message: String, client: String) {
        broadcastAll(message)
    }

    fun broadcastAll(message: String) {
        clientStreams.forEach { clientPeerStream ->
            // TODO disconnect in case of error
            clientPeerStream.write(message)
        }
        onReceivedFromHost(message)
    }

    fun onReceivedFromHost(message: String) {
        val chatMessage = ChatMessage("igrek51", message, Date())
        GlobalScope.launch(Dispatchers.Main) {
            newMessageListener(chatMessage)
        }
    }

    fun hasPasswordSet(): Boolean = roomPassword.isNotEmpty()

    fun watch() {
        // TODO cancel previous listeners
        // messages from host
        GlobalScope.launch {
            for (message in hostMessagesChannel) {
                logger.debug("received message from host")
                onReceivedFromHost(message)
            }
            logger.debug("channel closed")
        }

        // new connections
        GlobalScope.launch {
            for (clientStream in newClientChannel) {
                logger.debug("received client stream")
                clientStreams.add(clientStream)

                GlobalScope.launch {
                    for (message in clientStream.receivedMsgCh) {
                        logger.debug("received message from client")
                        onReceivedFromClient(message, "CLIENTNAME")
                    }
                    logger.debug("channel closed")
                }
            }
            logger.debug("channel closed")
        }
    }
}