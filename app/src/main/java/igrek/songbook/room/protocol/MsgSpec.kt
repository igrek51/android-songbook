package igrek.songbook.room.protocol

import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace


class HelloMsg(val username: String) : GtrMsg()
class RoomBouncerMsg(val roomName: String, val withPassword: Boolean) : GtrMsg()
class LoginMsg(val username: String, val password: String) : GtrMsg()
class WelcomeMsg(val valid: Boolean) : GtrMsg()
class RoomUsersMsg(val usernames: List<String>) : GtrMsg()
class DisconnectMsg : GtrMsg()
class ChatMessageMsg(val author: String, val timestampMs: Long, val message: String) : GtrMsg()
class SelectSongMsg(val songId: SongIdentifier) : GtrMsg()
class HeartbeatRequestMsg : GtrMsg()
class HeartbeatResponseMsg : GtrMsg()

internal val msgSpecs = listOf(
        MsgSpec("HI", HelloMsg::class,
                partsFormatter = { listOf(it.username) },
                requiredParts = 1,
                partsParser = { parts ->
                    HelloMsg(parts[0])
                }),
        MsgSpec("BOUNCER", RoomBouncerMsg::class,
                partsFormatter = { listOf(it.roomName, it.withPassword.toGtrString()) },
                requiredParts = 2,
                partsParser = { parts ->
                    RoomBouncerMsg(parts[0], parts[1] == "1")
                }),
        MsgSpec("LOGIN", LoginMsg::class,
                partsFormatter = { listOf(it.username, it.password) },
                requiredParts = 2,
                partsParser = { parts ->
                    LoginMsg(parts[0], parts[1])
                }),
        MsgSpec("WELCOME", WelcomeMsg::class,
                partsFormatter = { listOf(it.valid.toGtrString()) },
                requiredParts = 1,
                partsParser = { parts ->
                    WelcomeMsg(parts[0] == "1")
                }),
        MsgSpec("USERS", RoomUsersMsg::class,
                partsFormatter = { it.usernames },
                partsParser = { parts ->
                    RoomUsersMsg(parts)
                }),
        MsgSpec("BYE", DisconnectMsg::class,
                partsParser = {
                    DisconnectMsg()
                }),
        MsgSpec("CHAT", ChatMessageMsg::class,
                partsFormatter = { listOf(it.author, it.timestampMs.toString(), it.message) },
                requiredParts = 3,
                partsParser = { parts ->
                    ChatMessageMsg(parts[0], parts[1].toLong(), parts[2])
                }),
        MsgSpec("SELECT_SONG", SelectSongMsg::class,
                partsFormatter = { listOf(it.songId.namespace.id.toString(), it.songId.songId.toString()) },
                requiredParts = 2,
                partsParser = { parts ->
                    val namespace = SongNamespace.parseById(parts[0].toLong())
                    val songId = SongIdentifier(songId = parts[1].toLong(), namespace = namespace)
                    SelectSongMsg(songId)
                }),
        MsgSpec("RUOK", HeartbeatRequestMsg::class,
                partsParser = {
                    HeartbeatRequestMsg()
                }),
        MsgSpec("IMOK", HeartbeatResponseMsg::class,
                partsParser = {
                    HeartbeatResponseMsg()
                }),
)