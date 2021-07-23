package igrek.songbook.custom.share

import android.app.Activity
import android.content.Intent
import android.net.Uri
import igrek.songbook.activity.CopyToClipboardActivity
import igrek.songbook.custom.share.protos.SharedSong
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

    fun encodeSong(song: Song): String {
        val marsh = marshal(song)
        val bytes = gzip(marsh)
        return base64Encode(bytes)
    }

    fun decodeSong(encoded: String): Song {
        val bytes = base64Decode(encoded)
        val marsh = ungzip(bytes)
        return unmarshal(marsh)
    }

    fun marshal(song: Song): ByteArray {
        var customCategory = song.customCategoryName
        if (customCategory.isNullOrBlank()) {
            customCategory = song.displayCategories()
            if (customCategory.isEmpty()) customCategory = null
        }

        val dtoBuilder = SharedSong.SharedSongDto.newBuilder()

        dtoBuilder.content = song.content.orEmpty()
        dtoBuilder.title = song.title
        if (customCategory != null) {
            dtoBuilder.customCategory = customCategory
        }
        song.chordsNotation?.id?.let { chordsNotationId ->
            dtoBuilder.chordsNotation = chordsNotationId
        }

        return dtoBuilder.build().toByteArray()
    }

    fun unmarshal(bytes: ByteArray): Song {
        val dto: SharedSong.SharedSongDto = SharedSong.SharedSongDto.parseFrom(bytes)

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
            UiErrorHandler().handleError(RuntimeException("Invalid URL: ${t.message}", t))
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

fun gzip(bytes: ByteArray): ByteArray {
    val byteStream = ByteArrayOutputStream()
    val zipStream = GZIPOutputStream(byteStream)
    zipStream.write(bytes)
    zipStream.close()
    byteStream.close()
    return byteStream.toByteArray()
}

fun ungzip(content: ByteArray): ByteArray {
    val byteStream = content.inputStream()
    val zipStream = GZIPInputStream(byteStream)
    val bytes = zipStream.readBytes()
    zipStream.close()
    byteStream.close()
    return bytes
}

fun base64Encode(bytes: ByteArray): String {
    val encoded = Base64.encodeBase64String(bytes)
    // encode url
    return encoded
        .replace("+", ".")
        .replace("/", "-")
        .replace("=", "_")
}

fun base64Decode(str: String): ByteArray {
    // decode from url
    val encoded = str
        .replace(".", "+")
        .replace("-", "/")
        .replace("_", "=")
    return Base64.decodeBase64(encoded)
}

