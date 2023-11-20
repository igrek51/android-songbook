package igrek.songbook.cast

import igrek.songbook.system.HttpRequester
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.appFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class SongCastRequester {
    private val deviceIdProvider by LazyExtractor(appFactory.deviceIdProvider)

    private val logger: Logger = LoggerFactory.logger
    private val httpRequester = HttpRequester()
    var sessionCode: String = ""

    companion object {
        private const val songbookApiBase = "https://songbook.igrek.dev"
        private const val createSessionUrl = "$songbookApiBase/api/cast"
        private val joinSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/join" }
        private const val rejoinSessionUrl = "${songbookApiBase}/api/cast/rejoin"
        private val dropSessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session/drop" }
        private val sessionUrl = { session: String -> "${songbookApiBase}/api/cast/$session" }
        private val sessionSongUrl = { session: String -> "${songbookApiBase}/api/cast/$session/song" }
        private val sessionScrollUrl = { session: String -> "${songbookApiBase}/api/cast/$session/scroll" }
        private val sessionChatUrl = { session: String -> "${songbookApiBase}/api/cast/$session/chat" }
        private val promoteMemberUrl = { session: String, memberPubId: String ->
            "${songbookApiBase}/api/cast/$session/member/$memberPubId/promote"
        }
        private const val authDeviceHeader = "X-Songbook-Device-Id"
    }

    fun createSessionAsync(
        memberName: String,
        onSuccess: suspend (responseData: CastSessionJoined) -> Unit,
    ): Deferred<Result<CastSessionJoined>> {
        logger.info("Creating SongCast session by member '$memberName'...")
        val deviceId = deviceIdProvider.getDeviceId()
        val dto = CastSessionJoin(member_name = memberName)
        val json = httpRequester.jsonSerializer.encodeToString(CastSessionJoin.serializer(), dto)
        val request: Request = Request.Builder()
            .url(createSessionUrl)
            .header(authDeviceHeader, deviceId)
            .post(json.toRequestBody(httpRequester.jsonType))
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: CastSessionJoined =
                httpRequester.jsonSerializer.decodeFromString(
                    CastSessionJoined.serializer(),
                    jsonData
                )
            when (responseData.rejoined) {
                true -> logger.info("SongCast session rejoined: ${responseData.short_id}")
                false -> logger.info("SongCast session created: ${responseData.short_id}")
            }
            onSuccess(responseData)
            responseData
        }
    }

    fun joinSessionAsync(
        sessionCode: String,
        memberName: String,
        onSuccess: suspend (responseData: CastSessionJoined) -> Unit,
    ): Deferred<Result<CastSessionJoined>> {
        logger.info("Joining SongCast session by member '$memberName'...")
        val deviceId = deviceIdProvider.getDeviceId()
        val dto = CastSessionJoin(member_name = memberName)
        val json = httpRequester.jsonSerializer.encodeToString(CastSessionJoin.serializer(), dto)
        val request: Request = Request.Builder()
            .url(joinSessionUrl(sessionCode))
            .header(authDeviceHeader, deviceId)
            .post(json.toRequestBody(httpRequester.jsonType))
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: CastSessionJoined =
                httpRequester.jsonSerializer.decodeFromString(CastSessionJoined.serializer(), jsonData)
            when (responseData.rejoined) {
                true -> logger.info("SongCast session rejoined: ${responseData.short_id}")
                false -> logger.info("SongCast session joined: ${responseData.short_id}")
            }
            onSuccess(responseData)
            responseData
        }
    }

    fun restoreSessionAsync(
        onSuccess: suspend (responseData: CastSessionJoined) -> Unit,
    ): Deferred<Result<CastSessionJoined>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(rejoinSessionUrl)
            .header(authDeviceHeader, deviceId)
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: CastSessionJoined =
                httpRequester.jsonSerializer.decodeFromString(CastSessionJoined.serializer(), jsonData)
            logger.info("SongCast session restored: ${responseData.short_id}")
            onSuccess(responseData)
            responseData
        }
    }

    fun dropSessionAsync(
        onExit: () -> Unit,
    ): Deferred<Result<Unit>> {
        val oldSessionCode = sessionCode
        onExit()

        if (oldSessionCode.isEmpty()) {
            logger.warn("SongCast session not dropped - not joined")
            return GlobalScope.async { Result.success(Unit) }
        }
        logger.info("Dropping SongCast session: $oldSessionCode")
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(dropSessionUrl(oldSessionCode))
            .header(authDeviceHeader, deviceId)
            .post("".toRequestBody(null))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast session $oldSessionCode dropped")
        }
    }

    fun getSessionDetailsAsync(
        onSuccess: suspend (responseData: CastSession) -> Unit,
    ): Deferred<Result<CastSession>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(sessionUrl(sessionCode))
            .header(authDeviceHeader, deviceId)
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: CastSession =
                httpRequester.jsonSerializer.decodeFromString(
                    CastSession.serializer(),
                    jsonData
                )
            onSuccess(responseData)
            responseData
        }
    }

    fun postSongPresentAsync(
        payload: CastSongSelected,
        onSuccess: () -> Unit,
    ): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val json = httpRequester.jsonSerializer.encodeToString(CastSongSelected.serializer(), payload)
        val request: Request = Request.Builder()
            .url(sessionSongUrl(sessionCode))
            .header(authDeviceHeader, deviceId)
            .post(json.toRequestBody(httpRequester.jsonType))
            .build()
        return httpRequester.httpRequestAsync(request) {
            onSuccess()
            logger.info("SongCast: Song selection sent: ${payload.title} - ${payload.artist}")
        }
    }

    fun postScrollControlAsync(
        payload: CastScroll,
    ): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val json = httpRequester.jsonSerializer.encodeToString(CastScroll.serializer(), payload)
        val request: Request = Request.Builder()
            .url(sessionScrollUrl(sessionCode))
            .header(authDeviceHeader, deviceId)
            .post(json.toRequestBody(httpRequester.jsonType))
            .build()
        return httpRequester.httpRequestAsync(request) {}
    }

    fun postChatMessageAsync(
        payload: CastChatMessageSent,
    ): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val json = httpRequester.jsonSerializer.encodeToString(CastChatMessageSent.serializer(), payload)
        val request: Request = Request.Builder()
            .url(sessionChatUrl(sessionCode))
            .header(authDeviceHeader, deviceId)
            .post(json.toRequestBody(httpRequester.jsonType))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast: chat message sent: ${payload.text}")
        }
    }

    fun promoteMemberAsync(
        memberPubId: String,
    ): Deferred<Result<Unit>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val request: Request = Request.Builder()
            .url(promoteMemberUrl(sessionCode, memberPubId))
            .header(authDeviceHeader, deviceId)
            .post("".toRequestBody(null))
            .build()
        return httpRequester.httpRequestAsync(request) {
            logger.info("SongCast: member promoted: $memberPubId")
        }
    }
}
