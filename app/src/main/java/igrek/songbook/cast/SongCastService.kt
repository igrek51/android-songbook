package igrek.songbook.cast

import igrek.songbook.R
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
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
import igrek.songbook.util.limitBetween
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
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
    private var streamSocket = StreamSocket(::onEventBroadcast)

    private var myName: String = ""
    var myMemberPublicId: String = ""
        private set
    var sessionCode: String? = null
        private set
    private var ephemeralSong: Song? = null
    var onSessionUpdated: () -> Unit = {}
    var sessionState: SessionState = SessionState()
    private var periodicRefreshJob: Job? = null
    private var lastSessionDetailsChange: Long = 0

    val presenters: List<CastMember> get() = sessionState.members.filter { it.type == CastMemberType.OWNER.value }
    val spectators: List<CastMember> get() = sessionState.members.filter { it.type == CastMemberType.GUEST.value }

    companion object {
        const val songbookApiBase = "https://songbook.igrek.dev"
        private const val createSessionUrl = "$songbookApiBase/api/cast"
        private val joinSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/join" }
        private val dropSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/drop" }
        private val sessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session" }
        private val sessionSongUrl = { session: String -> "${songbookApiBase}/api/cast/$session/song" }
        private val sessionScrollUrl = { session: String -> "${songbookApiBase}/api/cast/$session/scroll" }
        private val sessionChatUrl = { session: String -> "${songbookApiBase}/api/cast/$session/chat" }
        private const val authDeviceHeader = "X-Songbook-Device-Id"
    }

    fun isInRoom(): Boolean {
        return sessionCode != null
    }

    fun isPresenter(): Boolean {
        return isInRoom() && presenters.any { it.public_member_id == myMemberPublicId }
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
            when (responseData.rejoined) {
                true -> logger.info("SongCast session rejoined: ${responseData.short_id}")
                false -> logger.info("SongCast session created: ${responseData.short_id}")
            }
            initRoom(responseData)
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
            when (responseData.rejoined) {
                true -> logger.info("SongCast session rejoined: ${responseData.short_id}")
                false -> logger.info("SongCast session joined: ${responseData.short_id}")
            }
            initRoom(responseData)
            responseData
        }
    }

    private fun initRoom(responseData: CastSessionJoined) {
        this.sessionCode = responseData.short_id
        this.myName = responseData.member_name
        this.myMemberPublicId = responseData.public_member_id
        streamSocket.connect(responseData.short_id)

        periodicRefreshJob = GlobalScope.launch(Dispatchers.IO) {
            while (isInRoom()) {
                refreshSessionDetails()
                val noActivityPenalty: Long = when (lastSessionDetailsChange) {
                    0L -> 0
                    else -> {
                        val millis = Date().time - lastSessionDetailsChange
                        millis.limitBetween(0, 25 * 60 * 1000) / 5 // 0-5 min
                    }
                }
                val interval = 5_000 + noActivityPenalty + (0..1000).random().toLong()
                delay(interval)
            }
        }
    }

    private fun exitRoom() {
        sessionCode = null
        sessionState.initialized = false
        sessionState.members = listOf()
        sessionState.castSongDto = null
        sessionState.currentScroll = null
        sessionState.chatMessages = listOf()
        streamSocket.close()
        if (periodicRefreshJob?.isActive == true) {
            periodicRefreshJob?.cancel()
        }
        periodicRefreshJob = null
        lastSessionDetailsChange = 0
    }

    fun dropSessionAsync(): Deferred<Result<Unit>> {
        val nSessionCode = sessionCode
        exitRoom()

        if (nSessionCode == null) {
            logger.warn("SongCast session not dropped - not joined")
            return GlobalScope.async { Result.success(Unit) }
        }
        logger.info("Dropping SongCast session: $nSessionCode")
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(dropSessionUrl(nSessionCode))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(null, ""))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast session $nSessionCode dropped")
        }
    }

    private fun getSessionDetailsAsync(): Deferred<Result<CastSession>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(sessionUrl(sessionCode ?: ""))
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
            sessionState.members = responseData.members
            sessionState.castSongDto = responseData.song
            this.ephemeralSong = buildEphemeralSong(responseData.song)
            sessionState.currentScroll = responseData.scroll
            sessionState.chatMessages = responseData.chat_messages
            sessionState.initialized = true
            responseData
        }
    }

    private fun postSongPresentAsync(payload: CastSongSelected): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val json = httpRequester.jsonSerializer.encodeToString(CastSongSelected.serializer(), payload)
        val request: Request = Request.Builder()
            .url(sessionSongUrl(sessionCode ?: ""))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast: Song selection sent: ${payload.title} - ${payload.artist}")
        }
    }

    fun postChatMessageAsync(payload: CastChatMessageSent): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val json = httpRequester.jsonSerializer.encodeToString(CastChatMessageSent.serializer(), payload)
        val request: Request = Request.Builder()
            .url(sessionChatUrl(sessionCode ?: ""))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast: chat message sent: ${payload.text}")
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

    fun openCurrentSong() {
        val ephemeralSongN = ephemeralSong ?: return
        GlobalScope.launch {
            appFactory.songOpener.get().openSongPreview(ephemeralSongN)
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

    private fun onSongSelectedEvent(eventData: JSONObject) {
        val id = eventData.getString("id")
        val title = eventData.getString("title")
        val artist: String? = eventData.optString("artist")
        val content = eventData.getString("content")
        val chordsNotationId = eventData.getLong("chords_notation_id")
        val chosenBy = eventData.getString("chosen_by")
        logger.debug("SongSelectedEvent: $eventData")

        sessionState.castSongDto = CastSong(
            id = id,
            title = title,
            artist = artist,
            content = content,
            chords_notation_id = chordsNotationId,
            chosen_by = chosenBy,
        )
        this.ephemeralSong = buildEphemeralSong(sessionState.castSongDto)

        val presenter: CastMember? = sessionState.members.find { it.public_member_id == chosenBy }
        val presenterName = presenter?.name ?: "Unknown"
        val songName = buildSongName(title, artist)
        uiInfoService.showInfo(R.string.songcast_song_selected, presenterName, songName)
        notifySessionUpdated()
        if (isFollowingCurrentSong(presenter)) {
            openCurrentSong()
        }
    }

    private fun isFollowingCurrentSong(presenter: CastMember?): Boolean {
        return when (presenter?.public_member_id) {
            null -> false
            myMemberPublicId -> false
            else -> true
        }
    }

    private suspend fun onEventBroadcast(data: JSONObject) {
        when (val type = data.getString("type")) {

            "SongSelectedEvent" -> {
                val eventData = data.getJSONObject("data")
                onSongSelectedEvent(eventData)
            }

            "SongDeselectedEvent" -> {
                sessionState.castSongDto = null
                this.ephemeralSong = null
                notifySessionUpdated()
            }

            "CastMembersUpdatedEvent" -> {
                refreshSessionDetails()
            }

            "ChatMessageReceivedEvent" -> {
                refreshSessionDetails()
            }

            else -> logger.warn("Unknown SongCast event type: $type")
        }
    }

    suspend fun refreshSessionDetails() {
        logger.debug("refreshing SongCast session...")
        val oldState = sessionState.copy()
        val result = getSessionDetailsAsync().await()
        result.fold(onSuccess = {
            val compareResult = compareSessionStates(oldState, sessionState)
            if (compareResult)
                lastSessionDetailsChange = Date().time
            notifySessionUpdated()
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private fun compareSessionStates(oldState: SessionState, newState: SessionState): Boolean {
        if (!oldState.initialized)
            return true

        if (oldState.chatMessages != newState.chatMessages) {
            val newMessages = newState.chatMessages.minus(oldState.chatMessages.toSet())
            if (newMessages.isNotEmpty()) {
                val message = newMessages.last()
                uiInfoService.showInfo(R.string.songcast_new_chat_message, message.author, message.text)
            }
            return true
        }
        if (oldState.members != newState.members) {
            val droppedMembers = oldState.members.toSet().minus(newState.members.toSet())
            droppedMembers.forEach { member ->
                uiInfoService.showInfo(R.string.songcast_member_dropped, member.name)
            }
            val newMembers = newState.members.toSet().minus(oldState.members.toSet())
            newMembers.forEach { member ->
                uiInfoService.showInfo(R.string.songcast_new_member_joined, member.name)
            }
            return true
        }
        if (oldState.castSongDto != newState.castSongDto) {
            val castSongDto = newState.castSongDto
            if (castSongDto != null) {
                val chosenById = castSongDto.chosen_by
                val presenter: CastMember? = sessionState.members.find { it.public_member_id == chosenById }
                val presenterName = presenter?.name ?: "Unknown"
                val songName = buildSongName(castSongDto.title, castSongDto.artist)
                uiInfoService.showInfo(R.string.songcast_song_selected, presenterName, songName)
            }
            return true
        }
        return false
    }

    private fun notifySessionUpdated() = GlobalScope.launch(Dispatchers.Main) {
        onSessionUpdated()
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
    var chosen_by: String, // public member ID
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

data class SessionState(
    var initialized: Boolean = false,
    var members: List<CastMember> = listOf(),
    var castSongDto: CastSong? = null,
    var currentScroll: CastScroll? = null,
    var chatMessages: List<CastChatMessage> = listOf(),
)

enum class CastMemberType(val value: String) {
    OWNER("owner"), // can pick current song, presenter
    GUEST("guest"), // read-only spectator
    ;
}
