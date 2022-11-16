package igrek.songbook.room.protocol

import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.settings.chordsnotation.ChordsNotation


class HelloMsg : GtrMsg()
class WhosThereMsg(val roomName: String, val withPassword: Boolean) : GtrMsg()
class LoginMsg(val username: String, val password: String) : GtrMsg()
class WelcomeMsg(val valid: Boolean) : GtrMsg()
class RoomUsersMsg(val usernames: List<String>) : GtrMsg()
class DisconnectMsg : GtrMsg()
class ChatMessageMsg(val author: String, val timestampMs: Long, val message: String) : GtrMsg()
class SelectSongMsg(val song: SongDto) : GtrMsg()
class RoomStatusMsg(val song: SongDto?) : GtrMsg()
class WhatsupMsg : GtrMsg()
class HeartbeatRequestMsg : GtrMsg()
class HeartbeatResponseMsg : GtrMsg()

class SongDto(
    val songId: SongIdentifier,
    val categoryName: String,
    val title: String,
    val chordsNotation: ChordsNotation,
    val content: String
)

internal val msgSpecs = listOf(
    MsgSpec("HI", HelloMsg::class,
        partsParser = {
            HelloMsg()
        }),
    MsgSpec("WHOSTHERE", WhosThereMsg::class,
        partsFormatter = { listOf(it.roomName, it.withPassword.toGtrString()) },
        requiredParts = 2,
        partsParser = { parts ->
            WhosThereMsg(parts[0], parts[1] == "1")
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
    MsgSpec("WHATSUP", WhatsupMsg::class,
        partsParser = {
            WhatsupMsg()
        }),
    MsgSpec("ROOMSTATUS", RoomStatusMsg::class,
        partsFormatter = {
            if (it.song == null)
                return@MsgSpec listOf("0", "0", "", "", "0", "")
            listOf(
                it.song.songId.namespace.id.toString(), it.song.songId.songId.toString(),
                it.song.categoryName, it.song.title,
                it.song.chordsNotation.id.toString(), it.song.content
            )
        },
        requiredParts = 6,
        partsParser = { parts ->
            val song = when (val songId = parts[1].toLong()) {
                0L -> null
                else -> {
                    val namespace = SongNamespace.parseById(parts[0].toLong())
                    val songIdentifier = SongIdentifier(songId = songId, namespace = namespace)
                    val chordsNotation = ChordsNotation.parseById(parts[4].toLong())
                        ?: ChordsNotation.default
                    SongDto(
                        songId = songIdentifier,
                        categoryName = parts[2], title = parts[3],
                        chordsNotation = chordsNotation, content = parts[5],
                    )
                }
            }
            RoomStatusMsg(song)
        }),
    MsgSpec("CHAT", ChatMessageMsg::class,
        partsFormatter = { listOf(it.author, it.timestampMs.toString(), it.message) },
        requiredParts = 3,
        partsParser = { parts ->
            ChatMessageMsg(parts[0], parts[1].toLong(), parts[2])
        }),
    MsgSpec("SELECT_SONG", SelectSongMsg::class,
        partsFormatter = {
            listOf(
                it.song.songId.namespace.id.toString(), it.song.songId.songId.toString(),
                it.song.categoryName, it.song.title,
                it.song.chordsNotation.id.toString(), it.song.content
            )
        },
        requiredParts = 6,
        partsParser = { parts ->
            val songId = parts[1].toLong()
            val namespace = SongNamespace.parseById(parts[0].toLong())
            val songIdentifier = SongIdentifier(songId = songId, namespace = namespace)
            val chordsNotation = ChordsNotation.parseById(parts[4].toLong())
                ?: ChordsNotation.default
            val song = SongDto(
                songId = songIdentifier,
                categoryName = parts[2], title = parts[3],
                chordsNotation = chordsNotation, content = parts[5],
            )
            SelectSongMsg(song)
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