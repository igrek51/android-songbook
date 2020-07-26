package igrek.songbook.room.protocol

import igrek.songbook.room.protocol.GtrProtocol.Companion.VERSION
import java.util.*

sealed class GtrMsg {
    override fun toString(): String = GtrFormatter().format(this)
}

class ChatMessageMsg(val author: String, val timestampMs: Long, val message: String) : GtrMsg()
object HeartbeatRequest : GtrMsg()
object HeartbeatResponse : GtrMsg()

class GtrProtocol {

    companion object {
        const val VERSION = 1
        val BT_APP_UUID: UUID = UUID.fromString("eb5d5f8c-8a33-465d-5151-3c2e36cb5490")
    }

}

class GtrParseError(message: String) : RuntimeException(message)

class GtrParser {
    fun parse(msg: String): GtrMsg {
        try {
            return when {
                msg.startsWith("GTR") -> parseGTR(msg.drop(3))
                else -> throw GtrParseError("GTR header not found")
            }
        } catch (e: GtrParseError) {
            throw GtrParseError("GTR parsing error ($msg): ${e.message}")
        }
    }

    private fun parseGTR(msg: String): GtrMsg {
        val versionPrefix = "$VERSION|"
        return when {
            msg.startsWith(versionPrefix) -> parseVersioned(msg.drop(versionPrefix.length))
            else -> throw GtrParseError("invalid GTR version")
        }
    }

    private fun parseVersioned(msg: String): GtrMsg {
        return when {
            msg.startsWith("MSG|") -> parseChatMessage(msg.drop(4))
            else -> throw GtrParseError("unknown command")
        }
    }

    private fun parseChatMessage(msg: String): GtrMsg {
        var bound = msg.indexOf('|')
        if (bound == -1)
            throw GtrParseError("delimiter not found")
        val author = msg.take(bound)
        val rest = msg.drop(bound + 1)

        bound = rest.indexOf('|')
        if (bound == -1)
            throw GtrParseError("second delimiter not found")
        val timestamp = rest.take(bound).toLong()
        val message = rest.drop(bound + 1)

        return ChatMessageMsg(author, timestamp, message)
    }
}

class GtrFormatter {
    fun format(msg: GtrMsg): String {
        return "GTR$VERSION|" + formatGTR(msg)
    }

    fun formatGTR(msg: GtrMsg): String {
        return when (msg) {
            is ChatMessageMsg -> "MSG|${msg.author}|${msg.timestampMs}|${msg.message}"
            else -> throw GtrParseError("unsupported msg type")
        }
    }
}