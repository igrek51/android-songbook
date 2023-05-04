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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.RequestBody
import java.util.Date


@OptIn(DelicateCoroutinesApi::class)
class SongCastService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    deviceIdProvider: LazyInject<DeviceIdProvider> = appFactory.deviceIdProvider,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val deviceIdProvider by LazyExtractor(deviceIdProvider)

    private val httpRequester = HttpRequester()
    private val logger: Logger = LoggerFactory.logger

    private var myName: String = ""
    var myMemberPublicId: String = ""
        private set
    var sessionShortId: String? = null
        private set
    var members: List<CastMember> = listOf()
        private set
    var castSongDto: CastSong? = null
        private set
    var ephemeralSong: Song? = null
        private set
    var currentScroll: CastScroll? = null
        private set
    var chatMessages: List<CastChatMessage> = listOf()
        private set

    val presenters: List<CastMember> get() = members.filter { it.type == CastMemberType.OWNER.value }
    val spectators: List<CastMember> get() = members.filter { it.type == CastMemberType.GUEST.value }
    val myMember: CastMember? get() = members.find { it.public_member_id == myMemberPublicId }

    companion object {
        private const val songbookApiBase = "https://songbook.igrek.dev"
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
