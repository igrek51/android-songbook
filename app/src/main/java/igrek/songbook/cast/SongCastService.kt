package igrek.songbook.cast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import igrek.songbook.R
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.util.buildSongName
import igrek.songbook.util.interpolate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Date


class SongCastService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    deviceIdProvider: LazyInject<DeviceIdProvider> = appFactory.deviceIdProvider,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val deviceIdProvider by LazyExtractor(deviceIdProvider)
    private val layoutController by LazyExtractor(layoutController)

    private val logger: Logger = LoggerFactory.logger
    private val httpRequester = HttpRequester()
    private var streamSocket = StreamSocket(::onEventBroadcast)
    private var myName: String = ""
    private var myMemberPublicId: String = ""
    var sessionCode: String? = null
    private var ephemeralSong: Song? = null
    var onSessionUpdated: () -> Unit = {}
    var sessionState: SessionState = SessionState()
    private var periodicRefreshJob: Job? = null
    private var periodicReconnectJob: Job? = null
    private var lastSessionChange: Long = 0
    private var joinTimestamp: Long = 0
    var clientFollowScroll: Boolean by mutableStateOf(true)

    var presenterFocusControl: CastScrollControl
        get() = appFactory.preferencesState.g.castScrollControl
        set(value) {
            appFactory.preferencesState.g.castScrollControl = value
        }

    companion object {
        const val songbookApiBase = "https://songbook.igrek.dev"
        private const val createSessionUrl = "$songbookApiBase/api/cast"
        private val joinSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/join" }
        private const val rejoinSessionUrl = "${songbookApiBase}/api/cast/rejoin"
        private val dropSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/drop" }
        private val sessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session" }
        private val sessionSongUrl = { session: String -> "${songbookApiBase}/api/cast/$session/song" }
        private val sessionScrollUrl = { session: String -> "${songbookApiBase}/api/cast/$session/scroll" }
        private val sessionChatUrl = { session: String -> "${songbookApiBase}/api/cast/$session/chat" }
        private const val authDeviceHeader = "X-Songbook-Device-Id"
    }

    val presenters: List<CastMember> get() = sessionState.members.filter { it.type == CastMemberType.OWNER.value }
    val spectators: List<CastMember> get() = sessionState.members.filter { it.type == CastMemberType.GUEST.value }

    fun isInRoom(): Boolean = sessionCode != null

    fun isPresenter(): Boolean {
        return isInRoom() && presenters.any { it.public_member_id == myMemberPublicId }
    }

    fun isPresenting(): Boolean {
        return isPresenter() && sessionState.castSongDto?.chosen_by == myMemberPublicId
    }

    fun isSongSelected(): Boolean = sessionState.castSongDto != null

    private fun findMemberByPublicId(publicId: String): CastMember? {
        return sessionState.members.find { it.public_member_id == publicId }
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
                httpRequester.jsonSerializer.decodeFromString(CastSessionJoined.serializer(), jsonData)
            when (responseData.rejoined) {
                true -> logger.info("SongCast session rejoined: ${responseData.short_id}")
                false -> logger.info("SongCast session joined: ${responseData.short_id}")
            }
            initRoom(responseData)
            responseData
        }
    }

    fun restoreSessionAsync(): Deferred<Result<CastSessionJoined>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(rejoinSessionUrl)
            .header(authDeviceHeader, deviceId)
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body()?.string() ?: ""
            val responseData: CastSessionJoined =
                httpRequester.jsonSerializer.decodeFromString(CastSessionJoined.serializer(), jsonData)
            logger.info("SongCast session restored: ${responseData.short_id}")
            initRoom(responseData)
            responseData
        }
    }

    private fun initRoom(responseData: CastSessionJoined) {
        this.sessionCode = responseData.short_id
        this.myName = responseData.member_name
        this.myMemberPublicId = responseData.public_member_id
        streamSocket.connect(responseData.short_id)
        this.joinTimestamp = Date().time / 1000

        periodicRefreshJob = GlobalScope.launch(Dispatchers.IO) {
            try {
                periodicRefresh()
            } catch (e: Throwable) {
                UiErrorHandler().handleContextError(e, R.string.songcast_connection_context)
            }
        }
        periodicReconnectJob = GlobalScope.launch(Dispatchers.IO) {
            periodicReconnect()
        }
    }

    private suspend fun periodicRefresh() {
        val activityController = appFactory.activityController.get()
        var lastShot: Long = Date().time
        while (isInRoom()) {
            val interval: Long? = when {
                !activityController.isForeground -> null
                lastSessionChange == 0L -> 0
                streamSocket.ioSocket?.connected() == false -> (2000..3000).random().toLong()
                else -> {
                    val millis = Date().time - lastSessionChange
                    val fraction = millis.interpolate(0, 4 * 60_000) // 0-4 min -> 0-1
                    val penalty = (fraction * 4 * 60_000).toLong() // 0-1 -> 0-4 min
                    (5_000..6_000).random().toLong() + penalty
                }
            }

            if (interval != null && Date().time - lastShot >= interval) {
                val waitedS = (Date().time - lastShot) / 1000
                logger.debug("refreshing SongCast session, waited ${waitedS}s...")
                val result = refreshSessionDetails()
                if (result.isFailure)
                    return
                lastShot = Date().time
            }
            delay(1000)
        }
    }

    private suspend fun periodicReconnect() {
        val activityController = appFactory.activityController.get()
        var lastShot: Long = Date().time
        while (isInRoom()) {
            val interval: Long? = when {
                !activityController.isForeground -> null
                !streamSocket.initialized -> null
                streamSocket.ioSocket?.connected() == false -> 0
                else -> null
            }

            if (interval != null && Date().time - lastShot >= interval) {
                try {
                    uiInfoService.showInfo(R.string.songcast_reconnecting_to_room)
                    streamSocket.reconnect()
                } catch (e: Throwable) {
                    UiErrorHandler().handleContextError(e, R.string.songcast_connection_context)
                }
                lastShot = Date().time
            }
            delay(1_000)
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
        periodicRefreshJob = null
        periodicReconnectJob = null
        lastSessionChange = 0
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
            sessionState.castSongDto = CastSong(
                id = payload.id,
                chosen_by = myMemberPublicId,
                title = payload.title,
                artist = payload.artist,
                content = payload.content,
                chords_notation_id = payload.chords_notation_id,
            )
            logger.info("SongCast: Song selection sent: ${payload.title} - ${payload.artist}")
        }
    }

    fun postScrollControlAsync(payload: CastScroll): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val json = httpRequester.jsonSerializer.encodeToString(CastScroll.serializer(), payload)
        val request: Request = Request.Builder()
            .url(sessionScrollUrl(sessionCode ?: ""))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) {}
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

        val castSongDto = CastSong(
            id = id,
            title = title,
            artist = artist,
            content = content,
            chords_notation_id = chordsNotationId,
            chosen_by = chosenBy,
        )
        sessionState.castSongDto = castSongDto
        onSongSelectedEventDto(castSongDto)
    }

    private fun onSongSelectedEventDto(castSongDto: CastSong) {
        this.ephemeralSong = buildEphemeralSong(sessionState.castSongDto)

        val presenter: CastMember? = sessionState.members.find { it.public_member_id == castSongDto.chosen_by }
        val presenterName = presenter?.name ?: "Unknown"
        val songName = buildSongName(castSongDto.title, castSongDto.artist)
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
                refreshSessionDetails()
            }
            "CastMembersUpdatedEvent" -> {
                logger.debug("SongCast: CastMembersUpdatedEvent received")
                refreshSessionDetails()
            }
            "ChatMessageReceivedEvent" -> {
                logger.debug("SongCast: ChatMessageReceivedEvent received")
                refreshSessionDetails()
            }
            "SongScrolledEvent" -> {
                refreshSessionDetails()
            }
            else -> {
                logger.warn("Unknown SongCast event type: $type")
                refreshSessionDetails()
            }
        }
    }

    fun refreshSessionIfInRoom() {
        if (!isInRoom())
            return
        GlobalScope.launch(Dispatchers.IO) {
            refreshSessionDetails()
        }
    }

    suspend fun refreshSessionDetails(): Result<Unit> {
        val oldState = sessionState.copy()
        val result = getSessionDetailsAsync().await()
        result.fold(onSuccess = {
            val compareResult = compareSessionStates(oldState, sessionState)
            if (compareResult)
                lastSessionChange = Date().time
            notifySessionUpdated()
            return Result.success(Unit)
        }, onFailure = { e ->
            UiErrorHandler().handleContextError(e, R.string.songcast_connection_context)
            return Result.failure(e)
        })
    }

    private fun compareSessionStates(oldState: SessionState, newState: SessionState): Boolean {
        if (!oldState.initialized)
            return true

        if (oldState.castSongDto != newState.castSongDto) {
            val castSongDto = newState.castSongDto
            if (castSongDto != null) {
                onSongSelectedEventDto(castSongDto)
            }
            return true
        }
        if (oldState.chatMessages != newState.chatMessages) {
            val newMessages = newState.chatMessages.minus(oldState.chatMessages.toSet())
            if (newMessages.isNotEmpty()) {
                val message = newMessages.last()
                if (message.author != this.myName) {
                    uiInfoService.showInfo(R.string.songcast_new_chat_message, message.author, message.text)
                }
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
        if (oldState.currentScroll != newState.currentScroll) {
            val scrollDto = newState.currentScroll
            if (scrollDto != null) {
                if (clientFollowScroll && !isPresenting() && layoutController.isState(SongPreviewLayoutController::class)) {
                    logger.debug("Scrolling by SongCast event: ${scrollDto.view_start}")
                    appFactory.scrollService.g.adaptToScrollControl(
                        scrollDto.view_start, scrollDto.view_end, scrollDto.visible_text, scrollDto.mode,
                    )
                }
            }
            return true
        }
        return false
    }

    private fun notifySessionUpdated() = GlobalScope.launch(Dispatchers.Main) {
        onSessionUpdated()
    }

    fun generateChatEvents(): List<LogEvent> {
        val allEvents = mutableListOf<LogEvent>()
        allEvents.addAll(
            presenters.map {
                val name = if (it.public_member_id == myMemberPublicId) "${it.name} (You)" else it.name
                SystemLogEvent(
                    timestamp = this.joinTimestamp,
                    text = "$name joined the room as Presenter",
                )
            }
        )
        allEvents.addAll(
            spectators.map {
                val name = if (it.public_member_id == myMemberPublicId) "${it.name} (You)" else it.name
                SystemLogEvent(
                    timestamp = this.joinTimestamp,
                    text = "$name joined the room as Spectator",
                )
            }
        )

        allEvents.addAll(sessionState.chatMessages.map {
            MessageLogEvent(
                timestamp = it.timestamp,
                author = it.author,
                text = it.text,
            )
        })

        sessionState.castSongDto?.let { castSongDto ->
            ephemeralSong?.let { ephemeralSong ->
                val member = findMemberByPublicId(castSongDto.chosen_by)
                allEvents.add(
                    SongLogEvent(
                        timestamp = ephemeralSong.createTime / 1000,
                        author = member?.name ?: "Unknown",
                        song = ephemeralSong,
                    )
                )
            }
        }
        return allEvents.sortedBy { it.timestamp }
    }
}

data class SessionState(
    var initialized: Boolean = false,
    var members: List<CastMember> = listOf(),
    var castSongDto: CastSong? = null,
    var currentScroll: CastScroll? = null,
    var chatMessages: List<CastChatMessage> = listOf(),
)
