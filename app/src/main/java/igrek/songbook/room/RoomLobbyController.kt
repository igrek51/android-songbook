package igrek.songbook.room

import android.bluetooth.BluetoothSocket
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.room.protocol.DisconnectMsg
import igrek.songbook.room.protocol.GtrMsg
import igrek.songbook.room.protocol.GtrParser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomLobbyController(
        private val bluetoothService: BluetoothService,
) {
    var peerStatus: PeerStatus = PeerStatus.Disconnected
        private set
    var clients: MutableList<PeerClient> = mutableListOf()
        private set

    private var masterStream: PeerStream? = null
    private val masterMessagesChannel = Channel<String>(Channel.UNLIMITED)
    private var slaveStreams: MutableList<PeerStream> = mutableListOf()
    private var newSlaveListener: NewSlaveListener? = null
    private val newSlaveChannel = Channel<PeerStream>(Channel.UNLIMITED)
    private val writeMutex = Mutex()

    var onMasterMsgReceived: suspend (gtrMsg: GtrMsg, slaveStream: PeerStream?) -> Unit = { _, _ -> }
    var onClientMsgReceived: suspend (gtrMsg: GtrMsg) -> Unit = {}
    var onJoinRoomKnocked: () -> Unit = {}
    var onClientsChange: (List<PeerClient>) -> Unit = {}
    var onNewClientJoined: (String) -> Unit = {}
    var onDroppedFromMaster: (error: Throwable?) -> Unit = {}

    init {
        // new slave connections
        GlobalScope.launch(Dispatchers.IO) {
            for (clientStream: PeerStream in newSlaveChannel) {
                LoggerFactory.logger.debug("new slave connected")
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

    fun reset() {
        peerStatus = PeerStatus.Disconnected
    }

    fun isActive(): Boolean = peerStatus != PeerStatus.Disconnected

    private fun watchNewSlave(clientStream: PeerStream) {
        slaveStreams.add(clientStream)
        // messages from slave client
        GlobalScope.launch(Dispatchers.IO) {
            for (message in clientStream.receivedMsgCh) {
                onMasterReceived(message, clientStream)
            }
            LoggerFactory.logger.debug("slave channel closed")
        }
        GlobalScope.launch(Dispatchers.IO) {
            val error = clientStream.disconnectedCh.receive()
            onSlaveDisconnect(clientStream, error)
            LoggerFactory.logger.debug("slave disconnected: ${clientStream.remoteName()}")
        }
    }

    suspend fun close(broadcast: Boolean = true) {
        if (broadcast) {
            try {
                when (peerStatus) {
                    PeerStatus.Master -> {
                        sendToSlaves(DisconnectMsg()).join()
                    }
                    PeerStatus.Slave -> {
                        sendToMaster(DisconnectMsg()).join()
                    }
                    else -> {
                    }
                }
            } catch (t: Throwable) {
                LoggerFactory.logger.error("sending close message", t)
            }
        }

        peerStatus = PeerStatus.Disconnected
        masterStream?.close()
        slaveStreams.forEach { it.close() }
        slaveStreams.clear()
        newSlaveListener?.close()
        clients = mutableListOf()
    }

    fun hostRoomAsync(username: String): Deferred<Result<Unit>> {
        return try {
            makeDiscoverable()
            this.clients = mutableListOf(PeerClient(username, null, PeerStatus.Master))
            newSlaveListener?.close()
            val bluetoothAdapter = bluetoothService.bluetoothAdapter
                    ?: throw RuntimeException("No Bluetooth adapter")
            newSlaveListener = NewSlaveListener(bluetoothAdapter, newSlaveChannel)
            peerStatus = PeerStatus.Master
            newSlaveListener!!.isInitializedAsync()
        } catch (t: Throwable) {
            GlobalScope.async { Result.failure(t) }
        }
    }

    fun makeDiscoverable() {
        bluetoothService.ensureBluetoothEnabled()
        bluetoothService.cancelDiscovery()
        bluetoothService.makeDiscoverable()
    }

    fun joinRoomKnockAsync(room: Room): Deferred<Result<Unit>> {
        newSlaveListener?.close()
        return GlobalScope.async {
            val result = bluetoothService.connectToRoomSocketAsync(room).await()
            return@async result.onSuccess { btSocket: BluetoothSocket ->
                LoggerFactory.logger.debug("joined to room socket")
                masterStream = PeerStream(btSocket, masterMessagesChannel)
                peerStatus = PeerStatus.Slave
                GlobalScope.launch {
                    val error = masterStream?.disconnectedCh?.receive()
                    onMasterDisconnect(error)
                }

                onJoinRoomKnocked()
            }.map { Unit }
        }
    }

    fun sendToMaster(msg: GtrMsg): Job {
        return GlobalScope.launch {
            when (peerStatus) {
                PeerStatus.Master -> {
                    onMasterReceived(msg.toString(), null)
                }
                PeerStatus.Slave -> {
                    val strMsg = msg.toString()
                    LoggerFactory.logger.debug("sending to master: $strMsg")
                    try {
                        masterStream?.write(strMsg)
                    } catch (e: Throwable) {
                        LoggerFactory.logger.error("failed to write to host", e)
                        onMasterDisconnect(e, broadcast = false)
                    }
                }
                else -> {
                }
            }
        }
    }

    fun sendToClients(msg: GtrMsg): Job {
        return GlobalScope.launch {
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
    }

    fun sendToSlaves(msg: GtrMsg): Job {
        return GlobalScope.launch {
            when (peerStatus) {
                PeerStatus.Master -> {
                    val strMsg = msg.toString()
                    val activeSlaveStreams = clients.filter { it.status == PeerStatus.Slave && it.stream != null }
                            .map { it.stream!! }
                    LoggerFactory.logger.debug("sending to ${activeSlaveStreams.size} slaves: $strMsg")
                    activeSlaveStreams.forEach { stream ->
                        try {
                            stream.write(strMsg)
                        } catch (e: Throwable) {
                            LoggerFactory.logger.error("sending to disconnected peer", e)
                            stream.close()
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    fun sendToSlave(stream: PeerStream, msg: GtrMsg): Job {
        return GlobalScope.launch {
            when (peerStatus) {
                PeerStatus.Master -> {
                    val strMsg = msg.toString()
                    LoggerFactory.logger.debug("sending to ${stream.remoteName()} (${stream.remoteAddress()}) slave: $strMsg")
                    try {
                        stream.write(strMsg)
                    } catch (e: Throwable) {
                        LoggerFactory.logger.error("sending to disconnected peer", e)
                        stream.close()
                    }
                }
                else -> {
                }
            }
        }
    }

    suspend fun addNewSlave(newUsername: String, clientStream: PeerStream?) {
        writeMutex.withLock {
            clients.add(PeerClient(newUsername, clientStream, PeerStatus.Slave))
        }
        onClientsChange(clients)
        onNewClientJoined(newUsername)
    }

    // receive as Master
    private suspend fun onMasterReceived(strMsg: String, slaveStream: PeerStream?) {
        try {
            val clientName = if (slaveStream == null) "itself" else "slave ${slaveStream.remoteName()}"
            LoggerFactory.logger.debug("received message from $clientName: $strMsg")
            val gtrMsg: GtrMsg = GtrParser().parse(strMsg)
            onMasterMsgReceived(gtrMsg, slaveStream)
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }

    // receive as Slave or Master client
    private suspend fun onClientReceived(strMsg: String) {
        try {
            LoggerFactory.logger.debug("received message from master: $strMsg")
            val gtrMsg: GtrMsg = GtrParser().parse(strMsg)
            onClientMsgReceived(gtrMsg)
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }

    suspend fun onSlaveDisconnect(slaveStream: PeerStream, error: Throwable?) {
        LoggerFactory.logger.debug("slave dropped: ${error?.message}")
        slaveStream.close()
        writeMutex.withLock {
            slaveStreams = slaveStreams.filterNot { it == slaveStream }.toMutableList()
            clients = clients.filterNot { it.stream == slaveStream }.toMutableList()
        }
        onClientsChange(clients)
    }

    suspend fun onMasterDisconnect(error: Throwable?, broadcast: Boolean = true) {
        if (peerStatus != PeerStatus.Disconnected) {
            LoggerFactory.logger.debug("dropped from master")
            onDroppedFromMaster(error)
            close(broadcast)
        }
    }

    suspend fun setClients(clients: MutableList<PeerClient>) {
        writeMutex.withLock {
            this.clients = clients
        }
    }
}