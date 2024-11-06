package igrek.songbook.cast

import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.util.ioScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class StreamSocket(
    private val onEventBroadcast: suspend (data: JSONObject) -> Unit,
)  : WebSocketListener() {

    private val logger: Logger = LoggerFactory.logger
    private var webSocket: WebSocket? = null
    var initialized: Boolean = false
    var connected: Boolean = false
    private var connectJob: Job? = null
    private var sessionCode: String? = null

    companion object {
        const val WS_URL_BASE = "wss://songbook.igrek.dev"
    }

    fun connect(sessionCode: String) {
        this.sessionCode = sessionCode
        initialized = false
        connected = false
        val that = this
        connectJob = ioScope.launch {

            val client: OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(10,  TimeUnit.SECONDS)
                .readTimeout(0,  TimeUnit.MILLISECONDS)
                .build()
            val request = Request.Builder()
                .url("$WS_URL_BASE/ws/cast/$sessionCode/events")
                .build()
            webSocket = client.newWebSocket(request, that)
            // Trigger shutdown of the dispatcher's executor so this process exits immediately.
            client.dispatcher.executorService.shutdown()

            initialized = true
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.info("SongCast WS: connection accepted")
        connected = true
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.info("SongCast WS: message received: $text")
        onTextMessage(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        logger.info("SongCast WS: binary message received: $bytes")
        val text = bytes.utf8()
        onTextMessage(text)
    }

    private fun onTextMessage(text: String) {
        if (text == "ping") return
        val jsonData = JSONObject(text)
        ioScope.launch {
            onEventBroadcast(jsonData)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logger.info("SongCast WS: closing by remote peer")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logger.info("SongCast WS: connection closed")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.error("SongCast: Websocket error", t)
        connected = false
    }

    fun reconnect() {
        val sessionCode = this.sessionCode ?: return
        logger.debug("Reconnecting to SongCast room $sessionCode")
        close()
        connect(sessionCode)
    }

    fun close() {
        if (connectJob?.isActive == true) {
            connectJob?.cancel()
        }
        if (webSocket != null) {
            webSocket?.close(1000, "closing by client")
            logger.info("Unsubscribed from topic")
            sessionCode = null
            webSocket = null
        }
        initialized = false
        connected = false
    }
}
