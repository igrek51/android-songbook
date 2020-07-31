package igrek.songbook.room.protocol

import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.room.protocol.GtrProtocol.Companion.VERSION
import java.util.*

sealed class GtrMsg(val code: String) {
    override fun toString(): String = GtrFormatter().format(this)
}

class HelloMsg(val username: String) : GtrMsg("HELLO")
object DisconnectMsg : GtrMsg("BYE")
class RoomUsersMsg(val usernames: List<String>) : GtrMsg("USERS")
class ChatMessageMsg(val author: String, val timestampMs: Long, val message: String) : GtrMsg("CHAT")
class SelectSongMsg(val songId: SongIdentifier) : GtrMsg("SELECT_SONG")
object HeartbeatRequestMsg : GtrMsg("RUOK")
object HeartbeatResponseMsg : GtrMsg("IMOK")

class GtrProtocol {
    companion object {
        const val VERSION = 1
        val BT_APP_UUID: UUID = UUID.fromString("eb5d5f8c-8a33-465d-5151-3c2e36cb5490")
    }
}

class GtrParseError(message: String) : RuntimeException(message)

class GtrParser {
    private fun parseCommand(msg: String): GtrMsg {
        val bound = msg.indexOf('|')
        if (bound == -1)
            throw GtrParseError("code delimiter not found")
        val code = msg.take(bound)
        val rest = msg.drop(bound + 1)

        return when (code) {
            "CHAT" -> parseChatMessage(rest)
            "HELLO" -> HelloMsg(rest)
            "USERS" -> parseRoomUsers(rest)
            DisconnectMsg.code -> DisconnectMsg
            "SELECT_SONG" -> parseSelectSong(rest)
            HeartbeatRequestMsg.code -> HeartbeatRequestMsg
            HeartbeatResponseMsg.code -> HeartbeatResponseMsg
            else -> throw GtrParseError("unknown command")
        }
    }

    private fun parseSelectSong(msg: String): SelectSongMsg {
        val parts = msg.split('|', limit = 2)
        check(parts.size == 2) { "invalid section size" }
        val namespace = SongNamespace.parseById(parts[0].toLong())
        val songId = SongIdentifier(songId = parts[1].toLong(), namespace = namespace)
        return SelectSongMsg(songId)
    }

    fun parse(msg: String): GtrMsg {
        try {
            return when {
                msg.startsWith("GTR") -> parseGTR(msg.drop(3))
                else -> throw GtrParseError("GTR header not found")
            }
        } catch (e: GtrParseError) {
            throw GtrParseError("GTR parsing error ($msg): ${e.message}")
        } catch (e: IllegalStateException) {
            throw GtrParseError("GTR parsing error ($msg): ${e.message}")
        }
    }

    private fun parseGTR(msg: String): GtrMsg {
        val versionPrefix = "$VERSION|"
        return when {
            msg.startsWith(versionPrefix) -> parseCommand(msg.drop(versionPrefix.length))
            else -> throw GtrParseError("invalid GTR version")
        }
    }

    private fun parseChatMessage(msg: String): ChatMessageMsg {
        val parts = msg.split('|', limit = 3)
        check(parts.size == 3) { "invalid section size" }
        val timestamp = parts[1].toLong()
        return ChatMessageMsg(parts[0], timestamp, parts[2])
    }

    private fun parseRoomUsers(msg: String): RoomUsersMsg {
        val parts = msg.split('|')
        return RoomUsersMsg(parts)
    }
}

class GtrFormatter {
    fun format(msg: GtrMsg): String {
        return "GTR$VERSION|" + formatGTR(msg)
    }

    private fun formatGTR(msg: GtrMsg): String {
        val datapart = when (msg) {
            is ChatMessageMsg -> "${msg.author}|${msg.timestampMs}|${msg.message}"
            is RoomUsersMsg -> msg.usernames.joinToString(separator = "|")
            is HelloMsg -> msg.username
            is SelectSongMsg -> "${msg.songId.namespace.id}|${msg.songId.songId}"
            else -> ""
        }
        return msg.code + "|" + datapart
    }
}