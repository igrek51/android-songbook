package igrek.songbook.room

import android.bluetooth.BluetoothSocket
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.room.protocol.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class RoomLobby(
        bluetoothService: LazyInject<BluetoothService> = appFactory.bluetoothService,
) {
    private val bluetoothService by LazyExtractor(bluetoothService)

    private var peerStatus: PeerStatus = PeerStatus.Disconnected
    private var roomPassword: String = ""
    private var username: String = ""
    private var clients: MutableList<PeerClient> = mutableListOf()
    val usernames: List<String> get() = this.clients.map { it.username }

    private var masterStream: PeerStream? = null
    private val masterMessagesChannel = Channel<String>(Channel.UNLIMITED)
    private var slaveStreams: MutableList<PeerStream> = mutableListOf()
    private var newSlaveListener: NewSlaveListener? = null
    private val newSlaveChannel = Channel<PeerStream>(Channel.UNLIMITED)
    private val writeMutex = Mutex()

    var newChatMessageCallback: (ChatMessage) -> Unit = {}
    var updateUsersCallback: (List<String>) -> Unit = {}
    var onDisconnectCallback: () -> Unit = {}

    init {
        // new slave connections
        GlobalScope.launch(Dispatchers.IO) {
            for (clientStream: PeerStream in newSlaveChannel) {
                logger.debug("new slave connected")
                watchNewSlave(clientStream)
            }
        }

        // messages from master to client
        GlobalScope.launch(Dispatchers.IO) {
            for (message in masterMessagesChannel) {
                launch(Dispatchers.IO) {
                    onClientReceived(message)
                }
            }
        }
    }

    private fun watchNewSlave(clientStream: PeerStream) {
        slaveStreams.add(clientStream)
        // messages from slave client
        GlobalScope.launch(Dispatchers.IO) {
            for (message in clientStream.receivedMsgCh) {
                onMasterReceived(message, clientStream)
            }
            logger.debug("slave channel closed")
        }
        GlobalScope.launch(Dispatchers.IO) {
            clientStream.disconnectedCh.receive()
            onSlaveDisconnect(clientStream)
            logger.debug("slave disconnected: ${clientStream.remoteName()}")
        }
    }

    suspend fun close() {
        try {
            when (peerStatus) {
                PeerStatus.Master -> {
                    sendToSlaves(DisconnectMsg)
                }
                PeerStatus.Slave -> {
                    sendToMaster(DisconnectMsg)
                }
            }
        } catch (t: Throwable) {
            logger.error("sending close message", t)
        }

        peerStatus = PeerStatus.Disconnected
        masterStream?.close()
        slaveStreams.forEach { it.close() }
        slaveStreams.clear()
        newSlaveListener?.close()

        roomPassword = ""
        clients = mutableListOf()
    }

    fun hostRoom(username: String, password: String): Deferred<Result<Unit>> {
        makeDiscoverable()
        this.roomPassword = password
        this.username = username
        this.clients = mutableListOf(PeerClient(username, null, PeerStatus.Master))
        newSlaveListener = NewSlaveListener(bluetoothService.bluetoothAdapter, newSlaveChannel)
        peerStatus = PeerStatus.Master
        return newSlaveListener!!.isInitialized()
    }

    fun makeDiscoverable() {
        bluetoothService.ensureBluetoothEnabled()
        bluetoothService.cancelDiscovery()
        bluetoothService.makeDiscoverable()
    }

    fun joinRoom(username: String, room: Room): Deferred<Result<Unit>> {
        this.username = username
        return GlobalScope.async {
            val result = bluetoothService.connectToRoomSocketAsync(room).await()
            return@async result.onSuccess { btSocket: BluetoothSocket ->
                logger.debug("joined to room socket")
                masterStream = PeerStream(btSocket, masterMessagesChannel)
                peerStatus = PeerStatus.Slave
                GlobalScope.launch {
                    masterStream?.disconnectedCh?.receive()
                    onMasterDisconnect()
                }
                sendToMaster(HelloMsg(username))
            }.map { Unit }
        }
    }

    fun sendChatMessage(message: String) {
        logger.debug("Sending chat message")
        GlobalScope.launch {
            sendToMaster(ChatMessageMsg(username, 0, message))
        }
    }

    suspend fun sendToMaster(msg: GtrMsg) {
        when (peerStatus) {
            PeerStatus.Master -> {
                onMasterReceived(msg.toString(), null)
            }
            PeerStatus.Slave -> {
                val strMsg = msg.toString()
                logger.debug("sending to master: $strMsg")
                masterStream?.write(strMsg)
            }
            else -> {
            }
        }
    }

    suspend fun sendToClients(msg: GtrMsg) {
        // From Master
        when (peerStatus) {
            PeerStatus.Master -> {
                sendToSlaves(msg)
                onClientReceived(msg.toString())
            }
            else -> {
            }
        }
    }

    fun sendToSlaves(msg: GtrMsg) {
        when (peerStatus) {
            PeerStatus.Master -> {
                val strMsg = msg.toString()
                val activeSlaveStreams = clients.filter { it.status == PeerStatus.Slave && it.stream != null }
                        .map { it.stream!! }
                logger.debug("sending to ${activeSlaveStreams.size} slaves: $strMsg")
                activeSlaveStreams.forEach { stream ->
                    try {
                        stream.write(strMsg)
                    } catch (e: Exception) {
                        logger.error("sending to disconnected peer", e)
                        stream.close()
                    }
                }
            }
            else -> {
            }
        }
    }

    private suspend fun addNewSlave(newUsername: String, clientStream: PeerStream?) {
        writeMutex.withLock {
            clients.add(PeerClient(newUsername, clientStream, PeerStatus.Slave))
        }
        sendToSlaves(RoomUsersMsg(clients.map { it.username }))
        GlobalScope.launch(Dispatchers.Main) {
            updateUsersCallback(usernames)
        }
    }

    // receive as Master
    suspend fun onMasterReceived(strMsg: String, slaveStream: PeerStream?) {
        val clientName = if (slaveStream == null) "itself" else "slave ${slaveStream.remoteName()}"
        logger.debug("received message from $clientName: $strMsg")
        val gtrMsg: GtrMsg = GtrParser().parse(strMsg)
        when (gtrMsg) {
            is ChatMessageMsg -> sendToClients(ChatMessageMsg(gtrMsg.author, Date().time, gtrMsg.message))
            is HelloMsg -> addNewSlave(gtrMsg.username, slaveStream)
            is DisconnectMsg -> slaveStream?.let { onSlaveDisconnect(slaveStream) }
        }
    }

    // receive as Slave or Master client
    suspend fun onClientReceived(strMsg: String) {
        logger.debug("received message from master: $strMsg")
        val gtrMsg: GtrMsg = GtrParser().parse(strMsg)
        when (gtrMsg) {
            is ChatMessageMsg -> {
                val cal = Calendar.getInstance()
                cal.timeInMillis = gtrMsg.timestampMs // in milliseconds
                val chatMessage = ChatMessage(gtrMsg.author, gtrMsg.message, cal.time)
                GlobalScope.launch(Dispatchers.Main) {
                    newChatMessageCallback(chatMessage)
                }
            }
            is RoomUsersMsg -> {
                val clients = gtrMsg.usernames.mapIndexed { index, it ->
                    PeerClient(it, null, status = if (index == 0) PeerStatus.Master else PeerStatus.Slave)
                }.toMutableList()
                writeMutex.withLock {
                    this.clients = clients
                }
                GlobalScope.launch(Dispatchers.Main) {
                    updateUsersCallback(usernames)
                }
            }
            is DisconnectMsg -> onMasterDisconnect()
        }
    }

    private suspend fun onSlaveDisconnect(slaveStream: PeerStream) {
        logger.debug("slave dropped")
        writeMutex.withLock {
            slaveStreams = slaveStreams.filterNot { it == slaveStream }.toMutableList()
            clients = clients.filterNot { it.stream == slaveStream }.toMutableList()
        }
        sendToSlaves(RoomUsersMsg(clients.map { it.username }))
        GlobalScope.launch(Dispatchers.Main) {
            updateUsersCallback(usernames)
        }
    }

    private fun onMasterDisconnect() {
        logger.debug("master dropped")
        onDisconnectCallback()
    }

}