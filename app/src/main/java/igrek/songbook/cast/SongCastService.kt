package igrek.songbook.cast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import igrek.songbook.R
import igrek.songbook.custom.sync.SongHasher
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.util.buildSongName
import igrek.songbook.util.defaultScope
import igrek.songbook.util.interpolate
import igrek.songbook.util.ioScope
import igrek.songbook.util.mainScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date


class SongCastService {
    private val uiInfoService by LazyExtractor(appFactory.uiInfoService)
    private val layoutController by LazyExtractor(appFactory.layoutController)
    private val activityController by LazyExtractor(appFactory.activityController)
    private val songPreviewLayoutController by LazyExtractor(appFactory.songPreviewLayoutController)
    private val songOpener by LazyExtractor(appFactory.songOpener)
    private val scrollService by LazyExtractor(appFactory.scrollService)

    private val logger: Logger = LoggerFactory.logger
    private val requester = SongCastRequester()
    private var streamSocket = StreamSocket(::onEventBroadcast)
    private var myName: String = ""
    var myMemberPublicId: String = ""
    var sessionCode: String? = null
    private var ephemeralSong: Song? = null
    var onSessionUpdated: () -> Unit = {}
    var sessionState: SessionState = SessionState()
    private var periodicRefreshJob: Job? = null
    private var periodicReconnectJob: Job? = null
    private var lastSessionChange: Long = 0
    private var joinTimestamp: Long = 0
    var clientFollowScroll: Boolean by mutableStateOf(true)
    var clientOpenPresentedSongs: Boolean by mutableStateOf(true)
    var lastSharedScroll: CastScroll? = null
    private val logEvents: MutableList<LogEvent> = mutableListOf()

    val presenters: List<CastMember> get() = sessionState.members.filter { it.type == CastMemberType.OWNER.value }
    val spectators: List<CastMember> get() = sessionState.members.filter { it.type == CastMemberType.GUEST.value }

    fun isInRoom(): Boolean = sessionCode != null

    fun isPresenter(): Boolean = isInRoom() && presenters.any { it.public_member_id == myMemberPublicId }

    fun isPresenting(): Boolean = isPresenter() && sessionState.castSongDto?.chosen_by == myMemberPublicId

    fun isSongSelected(): Boolean = sessionState.castSongDto != null

    fun createSessionAsync(memberName: String): Deferred<Result<CastSessionJoined>> {
        return requester.createSessionAsync(memberName) { responseData ->
            initRoom(responseData)
        }
    }

    fun joinSessionAsync(sessionCode: String, memberName: String): Deferred<Result<CastSessionJoined>> {
        return requester.joinSessionAsync(sessionCode, memberName) { responseData ->
            initRoom(responseData)
        }
    }

    fun restoreSessionAsync(): Deferred<Result<CastSessionJoined>> {
        return requester.restoreSessionAsync { responseData ->
            initRoom(responseData)
        }
    }

    private fun initRoom(responseData: CastSessionJoined) {
        this.sessionCode = responseData.short_id
        this.requester.sessionCode = responseData.short_id
        this.myName = responseData.member_name
        this.myMemberPublicId = responseData.public_member_id
        streamSocket.connect(responseData.short_id)
        this.joinTimestamp = Date().time / 1000
        addSystemLogEvent(R.string.songcast_new_member_joined, responseData.member_name)
        defaultScope.launch {
            refreshSessionDetails()
            val ephemeralSongN = ephemeralSong ?: return@launch
            val castSongDtoN = sessionState.castSongDto ?: return@launch
            val presenter: CastMember? = sessionState.members.find { it.public_member_id == castSongDtoN.chosen_by }
            val presenterName = presenter?.name ?: "Unknown"
            logEvents.add(
                SongLogEvent(
                    timestampMs = Date().time,
                    author = presenterName,
                    song = ephemeralSongN,
                )
            )
            logEvents.add(
                SystemLogEvent(
                    timestampMs = sessionState.createdTime * 1000,
                    text = uiInfoService.resString(R.string.songcast_session_created),
                )
            )
            refreshUI()
        }
        periodicRefreshJob = ioScope.launch {
            try {
                periodicRefresh()
            } catch (e: Throwable) {
                UiErrorHandler().handleContextError(e, R.string.songcast_connection_context)
            }
        }
        periodicReconnectJob = ioScope.launch {
            periodicReconnect()
        }
    }

    private fun exitRoom() {
        sessionCode = null
        requester.sessionCode = ""
        sessionState.initialized = false
        sessionState.members = listOf()
        sessionState.castSongDto = null
        sessionState.currentScroll = null
        sessionState.chatMessages = listOf()
        streamSocket.close()
        periodicRefreshJob = null
        periodicReconnectJob = null
        lastSessionChange = 0
        lastSharedScroll = null
        logEvents.clear()
    }

    private suspend fun periodicRefresh() {
        var lastShot: Long = Date().time
        while (isInRoom()) {
            val interval: Long? = when {
                !activityController.isForeground -> null
                lastSessionChange == 0L -> 0
                streamSocket.ioSocket?.connected() == false -> (2000..3000).random().toLong()
                else -> {
                    val millis = Date().time - lastSessionChange
                    val fraction = millis.interpolate(0, 10 * 60_000) // 0-10 min -> 0-1
                    val penalty = (fraction * 2 * 60_000).toLong() // 0-1 -> 0-2 min
                    (2_000..3_000).random().toLong() + penalty
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
                    uiInfoService.showInfoAction(
                        R.string.songcast_reconnecting_to_room,
                        actionResId = R.string.songcast_action_lobby,
                        action = { showLobby() },
                    )
                    streamSocket.reconnect()
                    uiInfoService.clearSnackBars()
                } catch (e: Throwable) {
                    UiErrorHandler().handleContextError(e, R.string.songcast_connection_context)
                }
                lastShot = Date().time
            }
            delay(1_000)
        }
    }

    fun dropSessionAsync(): Deferred<Result<Unit>> {
        return requester.dropSessionAsync { exitRoom() }
    }

    private fun getSessionDetailsAsync(): Deferred<Result<CastSession>> {
        return requester.getSessionDetailsAsync { responseData ->
            sessionState.members = responseData.members
            sessionState.castSongDto = responseData.song
            this.ephemeralSong = buildEphemeralSong(responseData.song)
            sessionState.currentScroll = responseData.scroll
            sessionState.chatMessages = responseData.chat_messages
            sessionState.createdTime = responseData.create_timestamp
            sessionState.initialized = true
        }
    }

    private fun postSongPresentAsync(payload: CastSongSelected): Deferred<Result<Unit>> {
        return requester.postSongPresentAsync(payload) {}
    }

    fun postScrollControlAsync(payload: CastScroll): Deferred<Result<Unit>> {
        return requester.postScrollControlAsync(payload)
    }

    fun postChatMessageAsync(payload: CastChatMessageSent): Deferred<Result<Unit>> {
        return requester.postChatMessageAsync(payload)
    }

    fun promoteMemberAsync(memberPubId: String): Deferred<Result<Unit>> {
        return requester.promoteMemberAsync(memberPubId)
    }

    fun reportSongOpened(song: Song) {
        if (!isPresenter()) return
        // opening the song presented by someone else (or returning to the same one)
        if (song.namespace == SongNamespace.Ephemeral) return

        val castSongId: String = SongHasher().hashSong(song)
        val payload = CastSongSelected(
            id = castSongId,
            title = song.title,
            artist = song.artist,
            content = song.content.orEmpty(),
            chords_notation_id = song.chordsNotation.id,
        )
        sessionState.castSongDto = CastSong(
            id = payload.id,
            chosen_by = myMemberPublicId,
            title = payload.title,
            artist = payload.artist,
            content = payload.content,
            chords_notation_id = payload.chords_notation_id,
        )

        songPreviewLayoutController.addOnInitListener {
            defaultScope.launch {
                val result = postSongPresentAsync(payload).await()
                result.fold(onSuccess = {
                    scrollService.shareScrollControl()
                }, onFailure = { e ->
                    UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
                })
            }
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
        ephemeralSong?.let { ephemeralSong ->
            logEvents.add(
                SongLogEvent(
                    timestampMs = Date().time,
                    author = presenterName,
                    song = ephemeralSong,
                )
            )
        }
        uiInfoService.showInfoAction(
            R.string.songcast_song_selected, presenterName, songName,
            actionResId = R.string.songcast_action_open_song,
            action = { openPresentedSong() },
        )

        refreshUI()
        if (followsPresentedSong(presenter?.public_member_id)) {
            openPresentedSong()
        }
    }

    private fun followsPresentedSong(pubMemberId: String?): Boolean {
        return when {
            !clientOpenPresentedSongs -> false
            pubMemberId == null -> false
            pubMemberId == myMemberPublicId -> false
            songPreviewLayoutController.currentSong == ephemeralSong -> false // already opened
            else -> true
        }
    }

    fun openPresentedSong() {
        val ephemeralSongN = ephemeralSong ?: return
        defaultScope.launch {
            songOpener.openSongPreview(ephemeralSongN) {
                adaptToScrollControl()
            }
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
        ioScope.launch {
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
            refreshUI()
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
            if (castSongDto != null)
                onSongSelectedEventDto(castSongDto)
            return true
        }
        if (oldState.chatMessages != newState.chatMessages) {
            val newMessages = newState.chatMessages.minus(oldState.chatMessages.toSet())
            if (newMessages.isNotEmpty()) {
                val message = newMessages.last()
                if (message.author != this.myName) {
                    uiInfoService.showInfoAction(
                        R.string.songcast_new_chat_message, message.author, message.text,
                        actionResId = R.string.songcast_action_lobby,
                        action = { showLobby() },
                    )
                }
            }
            return true
        }
        if (oldState.members != newState.members) {
            val droppedMembers = oldState.members.toSet().minus(newState.members.toSet())
            droppedMembers.forEach { member ->
                uiInfoService.showInfoAction(
                    R.string.songcast_member_dropped, member.name,
                    actionResId = R.string.songcast_action_lobby,
                    action = { showLobby() },
                )
                addSystemLogEvent(R.string.songcast_member_dropped, member.name)
            }
            val newMembers = newState.members.toSet().minus(oldState.members.toSet())
            newMembers.forEach { member ->
                uiInfoService.showInfoAction(
                    R.string.songcast_new_member_joined, member.name,
                    actionResId = R.string.songcast_action_lobby,
                    action = { showLobby() },
                )
                addSystemLogEvent(R.string.songcast_new_member_joined, member.name)
            }
            return true
        }
        if (oldState.currentScroll != newState.currentScroll) {
            val scrollDto = newState.currentScroll
            scrollDto?.run {
                adaptToScrollControl()
            }
            return true
        }
        return false
    }

    fun showLobby() {
        layoutController.showLayout(SongCastLobbyLayout::class)
    }

    private fun adaptToScrollControl() {
        val scrollDto = sessionState.currentScroll ?: return
        val chordsNotation = ephemeralSong?.chordsNotation ?: ChordsNotation.default
        if (clientFollowScroll && !isPresenting() && layoutController.isState(SongPreviewLayoutController::class)) {
            logger.debug("scrolling by SongCast event: mode=${scrollDto.mode}, start=${scrollDto.view_start}")
            scrollService.adaptToScrollControl(
                scrollDto.view_start, scrollDto.view_end, scrollDto.visible_text, scrollDto.mode, chordsNotation,
            )
        }
    }

    private fun addSystemLogEvent(resourceId: Int, vararg args: Any?) {
        val text = uiInfoService.resString(resourceId, *args)
        logEvents.add(
            SystemLogEvent(timestampMs = Date().time, text = text)
        )
        refreshUI()
    }

    fun generateChatEvents(): List<LogEvent> {
        val allEvents = mutableListOf<LogEvent>()
        allEvents.addAll(logEvents)
        allEvents.addAll(sessionState.chatMessages.map {
            MessageLogEvent(
                timestampMs = it.timestamp * 1000,
                author = it.author,
                text = it.text,
            )
        })
        return allEvents.sortedBy { it.timestampMs }
    }

    private fun refreshUI() {
        mainScope.launch {
            onSessionUpdated()
        }
    }
}

data class SessionState(
    var initialized: Boolean = false,
    var members: List<CastMember> = listOf(),
    var castSongDto: CastSong? = null,
    var currentScroll: CastScroll? = null,
    var chatMessages: List<CastChatMessage> = listOf(),
    var createdTime: Long = 0, // in seconds
)
