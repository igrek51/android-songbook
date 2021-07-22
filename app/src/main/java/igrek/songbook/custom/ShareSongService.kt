package igrek.songbook.custom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import igrek.songbook.activity.CopyToClipboardActivity
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.songpreview.SongOpener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

open class ShareSongService(
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    activity: LazyInject<Activity> = appFactory.activity,
) {
    private val songOpener by LazyExtractor(songOpener)
    private val activity by LazyExtractor(activity)

    private val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    fun encodeSong(song: Song): String {
        val json = marshal(song)
        val bytes = gzip(json)
        return base64Encode(bytes)
    }

    fun decodeSong(encoded: String): Song {
        val bytes = base64Decode(encoded)
        val json = ungzip(bytes)
        return unmarshal(json)
    }

    fun marshal(song: Song): String {
        val dto = SharedSongDto(
            content = song.content.orEmpty(),
            title = song.title,
            customCategory = song.customCategoryName,
            chordsNotation = song.chordsNotation?.id,
        )
        return jsonSerializer.encodeToString(SharedSongDto.serializer(), dto)
    }

    fun unmarshal(str: String): Song {
        val dto: SharedSongDto = jsonSerializer.decodeFromString(SharedSongDto.serializer(), str)

        val now: Long = Date().time
        val chordsNotation = ChordsNotation.parseById(dto.chordsNotation) ?: ChordsNotation.default
        return Song(
            id = 0,
            title = dto.title,
            categories = mutableListOf(),
            content = dto.content,
            versionNumber = 1,
            createTime = now,
            updateTime = now,
            status = SongStatus.PUBLISHED,
            customCategoryName = dto.customCategory,
            chordsNotation = chordsNotation,
            namespace = SongNamespace.Ephemeral,
        )
    }

    fun openSharedEncodedSong(encodedSong: String) {
        logger.info("decoding shared song: $encodedSong")
        try {
            val song = decodeSong(encodedSong)
            song.let {
                GlobalScope.launch {
                    songOpener.openSongPreview(song)
                }
            }
        } catch (t: Throwable) {
            UiErrorHandler().handleError(RuntimeException("Invalid URL", t))
        }
    }

    private fun generateURL(song: Song): String {
        val base64 = encodeSong(song)
        return "https://songbookapp.page.link/song?d=$base64"
    }

    fun shareSong(song: Song) {
        val url = generateURL(song)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${song.displayName()}: $url")
            type = "text/plain"
        }

        val clipboardIntent =
            Intent(activity.applicationContext, CopyToClipboardActivity::class.java)
        clipboardIntent.data = Uri.parse(url)

        val chooserIntent = Intent.createChooser(shareIntent, "Share with")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(clipboardIntent))

        activity.startActivity(chooserIntent)
    }
}

fun gzip(content: String): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(content) }
    return bos.toByteArray()
}

fun ungzip(content: ByteArray): String {
    return GZIPInputStream(content.inputStream()).bufferedReader(Charsets.UTF_8)
        .use { it.readText() }
}

fun base64Encode(bytes: ByteArray): String {
    return Base64.encodeBase64String(bytes)
}

fun base64Decode(str: String): ByteArray {
    return Base64.decodeBase64(str)
}


@Serializable
data class SharedSongDto(
    val title: String,
    val content: String,
    val customCategory: String? = null,
    val chordsNotation: Long? = null,
)
