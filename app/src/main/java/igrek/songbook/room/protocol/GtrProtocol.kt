package igrek.songbook.room.protocol

import igrek.songbook.room.protocol.GtrProtocol.Companion.VERSION
import java.util.*
import kotlin.reflect.KClass

open class GtrMsg {
    override fun toString(): String = GtrFormatter().format(this)
}

internal class MsgSpec<T : GtrMsg>(
    val code: String,
    val clazz: KClass<T>,
    val partsFormatter: ((msg: T) -> List<String>)? = null,
    val partsParser: (parts: List<String>) -> T,
    val requiredParts: Int = 0,
) {
    @Suppress("UNCHECKED_CAST")
    fun formatGTR(msg: GtrMsg): String {
        val datapart = when (partsFormatter) {
            null -> ""
            else -> partsFormatter.invoke(msg as T).joinToString("|")
        }
        return "$code|$datapart"
    }
}

internal fun Boolean.toGtrString(): String = if (this) "1" else "0"

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

    private fun parseCommand(msg: String): GtrMsg {
        val bound = msg.indexOf('|')
        if (bound == -1)
            throw GtrParseError("code delimiter not found")
        val code = msg.take(bound)
        val rest = msg.drop(bound + 1)

        val msgSpec = findMsgSpec(code) ?: throw GtrParseError("unknown command")

        val parts = when {
            msgSpec.requiredParts > 0 -> {
                val parts = rest.split('|', limit = msgSpec.requiredParts)
                check(parts.size == msgSpec.requiredParts) { "invalid datagram parts" }
                parts
            }
            else -> rest.split('|')
        }

        return msgSpec.partsParser.invoke(parts)
    }

    private fun findMsgSpec(code: String): MsgSpec<out GtrMsg>? {
        return msgSpecs.find { it.code == code }
    }
}

class GtrFormatter {
    fun format(msg: GtrMsg): String {
        return "GTR$VERSION|" + formatGTR(msg)
    }

    private fun formatGTR(msg: GtrMsg): String {
        val msgSpec = findMsgSpec(msg)
        return msgSpec.formatGTR(msg)
    }

    private fun findMsgSpec(msg: GtrMsg): MsgSpec<out GtrMsg> {
        return msgSpecs.first { it.clazz.isInstance(msg) }
    }
}