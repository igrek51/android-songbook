package igrek.songbook.cast

import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.DeviceIdProvider
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.RequestBody


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
    private var myMemberPublicId: String = ""
    var sessionShortId: String? = null
        private set
    var members: List<CastMember> = listOf()
        private set
    var currentSong: CastSong? = null
        private set
    var currentScroll: CastScroll? = null
        private set
    var chatMessages: List<CastChatMessage> = listOf()
        private set

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

    private fun clearRoom() {
        sessionShortId = null
        members = listOf()
        currentSong = null
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
            logger.info("SongCast session created (or rejoined): ${responseData.short_id}")
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
            logger.info("SongCast session joined: ${responseData.short_id}")
            responseData
        }
    }

    fun dropSessionAsync(): Deferred<Result<Unit>> {
        if (sessionShortId == null) {
            logger.warn("SongCast session not dropped - not joined")
            return GlobalScope.async { Result.success(Unit) }
        }
        logger.info("Dropping SongCast session: $sessionShortId")
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(dropSessionUrl(sessionShortId ?: ""))
            .header(authDeviceHeader, deviceId)
            .post(RequestBody.create(null, ""))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast session $sessionShortId dropped")
            clearRoom()
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
            this.currentSong = responseData.song
            this.currentScroll = responseData.scroll
            this.chatMessages = responseData.chat_messages
            responseData
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
