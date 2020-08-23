package igrek.songbook.room

import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.room.protocol.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class RoomLobby(
        bluetoothService: LazyInject<BluetoothService> = appFactory.bluetoothService,
) {
    private val bluetoothService by LazyExtractor(bluetoothService)

    private val controller = RoomLobbyController(this.bluetoothService)

    val peerStatus: PeerStatus get() = controller.peerStatus
    val clients: MutableList<PeerClient> get() = controller.clients

    private var roomPassword: String = ""
    private var username: String = ""
    var currentSongId: SongIdentifier? = null
        private set

    var updateMembersCallback: (List<PeerClient>) -> Unit = {}
    var newChatMessageCallback: (ChatMessage) -> Unit = {}
    var onSelectedSongChange: (songId: SongIdentifier) -> Unit = {}
    var onRoomLobbyIntroduced: (roomName: String, withPassword: Boolean) -> Unit = { _, _ -> }
    var onRoomWelcomed: (valid: Boolean) -> Unit = {}
    var onDroppedCallback: () -> Unit
        get() = controller.onDroppedFromMaster
        set(value) {
            controller.onDroppedFromMaster = value
        }

    init {
        controller.onMasterMsgReceived = ::onMasterMsgReceived
        controller.onClientMsgReceived = ::onClientMsgReceived
        controller.onJoinRoomKnocked = ::onJoinRoomKnocked
        controller.onClientsChange = ::onClientsChange
    }

    fun isActive(): Boolean = controller.isActive()

    fun makeDiscoverable() = controller.makeDiscoverable()

    fun close() {
        controller.close()
        roomPassword = ""
        currentSongId = null
    }

    fun hostRoomAsync(username: String, password: String): Deferred<Result<Unit>> {
        this.roomPassword = password
        this.username = username
        return controller.hostRoomAsync(username)
    }

    fun joinRoomKnockAsync(username: String, room: Room): Deferred<Result<Unit>> {
        this.username = username
        return controller.joinRoomKnockAsync(room)
    }

    private fun onJoinRoomKnocked() {
        controller.sendToMaster(HelloMsg())
    }

    fun enterRoom(username: String, password: String) {
        controller.sendToMaster(LoginMsg(username, password))
    }

    private fun onClientsChange(clients: List<PeerClient>) {
        controller.sendToSlaves(RoomUsersMsg(clients.map { it.username }))
        GlobalScope.launch(Dispatchers.Main) {
            updateMembersCallback(clients.toList())
        }
    }

    fun sendChatMessage(message: String) {
        logger.debug("Sending chat message")
        controller.sendToMaster(ChatMessageMsg(username, 0, message))
    }

    fun reportSongSelected(songIdentifier: SongIdentifier) {
        if (peerStatus == PeerStatus.Master) {
            currentSongId = songIdentifier
            controller.sendToSlaves(SelectSongMsg(songIdentifier))
        }
    }

    // receive as Master
    private suspend fun onMasterMsgReceived(msg: GtrMsg, slaveStream: PeerStream?) {
        when (msg) {
            is HelloMsg -> {
                if (slaveStream != null)
                    controller.sendToSlave(slaveStream, WhosThereMsg(username, roomPassword.isNotEmpty()))
            }
            is LoginMsg -> {
                if (slaveStream != null)
                    verifyLoggingUser(msg.username, msg.password, slaveStream)
            }
            is ChatMessageMsg -> controller.sendToClients(ChatMessageMsg(msg.author, Date().time, msg.message))
            is DisconnectMsg -> slaveStream?.let { controller.onSlaveDisconnect(slaveStream) }
        }
    }

    private suspend fun verifyLoggingUser(username: String, givenPassword: String, slaveStream: PeerStream) {
        if (givenPassword != roomPassword) {
            logger.warn("User attempted to login to room with invalid password")
            controller.sendToSlave(slaveStream, WelcomeMsg(false))
            return
        }

        controller.sendToSlave(slaveStream, WelcomeMsg(true))
        controller.addNewSlave(username, slaveStream)
    }

    // receive as Slave or Master client
    private suspend fun onClientMsgReceived(msg: GtrMsg) {
        when (msg) {
            is WhosThereMsg -> {
                GlobalScope.launch(Dispatchers.Main) {
                    onRoomLobbyIntroduced(msg.roomName, msg.withPassword)
                }
            }
            is ChatMessageMsg -> {
                val chatMessage = ChatMessage(msg.author, msg.message, msg.timestampMs.timestampMsToDate())
                GlobalScope.launch(Dispatchers.Main) {
                    newChatMessageCallback(chatMessage)
                }
            }
            is WelcomeMsg -> {
                onRoomWelcomed(msg.valid)
            }
            is RoomUsersMsg -> {
                val clients = msg.usernames.mapIndexed { index, it ->
                    PeerClient(it, null, status = if (index == 0) PeerStatus.Master else PeerStatus.Slave)
                }.toMutableList()
                controller.setClients(clients)
                GlobalScope.launch(Dispatchers.Main) {
                    updateMembersCallback(clients.toList())
                }
            }
            is DisconnectMsg -> controller.onMasterDisconnect()
            is SelectSongMsg -> GlobalScope.launch {
                currentSongId = msg.songId
                onSelectedSongChange(msg.songId)
            }
        }
    }

}

private fun Long.timestampMsToDate(): Date {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this // in milliseconds
    return cal.time
}
