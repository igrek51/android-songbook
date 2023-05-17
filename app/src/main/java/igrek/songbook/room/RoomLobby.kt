package igrek.songbook.room

import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.room.protocol.ChatMessageMsg
import igrek.songbook.room.protocol.DisconnectMsg
import igrek.songbook.room.protocol.GtrMsg
import igrek.songbook.room.protocol.HelloMsg
import igrek.songbook.room.protocol.LoginMsg
import igrek.songbook.room.protocol.RoomStatusMsg
import igrek.songbook.room.protocol.RoomUsersMsg
import igrek.songbook.room.protocol.SelectSongMsg
import igrek.songbook.room.protocol.SongDto
import igrek.songbook.room.protocol.WelcomeMsg
import igrek.songbook.room.protocol.WhatsupMsg
import igrek.songbook.room.protocol.WhosThereMsg
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class RoomLobby(
    bluetoothService: LazyInject<BluetoothService> = appFactory.bluetoothService,
) {
    private val bluetoothService by LazyExtractor(bluetoothService)

    private val controller = RoomLobbyController(this.bluetoothService)

    val peerStatus: PeerStatus get() = controller.peerStatus
    val clients: MutableList<PeerClient> get() = controller.clients

    private var roomPassword: String = ""
    private var username: String = ""
    var currentSong: Song? = null
        private set
    var chatHistory: MutableList<ChatMessage> = mutableListOf()
        private set

    var updateMembersCallback: (List<PeerClient>) -> Unit = {}
    var newChatMessageCallback: (ChatMessage) -> Unit = {}
    var onOpenSong: (song: Song, updated: Boolean) -> Unit = { _, _ -> }
    var onRoomLobbyIntroduced: (roomName: String, withPassword: Boolean) -> Unit = { _, _ -> }
    var onRoomWrongPassword: () -> Unit = {}
    var onRoomWelcomedSuccessfully: () -> Unit = {}
    var onModelChanged: () -> Unit = {}
    var onDroppedCallback: (error: Throwable?) -> Unit
        get() = controller.onDroppedFromMaster
        set(value) {
            controller.onDroppedFromMaster = value
        }
    var onNewClientJoined: (username: String) -> Unit
        get() = controller.onNewClientJoined
        set(value) {
            controller.onNewClientJoined = value
        }

    init {
        controller.onMasterMsgReceived = ::onMasterMsgReceived
        controller.onClientMsgReceived = ::onClientMsgReceived
        controller.onJoinRoomKnocked = ::onJoinRoomKnocked
        controller.onClientsChange = ::onClientsChange
    }

    fun isActive(): Boolean = controller.isActive()

    fun makeDiscoverable() = controller.makeDiscoverable()

    suspend fun close(broadcast: Boolean = true) {
        controller.close(broadcast)
        reset()
    }

    private fun reset() {
        controller.reset()
        roomPassword = ""
        currentSong = null
        chatHistory = mutableListOf()
    }

    fun hostRoomAsync(username: String, password: String): Deferred<Result<Unit>> {
        reset()
        this.roomPassword = password
        this.username = username
        return controller.hostRoomAsync(username)
    }

    fun joinRoomKnockAsync(username: String, room: Room): Deferred<Result<Unit>> {
        reset()
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
        updateMembersCallback(clients.toList())
    }

    fun sendChatMessage(message: String) {
        logger.debug("Sending chat message")
        controller.sendToMaster(ChatMessageMsg(username, 0, message))
    }

    fun reportSongSelected(song: Song) {
        if (peerStatus == PeerStatus.Master) {
            currentSong = song
            controller.sendToSlaves(
                SelectSongMsg(
                    buildSongDto(song)
                )
            )
        }
    }

    // receive as Master
    private suspend fun onMasterMsgReceived(msg: GtrMsg, slaveStream: PeerStream?) {
        when (msg) {
            is HelloMsg -> {
                if (slaveStream != null)
                    controller.sendToSlave(
                        slaveStream,
                        WhosThereMsg(username, roomPassword.isNotEmpty())
                    )
            }
            is LoginMsg -> {
                if (slaveStream != null)
                    verifyLoggingUser(msg.username, msg.password, slaveStream)
            }
            is ChatMessageMsg -> controller.sendToClients(
                ChatMessageMsg(
                    msg.author,
                    Date().time,
                    msg.message
                )
            )
            is DisconnectMsg -> slaveStream?.let { controller.onSlaveDisconnect(slaveStream, null) }
            is WhatsupMsg -> {
                if (slaveStream != null)
                    sendRoomStatus(slaveStream)
            }
        }
    }

    // receive as Slave or Master client
    private suspend fun onClientMsgReceived(msg: GtrMsg) {
        when (msg) {
            is WhosThereMsg -> {
                GlobalScope.launch(Dispatchers.Main) {
                    onRoomLobbyIntroduced(msg.roomName, msg.withPassword)
                }
            }
            is WelcomeMsg -> {
                if (msg.valid) {
                    onRoomWelcomedSuccessfully()
                    controller.sendToMaster(WhatsupMsg())
                } else {
                    onRoomWrongPassword()
                    close(broadcast = false)
                }
            }
            is RoomUsersMsg -> {
                val clients = msg.usernames.mapIndexed { index, it ->
                    PeerClient(
                        it,
                        null,
                        status = if (index == 0) PeerStatus.Master else PeerStatus.Slave
                    )
                }.toMutableList()
                controller.setClients(clients)
                updateMembersCallback(clients.toList())
            }
            is ChatMessageMsg -> {
                val chatMessage =
                    ChatMessage(msg.author, msg.message, msg.timestampMs.timestampMsToDate())
                GlobalScope.launch(Dispatchers.Main) {
                    chatHistory.add(chatMessage)
                    newChatMessageCallback(chatMessage)
                }
            }
            is DisconnectMsg -> controller.onMasterDisconnect(null)
            is SelectSongMsg -> GlobalScope.launch {
                val newSong = buildEphemeralSong(msg.song)
                val updated = !areSongsTheSame(currentSong, newSong)
                currentSong = newSong
                logger.info("fetched song: ${msg.song.categoryName} - ${msg.song.title}")
                currentSong?.let {
                    onOpenSong(it, updated)
                }
            }
            is RoomStatusMsg -> {
                currentSong = buildEphemeralSong(msg.song)
                onModelChanged()
            }
        }
    }

    private suspend fun verifyLoggingUser(
        username: String,
        givenPassword: String,
        slaveStream: PeerStream
    ) {
        if (givenPassword != roomPassword) {
            logger.warn("User $username attempted to login to room with invalid password: $givenPassword")
            controller.sendToSlave(slaveStream, WelcomeMsg(false)).join()
            slaveStream.let { controller.onSlaveDisconnect(slaveStream, null) }
            return
        }

        controller.sendToSlave(slaveStream, WelcomeMsg(true))
        controller.addNewSlave(username, slaveStream)
    }

    private fun sendRoomStatus(slaveStream: PeerStream) {
        val songDto = currentSong?.let { buildSongDto(it) }
        controller.sendToSlave(slaveStream, RoomStatusMsg(songDto))
        controller.sendToSlave(slaveStream, RoomUsersMsg(clients.map { it.username }))
        chatHistory.forEach { chatMessage ->
            controller.sendToSlave(
                slaveStream,
                ChatMessageMsg(chatMessage.author, chatMessage.time.time, chatMessage.message)
            )
        }
    }

    private fun buildEphemeralSong(songDto: SongDto?): Song? {
        if (songDto == null)
            return null
        val now: Long = Date().time
        return Song(
            id = songDto.songId.songId,
            title = songDto.title,
            categories = mutableListOf(),
            content = songDto.content,
            versionNumber = 1,
            createTime = now,
            updateTime = now,
            status = SongStatus.PUBLISHED,
            customCategoryName = songDto.categoryName,
            chordsNotation = songDto.chordsNotation,
            namespace = SongNamespace.Ephemeral,
        )
    }

    private fun buildSongDto(song: Song): SongDto {
        return SongDto(
            songId = song.songIdentifier(),
            categoryName = song.displayCategories(),
            title = song.title,
            chordsNotation = song.chordsNotation,
            content = song.content.orEmpty(),
        )
    }

    private fun areSongsTheSame(oldSong: Song?, newSong: Song?): Boolean {
        if (oldSong == null || newSong == null)
            return false
        return oldSong.title == newSong.title
                && oldSong.content == newSong.content
                && oldSong.customCategoryName == newSong.customCategoryName
                && oldSong.chordsNotation == newSong.chordsNotation
    }

}

private fun Long.timestampMsToDate(): Date {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this // in milliseconds
    return cal.time
}
