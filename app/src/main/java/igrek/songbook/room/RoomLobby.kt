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

    var newChatMessageCallback: (ChatMessage) -> Unit = {}
    var updateMembersCallback: (List<PeerClient>) -> Unit = {}
    var onDroppedCallback: () -> Unit
        get() = controller.onDroppedFromMaster
        set(value) {
            controller.onDroppedFromMaster = value
        }
    var onSelectedSongChange: (songId: SongIdentifier) -> Unit = {}

    init {
        controller.onClientsChange = ::onClientsChange
        controller.onMasterMsgReceived = ::onMasterMsgReceived
        controller.onClientMsgReceived = ::onClientMsgReceived
        controller.onJoinRoomKnocked = ::onJoinRoomKnocked
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

    private fun onJoinRoomKnocked() {
        controller.sendToMaster(HelloMsg())
    }

    private fun onClientsChange(clients: List<PeerClient>) {
        controller.sendToSlaves(RoomUsersMsg(clients.map { it.username }))
        GlobalScope.launch(Dispatchers.Main) {
            updateMembersCallback(clients.toList())
        }
    }

    fun joinRoomKnockAsync(username: String, room: Room): Deferred<Result<Unit>> {
        this.username = username
        return controller.joinRoomKnockAsync(room)
    }

    fun sendChatMessage(message: String) {
        logger.debug("Sending chat message")
        controller.sendToMaster(ChatMessageMsg(username, 0, message))
    }

    // receive as Master
    private suspend fun onMasterMsgReceived(gtrMsg: GtrMsg, slaveStream: PeerStream?) {
        when (gtrMsg) {
            is ChatMessageMsg -> controller.sendToClients(ChatMessageMsg(gtrMsg.author, Date().time, gtrMsg.message))
            is LoginMsg -> controller.addNewSlave(gtrMsg.username, slaveStream)
            is DisconnectMsg -> slaveStream?.let { controller.onSlaveDisconnect(slaveStream) }
        }
    }

    // receive as Slave or Master client
    private suspend fun onClientMsgReceived(gtrMsg: GtrMsg) {
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
                controller.setClients(clients)
                GlobalScope.launch(Dispatchers.Main) {
                    updateMembersCallback(clients.toList())
                }
            }
            is DisconnectMsg -> controller.onMasterDisconnect()
            is SelectSongMsg -> GlobalScope.launch {
                currentSongId = gtrMsg.songId
                onSelectedSongChange(gtrMsg.songId)
            }
        }
    }

    fun reportSongSelected(songIdentifier: SongIdentifier) {
        if (peerStatus != PeerStatus.Master)
            return

        currentSongId = songIdentifier
        controller.sendToSlaves(SelectSongMsg(songIdentifier))
    }

}