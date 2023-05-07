package igrek.songbook.cast

import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(DelicateCoroutinesApi::class)
class StreamSocket(
    private val onEventBroadcast: suspend (data: JSONObject) -> Unit,
) {

    private val logger: Logger = LoggerFactory.logger
    private var ioSocket: Socket? = null
    private val debug: Boolean = false
    private var connectJob: Job? = null

    fun connect(sessionCode: String) {
        connectJob = GlobalScope.launch(Dispatchers.IO) {
            safeExecute {
                val opts = IO.Options()
                opts.path = "/socket.io/cast"
                val socket = IO.socket(SongCastService.songbookApiBase, opts)
                ioSocket = socket

                socket.on("broadcast_new_event") { args ->
                    GlobalScope.launch(Dispatchers.IO) {
                        safeExecute {
                            val data = args[0] as JSONObject
                            onEventBroadcast(data)
                        }
                    }
                }

                socket.on("subscribe_for_session_events_ack") {
                    logger.debug("SongCast subscribed to socket.io room events (ACK)")
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
                    logger.debug("socket.io: Connected, id: ${socket.id()}")
                    socket.emit("subscribe_for_session_events", JSONObject(
                        mapOf("session_id" to sessionCode)
                    ))
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

                if (debug)
                    logger.debug("SongCast connected to socket.io")
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