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

    private var masterStream: PeerStream? = null
    private val masterMessagesChannel = Channel<String>(Channel.UNLIMITED)

    private var slaveStreams: MutableList<PeerStream> = mutableListOf()
    private var newSlaveListener: NewClientListener? = null
    private val newSlaveChannel = Channel<PeerStream>(Channel.UNLIMITED)

    var newMessageListener: (ChatMessage) -> Unit = {}

    init {
        // new slave connections
        GlobalScope.launch {
            for (clientStream: PeerStream in newSlaveChannel) {
                logger.debug("new client connected")
                slaveStreams.add(clientStream)
                // messages from slave client
                launch {
                    for (message in clientStream.receivedMsgCh) {
                        logger.debug("received message from client")
                        onReceivedFromClient(message, clientStream)
                    }
                    logger.debug("client channel closed")
                }
            }
        }

        // messages from master
        GlobalScope.launch {
            for (message in masterMessagesChannel) {
                logger.debug("received message from host")
                onReceivedFromMaster(message)
            }
        }
    }

    fun close() {
        roomStatus = RoomStatus.Disconnected
        masterStream?.close()
        slaveStreams.forEach { it.close() }
        newSlaveListener?.takeIf { it.isAlive }?.interrupt()
        newSlaveListener?.takeIf { it.isAlive }?.close()
    }

    fun hostRoom(username: String, password: String): Deferred<Result<Unit>> {
        bluetoothService.ensureBluetoothEnabled()
        bluetoothService.cancelDiscovery()
        bluetoothService.makeDiscoverable()
        this.username = username
        this.roomPassword = password
        newSlaveListener = NewClientListener(bluetoothService.bluetoothAdapter, newSlaveChannel).apply {
            start()
        }
        roomStatus = RoomStatus.Master
        return newSlaveListener!!.isInitialized()
    }

    fun joinRoom(username: String, room: Room): Result<Unit> {
        this.username = username
        return runBlocking {
            val result = bluetoothService.connectToRoomSocketAsync(room).await()
            return@runBlocking result.onSuccess { btSocket: BluetoothSocket ->
                masterStream = PeerStream(btSocket, masterMessagesChannel).apply {
                    start()
                }
                roomStatus = RoomStatus.Slave
            }.map { Unit }
        }
    }

    fun sendChatMessage(message: String) {
        logger.debug("Sending chat message")
        sendToMaster(ChatMessageMsg(username, 0, message))
    }

    fun sendToMaster(msg: GtrMsg) {
        when (roomStatus) {
            RoomStatus.Master -> {
                onReceivedFromClient(msg.toString(), null)
            }
            RoomStatus.Slave -> {
                val strMsg = msg.toString()
                masterStream?.write(strMsg)
            }
            else -> {
            }
        }
    }

    fun sendToClients(msg: GtrMsg) {
        // From Master
        when (roomStatus) {
            RoomStatus.Master -> {
                sendToSlaves(msg)
                onReceivedFromMaster(msg.toString())
            }
            else -> {
            }
        }
    }

    private fun sendToSlaves(msg: GtrMsg) {
        when (roomStatus) {
            RoomStatus.Master -> {
                val strMsg = msg.toString()
                slaveStreams.forEach { clientPeerStream ->
                    // TODO disconnect in case of error
                    clientPeerStream.write(strMsg)
                }
            }
            else -> {
            }
        }
    }

    // receive as Master
    fun onReceivedFromClient(strMsg: String, clientStream: PeerStream?) {
        val gtrMsg: GtrMsg = GtrParser().parse(strMsg)
        when (gtrMsg) {
            is ChatMessageMsg -> sendToClients(ChatMessageMsg(gtrMsg.author, Date().time, gtrMsg.message))
        }
    }

    // receive as Slave or Master client
    fun onReceivedFromMaster(strMsg: String) {
        val gtrMsg: GtrMsg = GtrParser().parse(strMsg)
        when (gtrMsg) {
            is ChatMessageMsg -> {
                val cal = Calendar.getInstance()
                cal.timeInMillis = gtrMsg.timestampMs // in milliseconds
                val chatMessage = ChatMessage(gtrMsg.author, gtrMsg.message, cal.time)
                GlobalScope.launch(Dispatchers.Main) {
                    newMessageListener(chatMessage)
                }
            }
        }
    }

}