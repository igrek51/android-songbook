package igrek.songbook.room

import android.bluetooth.BluetoothSocket
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.room.protocol.ChatMessageMsg
import igrek.songbook.room.protocol.GtrMsg
import igrek.songbook.room.protocol.GtrParser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*

class RoomLobby(
        bluetoothService: LazyInject<BluetoothService> = appFactory.bluetoothService,
) {
    private val bluetoothService by LazyExtractor(bluetoothService)

    private var roomStatus: RoomStatus = RoomStatus.Disconnected
    private var username: String = ""
    private var roomPassword: String = ""

    private var hostStream: PeerStream? = null
    private val hostMessagesChannel = Channel<String>(Channel.UNLIMITED)

    private var clientStreams: MutableList<PeerStream> = mutableListOf()
    private var newClientListener: NewClientListener? = null
    private val newClientChannel = Channel<PeerStream>(Channel.UNLIMITED)

    var newMessageListener: (ChatMessage) -> Unit = {}

    init {
        // new connections
        GlobalScope.launch {
            for (clientStream: PeerStream in newClientChannel) {
                logger.debug("new client connected")
                clientStreams.add(clientStream)
                // messages from client
                launch {
                    for (message in clientStream.receivedMsgCh) {
                        logger.debug("received message from client")
                        onReceivedFromSlave(message, clientStream)
                    }
                    logger.debug("client channel closed")
                }
            }
        }

        // messages from host
        GlobalScope.launch {
            for (message in hostMessagesChannel) {
                logger.debug("received message from host")
                onReceivedFromMaster(message)
            }
        }
    }

    fun close() {
        roomStatus = RoomStatus.Disconnected
        hostStream?.close()
        clientStreams.forEach { it.close() }
        newClientListener?.takeIf { it.isAlive }?.interrupt()
        newClientListener?.takeIf { it.isAlive }?.close()
    }

    fun hostRoom(password: String): Deferred<Result<Unit>> {
        bluetoothService.ensureBluetoothEnabled()
        bluetoothService.cancelDiscovery()
        bluetoothService.makeDiscoverable()
        roomPassword = password
        newClientListener = NewClientListener(bluetoothService.bluetoothAdapter, newClientChannel).apply {
            start()
        }
        roomStatus = RoomStatus.Host
        return newClientListener!!.isInitialized()
    }

    fun joinRoom(room: Room): Result<Unit> {
        return runBlocking {
            val result = bluetoothService.connectToRoomSocketAsync(room).await()
            return@runBlocking result.onSuccess { btSocket: BluetoothSocket ->
                hostStream = PeerStream(btSocket, hostMessagesChannel).apply {
                    start()
                }
                roomStatus = RoomStatus.Client
            }.map { Unit }
        }
    }

    fun sendChatMessage(message: String) {
        logger.debug("Sending chat message")
        val author = bluetoothService.deviceName()
        sendToAll(ChatMessageMsg(message, author))
    }

    fun sendToAll(msg: GtrMsg) {
        when (roomStatus) {
            RoomStatus.Host -> {
                sendToSlaves(msg)
                onReceivedFromMaster(msg.toString())
            }
            RoomStatus.Client -> sendToMaster(msg)
        }
    }

    fun sendToMaster(msg: GtrMsg) {
        val strMsg = msg.toString()
        hostStream?.write(strMsg)
    }

    fun sendToSlaves(msg: GtrMsg) {
        val strMsg = msg.toString()
        clientStreams.forEach { clientPeerStream ->
            // TODO disconnect in case of error
            clientPeerStream.write(strMsg)
        }
    }

    fun onReceivedFromSlave(strMsg: String, clientStream: PeerStream) {
        val gtrMsg: GtrMsg = GtrParser().parse(strMsg)
        when (gtrMsg) {
            is ChatMessageMsg -> sendToAll(gtrMsg)
        }
    }

    fun onReceivedFromMaster(strMsg: String) {
        val msg: GtrMsg = GtrParser().parse(strMsg)
        when (msg) {
            is ChatMessageMsg -> {
                val chatMessage = ChatMessage(msg.author, msg.message, Date())
                GlobalScope.launch(Dispatchers.Main) {
                    newMessageListener(chatMessage)
                }
            }
        }
    }

    fun hasPasswordSet(): Boolean = roomPassword.isNotEmpty()
}