package igrek.songbook.cast

import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.util.ioScope
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class StreamSocket(
    private val onEventBroadcast: suspend (data: JSONObject) -> Unit,
) {

    private val logger: Logger = LoggerFactory.logger
    var ioSocket: Socket? = null
    var initialized: Boolean = false
    private val debug: Boolean = false
    private var connectJob: Job? = null

    companion object {
        const val songbookApiBase = "https://songbook.igrek.dev"
    }

    fun connect(sessionCode: String) {
        connectJob = ioScope.launch {
            val opts = IO.Options()
            opts.path = "/socket.io/cast"
            val socket = IO.socket(songbookApiBase, opts)
            ioSocket = socket
            initialized = false

            socket.on("broadcast_new_event") { args ->
                ioScope.launch {
                    val data = args[0] as JSONObject
                    onEventBroadcast(data)
                }
            }

            socket.on("subscribe_for_session_events_ack") {
                logger.debug("ACK: SongCast subscribed to socket.io room events")
            }

            if (debug) {
                socket.onAnyIncoming { args ->
                    logger.debug("socket incoming packet: $args")
                }
                socket.onAnyOutgoing { args ->
                    logger.debug("socket outgoing packet: $args")
                }
            }

            socket.on(Socket.EVENT_CONNECT) {
                logger.debug("socket.io: Connected with id: ${socket.id()}")
//                socket.emit("subscribe_for_session_events", JSONObject(
//                    mapOf("session_id" to sessionCode)
//                ))
            }
            socket.on(Socket.EVENT_DISCONNECT) {
                logger.debug("socket.io: Disconnected")
            }
            socket.on(Socket.EVENT_CONNECT_ERROR) {
                logger.error("socket.io: Connection Eror")
            }

            socket.disconnect()
            socket.connect()

            for (i in 1..20) {
                if (socket.connected())
                    break
                logger.warn("Reconnecting to socket.io ($i)")
                socket.disconnect()
                socket.connect()
                delay(100 + i * 100L)
                if (i == 20 && !socket.connected()) {
                    throw RuntimeException("Failed to connect to socket.io events stream")
                }
            }

            socket.emit("subscribe_for_session_events", JSONObject(
                mapOf("session_id" to sessionCode)
            ), Ack {
                if (debug)
                    logger.debug("ACK: subscribe_for_session_events")
            })

            initialized = true
            if (debug)
                logger.debug("SongCast connected to socket.io")
        }
    }

    suspend fun reconnect() {
        val socket = ioSocket ?: return
        for (i in 1..20) {
            if (socket.connected())
                break
            logger.warn("Reconnecting to socket.io ($i)")
            socket.disconnect()
            socket.connect()
            delay(100 + i * 150L)
            if (i == 20 && !socket.connected()) {
                throw RuntimeException("Failed to connect to socket.io events stream, reconnecting...")
            }
        }
    }

    fun close() {
        ioSocket?.disconnect()
        ioSocket = null
        if (connectJob?.isActive == true) {
            connectJob?.cancel()
        }
    }
}