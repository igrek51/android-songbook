package igrek.songbook.cast

import igrek.songbook.R
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.util.buildSongName
import io.socket.client.Ack
import io.socket.client.AckWithTimeout
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Date


@OptIn(DelicateCoroutinesApi::class)
class SongCastService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    deviceIdProvider: LazyInject<DeviceIdProvider> = appFactory.deviceIdProvider,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val deviceIdProvider by LazyExtractor(deviceIdProvider)

    private val logger: Logger = LoggerFactory.logger
    private val httpRequester = HttpRequester()
    private var ioSocket: Socket? = null

    private var myName: String = ""
    var myMemberPublicId: String = ""
        private set
    var sessionShortId: String? = null
        private set
    private var members: List<CastMember> = listOf()
        private set
    var castSongDto: CastSong? = null
        private set
    private var ephemeralSong: Song? = null
        private set
    private var currentScroll: CastScroll? = null
        private set
    var chatMessages: List<CastChatMessage> = listOf()
        private set
    var onSessionUpdated: () -> Unit = {}

    val presenters: List<CastMember> get() = members.filter { it.type == CastMemberType.OWNER.value }
    val spectators: List<CastMember> get() = members.filter { it.type == CastMemberType.GUEST.value }
    val myMember: CastMember? get() = members.find { it.public_member_id == myMemberPublicId }

    companion object {
        private const val songbookApiBase = "https://songbook.igrek.dev"
        private const val createSessionUrl = "$songbookApiBase/api/cast"
        private const val socketioUrl = "$songbookApiBase"
        private val joinSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/join" }
        private val dropSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/drop" }
        private val sessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session" }
        private val sessionSongUrl = { session: String -> "${songbookApiBase}/api/cast/$session/song" }
        private val sessionScrollUrl = { session: String -> "${songbookApiBase}/api/cast/$session/scroll" }
        private val sessionChatUrl = { session: String -> "${songbookApiBase}/api/cast/$session/chat" }
        private const val authDeviceHeader = "X-Songbook-Device-Id"
    }

    fun isInRoom(): Boolean {
        return sessionShortId != null
    }

    fun isPresenter(): Boolean {
        return isInRoom() && presenters.any { it.public_member_id == myMemberPublicId }
    }

    fun isSpectator(): Boolean {
        return isInRoom() && spectators.any { it.public_member_id == myMemberPublicId }
    }

    private fun clearRoom() {
        sessionShortId = null
        members = listOf()
        castSongDto = null
        currentScroll = null
        chatMessages = listOf()
        ioSocket?.disconnect()
        ioSocket?.close()
        ioSocket = null
    }

    fun createSessionAsync(memberName: String): Deferred<Result<CastSessionJoined>> {
        logger.info("Creating SongCast session by member '$memberName'...")
        val deviceId = deviceIdProvider.getDeviceId()
        val dto = CastSessionJoin(member_name = memberName)
        val json = httpRequester.jsonSerializer.encodeToString(CastSessionJoin.serializer(), dto)
        val request: Request = Request.Builder()
            .url(createSessionUrl)
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body()?.string() ?: ""
            val responseData: CastSessionJoined =
                httpRequester.jsonSerializer.decodeFromString(
                    CastSessionJoined.serializer(),
                    jsonData
                )
            this.sessionShortId = responseData.short_id
            this.myName = responseData.member_name
            this.myMemberPublicId = responseData.public_member_id
            when (responseData.rejoined) {
                true -> logger.info("SongCast session rejoined: ${responseData.short_id}")
                false -> logger.info("SongCast session created: ${responseData.short_id}")
            }
            connectSocketIO()
            responseData
        }
    }

    fun joinSessionAsync(sessionCode: String, memberName: String): Deferred<Result<CastSessionJoined>> {
        logger.info("Joining SongCast session by member '$memberName'...")
        val deviceId = deviceIdProvider.getDeviceId()
        val dto = CastSessionJoin(member_name = memberName)
        val json = httpRequester.jsonSerializer.encodeToString(CastSessionJoin.serializer(), dto)
        val request: Request = Request.Builder()
            .url(joinSessionUrl(sessionCode))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body()?.string() ?: ""
            val responseData: CastSessionJoined =
                httpRequester.jsonSerializer.decodeFromString(
                    CastSessionJoined.serializer(),
                    jsonData
                )
            this.sessionShortId = responseData.short_id
            this.myName = responseData.member_name
            this.myMemberPublicId = responseData.public_member_id
            when (responseData.rejoined) {
                true -> logger.info("SongCast session rejoined: ${responseData.short_id}")
                false -> logger.info("SongCast session joined: ${responseData.short_id}")
            }
            connectSocketIO()
            responseData
        }
    }

    fun dropSessionAsync(): Deferred<Result<Unit>> {
        val nSessionShortId = sessionShortId
        clearRoom()

        if (nSessionShortId == null) {
            logger.warn("SongCast session not dropped - not joined")
            return GlobalScope.async { Result.success(Unit) }
        }
        logger.info("Dropping SongCast session: $nSessionShortId")
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(dropSessionUrl(nSessionShortId))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(null, ""))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast session $nSessionShortId dropped")
        }
    }

    fun getSessionDetailsAsync(): Deferred<Result<CastSession>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(sessionUrl(sessionShortId ?: ""))
            .header(authDeviceHeader, deviceId)
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body()?.string() ?: ""
            val responseData: CastSession =
                httpRequester.jsonSerializer.decodeFromString(
                    CastSession.serializer(),
                    jsonData
                )
            this.members = responseData.members
            this.castSongDto = responseData.song
            this.ephemeralSong = buildEphemeralSong(responseData.song)
            this.currentScroll = responseData.scroll
            this.chatMessages = responseData.chat_messages
            responseData
        }
    }

    private fun postSongPresentAsync(payload: CastSongSelected): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val json = httpRequester.jsonSerializer.encodeToString(CastSongSelected.serializer(), payload)
        val request: Request = Request.Builder()
            .url(sessionSongUrl(sessionShortId ?: ""))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast: Song selection sent: ${payload.title} - ${payload.artist}")
        }
    }

    fun reportSongSelected(song: Song) {
        GlobalScope.launch {
            if (!isPresenter())
                return@launch

            val payload = CastSongSelected(
                id = song.id,
                title = song.title,
                artist = song.artist,
                content = song.content.orEmpty(),
                chords_notation_id = song.chordsNotation.id,
            )

            val result = postSongPresentAsync(payload).await()
            result.fold(onSuccess = {
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    private fun buildEphemeralSong(songDto: CastSong?): Song? {
        if (songDto == null)
            return null
        val now: Long = Date().time
        return Song(
            id = songDto.id,
            title = songDto.title,
            categories = mutableListOf(),
            content = songDto.content,
            versionNumber = 1,
            createTime = now,
            updateTime = now,
            status = SongStatus.PUBLISHED,
            customCategoryName = songDto.artist,
            chordsNotation = ChordsNotation.mustParseById(songDto.chords_notation_id),
            namespace = SongNamespace.Ephemeral,
        )
    }

    fun openCurrentSong() {
        val ephemeralSongN = ephemeralSong ?: return
        GlobalScope.launch {
            appFactory.songOpener.get().openSongPreview(ephemeralSongN)
        }
    }

    private val onSocketIOBroadcast: Emitter.Listener = Emitter.Listener { args ->
        GlobalScope.launch(Dispatchers.IO) {
            safeExecute {
                val data = args[0] as JSONObject
                logger.debug("socket.io broadcast: $data")
                when (val type = data.getString("type")) {

                    "SongSelectedEvent" -> {
                        val eventData = data.getJSONObject("data")
                        onSongSelectedEvent(eventData)
                    }

                    "SongDeselectedEvent" -> {
                        onSongDeselectedEvent()
                    }

                    "CastMembersUpdatedEvent" -> {
                        refreshSessionDetails()
                    }

                    else -> {
                        logger.warn("Unknown SongCast event type: $type")
                    }
                }
            }
        }
    }

    private fun onSongSelectedEvent(eventData: JSONObject) {
        val id = eventData.getString("id")
        val title = eventData.getString("title")
        val artist: String? = eventData.optString("artist")
        val content = eventData.getString("content")
        val chordsNotationId = eventData.getLong("chords_notation_id")
        val chosenBy = eventData.getString("chosen_by")
        logger.debug("SongSelectedEvent: $eventData")

        this.castSongDto = CastSong(
            id = id,
            title = title,
            artist = artist,
            content = content,
            chords_notation_id = chordsNotationId,
            chosen_by = chosenBy,
        )
        this.ephemeralSong = buildEphemeralSong(this.castSongDto)

        val presenter: CastMember? = members.find { it.public_member_id == chosenBy }
        val presenterName = presenter?.name ?: "Unknown"
        val songName = buildSongName(title, artist)
        uiInfoService.showInfo(R.string.songcast_presenter_chose_song, presenterName, songName)

        GlobalScope.launch(Dispatchers.Main) {
            onSessionUpdated()
        }
        if (isFollowingCurrentSong(presenter)) {
            openCurrentSong()
        }
    }

    private fun onSongDeselectedEvent() {
        this.castSongDto = null
        this.ephemeralSong = null
        GlobalScope.launch(Dispatchers.Main) {
            onSessionUpdated()
        }
    }

    private fun isFollowingCurrentSong(presenter: CastMember?): Boolean {
        return when (presenter?.public_member_id) {
            myMemberPublicId -> false
            else -> true
        }
    }

    private suspend fun refreshSessionDetails() {
        logger.debug("refreshing SongCast session...")
        val result = getSessionDetailsAsync().await()
        result.fold(onSuccess = {
            GlobalScope.launch(Dispatchers.Main) {
                onSessionUpdated()
            }
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private val onClientSubscribed: Emitter.Listener = Emitter.Listener { args ->
        logger.debug("ACK: SongCast client subscribed to socket.io")
    }

    private fun connectSocketIO() {
        GlobalScope.launch(Dispatchers.IO) {
            safeExecute {
                val opts = IO.Options()
                opts.path = "/socket.io/cast"
                val socket = IO.socket(socketioUrl, opts)
                ioSocket = socket

                socket.on("broadcast_new_event", onSocketIOBroadcast)
                socket.on("subscribe_for_session_events_ack", onClientSubscribed)

                socket.connect()
                logger.debug("SongCast connected to socket.io")
                logger.debug("socket.isActive: ${socket.isActive}")

                val args: Array<Any> = arrayOf(
                    JSONObject(
                        mapOf("session_id" to sessionShortId)
                    )
                )

                delay(500)
                socket.emit("subscribe_for_session_events", args, Ack {
                    logger.debug("ACK: subscribe_for_session_events: $it")
                })
            }
        }
    }

}

@Serializable
data class CastSessionJoin(
    var member_name: String,
)

@Serializable
data class CastSessionJoined(
    var short_id: String,
    var public_member_id: String,
    var member_name: String,
    var rejoined: Boolean,
)

@Serializable
data class CastSession(
    var short_id: String,
    var create_timestamp: Long, // in seconds
    var update_timestamp: Long, // in seconds
    var ttl: Long, // in seconds
    var members: List<CastMember>,
    var song: CastSong?,
    var scroll: CastScroll?,
    var chat_messages: List<CastChatMessage>,
)

@Serializable
data class CastMember(
    var public_member_id: String,
    var name: String,
    var type: String,
)

@Serializable
data class CastSong(
    var id: String,
    var chosen_by: String,
    var title: String,
    var artist: String?,
    var content: String,
    var chords_notation_id: Long,
)

@Serializable
data class CastScroll(
    var view_start: Float,
    var view_end: Float,
    var visible_text: String?,
)

@Serializable
data class CastChatMessage(
    var timestamp: Long, // in seconds
    var author: String,
    var text: String,
)

@Serializable
data class CastSongSelected(
    var id: String,
    var title: String,
    var artist: String?,
    var content: String,
    var chords_notation_id: Long,
)

@Serializable
data class CastChatMessageSent(
    var text: String,
)

enum class CastMemberType(val value: String) {
    OWNER("owner"), // can pick current song, presenter
    GUEST("guest"), // read-only spectator
    ;
}
