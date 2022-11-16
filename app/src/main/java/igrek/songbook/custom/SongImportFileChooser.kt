package igrek.songbook.custom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import com.google.common.io.CharStreams
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import igrek.songbook.R
import igrek.songbook.activity.ActivityResultDispatcher
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.LocalizedError
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.util.capitalize
import igrek.songbook.util.limitTo
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


class SongImportFileChooser(
    activity: LazyInject<Activity> = appFactory.activity,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    editSongLayoutController: LazyInject<EditSongLayoutController> = appFactory.editSongLayoutController,
    activityResultDispatcher: LazyInject<ActivityResultDispatcher> = appFactory.activityResultDispatcher,
) {
    private val activity by LazyExtractor(activity)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val editSongLayoutController by LazyExtractor(editSongLayoutController)
    private val activityResultDispatcher by LazyExtractor(activityResultDispatcher)

    private var fileChooserLauncher: ActivityResultLauncher<Intent>? = null

    companion object {
        const val FILE_IMPORT_LIMIT_B = 100 * 1024 // 100 KiB
    }

    fun init() {
        fileChooserLauncher =
            activityResultDispatcher.registerActivityResultLauncher { resultCode: Int, data: Intent? ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        onFileSelect(data)
                    }
                    Activity.RESULT_CANCELED -> {
                        uiInfoService.showToast(R.string.file_select_operation_canceled)
                    }
                    else -> {
                        UiErrorHandler().handleError(RuntimeException("Unknown operation result"))
                    }
                }
            }
    }

    fun showFileChooser() {
        SafeExecutor {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"

            try {
                // DON'T USE: val activityIntent = Intent.createChooser(intent, title)
                fileChooserLauncher.let { fileChooserLauncher ->
                    if (fileChooserLauncher != null) {
                        fileChooserLauncher.launch(intent)

                    } else {
                        activityResultDispatcher.startActivityForResult(intent) { resultCode: Int, data: Intent? ->
                            when (resultCode) {
                                Activity.RESULT_OK -> {
                                    onFileSelect(data)
                                }
                                Activity.RESULT_CANCELED -> {
                                    uiInfoService.showToast(R.string.file_select_operation_canceled)
                                }
                                else -> {
                                    UiErrorHandler().handleError(RuntimeException("Unknown operation result"))
                                }
                            }
                        }
                    }
                }
            } catch (ex: android.content.ActivityNotFoundException) {
                uiInfoService.showToast(R.string.file_manager_not_found)
            }
        }
    }

    private fun onFileSelect(intent: Intent?) {
        val uri: Uri? = intent?.data
        SafeExecutor {
            if (uri != null) {
                val fileContent: String = extractFileContent(uri)

                val parsedContent: ParsedSongContent = parseSongContentMetadata(fileContent)
                val title = parsedContent.title
                    ?: File(getFileNameFromUri(uri)).nameWithoutExtension.capitalize()

                editSongLayoutController.setupImportedSong(
                    title,
                    parsedContent.content,
                    parsedContent.notation
                )
            }
        }
    }

    private fun extractFileContent(uri: Uri): String {
        val mimetype = activity.contentResolver.getType(uri)
        val filename = getFileNameFromUri(uri)
        val virtualFile = isVirtualFile(uri)

        return when {

            virtualFile && mimetype == "application/vnd.google-apps.document" -> { // Google Docs
                logger.info("Converting Google Docs file $filename -> PDF -> TXT")

                val targetMimeType = "application/pdf"
                val openableMimeTypes = activity.contentResolver.getStreamTypes(uri, targetMimeType)

                if (openableMimeTypes == null || openableMimeTypes.isEmpty()) {
                    throw RuntimeException("Stream type $targetMimeType is not available")
                }

                val inputStream = activity.contentResolver
                    .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)
                    ?.createInputStream() ?: throw RuntimeException("Can't open file descriptor")

                inputStream.use {
                    extractPhysicalFileContent(it, filename, targetMimeType)
                }
            }

            virtualFile -> {
                throw RuntimeException("Can't read from a virtual file")
            }

            else -> {
                val binaryGuess = isBinaryFile(uri)
                if (binaryGuess)
                    logger.warn("File seems to be binary")

                val inputStream = activity.contentResolver.openInputStream(uri)
                    ?: throw RuntimeException("can't open input stream for a content")
                inputStream.use {
                    extractPhysicalFileContent(it, filename, mimetype)
                }
            }
        }
    }

    private fun extractPhysicalFileContent(
        inputStream: InputStream,
        filename: String,
        mimetype: String?,
    ): String {
        val size = inputStream.available()
        logger.debug("Reading file: $filename, type: $mimetype, size: $size")

        if (size > FILE_IMPORT_LIMIT_B) {
            throw LocalizedError(R.string.selected_file_is_too_big)
        }

        val extension = File(filename).extension.lowercase()

        return when {
            mimetype == "application/pdf" || extension == "`pdf" -> {
                logger.info("extracting content from PDF file $filename")
                extractPdfContent(inputStream)
            }
            mimetype == "text/plain" || extension == "txt" -> {
                logger.info("reading content from text file $filename")
                extractTxtContent(inputStream)
            }
            else -> {
                val error = uiInfoService.resString(
                    R.string.error_song_file_type_unallowed,
                    "$extension ($mimetype)"
                )
                throw RuntimeException(error)
            }
        }
    }

    private fun extractPdfContent(inputStream: InputStream): String {
        PDFBoxResourceLoader.init(activity.applicationContext)
        val doc: PDDocument = PDDocument.load(inputStream)
//        val stripper: PDFTextStripper = object : PDFTextStripper() {
//            override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
//                super.writeString(text, textPositions)
//                logger.debug("""fragment: ${text}, textPositions: $textPositions""")
//            }
//        }
//        stripper.sortByPosition = true
//        stripper.startPage = 0
//        val dummy: Writer = OutputStreamWriter(ByteArrayOutputStream())
//        stripper.writeText(doc, dummy)
        val stripper = PDFTextStripper()
        return stripper.getText(doc).trimIndent().trim()
    }

    private fun extractTxtContent(inputStream: InputStream): String {
        return CharStreams.toString(InputStreamReader(inputStream, Charset.forName("UTF-8")))
    }

    private fun parseSongContentMetadata(fileContent: String): ParsedSongContent {
        var title: String? = null
        var notation: ChordsNotation? = null

        val titleRegex = Regex("""\{title: ?"?([\S\s]+?)"?\}""") // escape character \} is needed
        val notationRegex = Regex("""\{chords_notation: ?(\d+)\}""") // escape character \} is needed

        val allLines = fileContent.trim().lines()
        val firstLines = allLines.take(3)
        val lastLines = allLines.drop(3)

        val parsedFirstLines = firstLines.mapNotNull { line: String ->
            val trimmedLine = line.trim()
            titleRegex.matchEntire(trimmedLine)?.let { match ->
                title = match.groupValues[1].trim()
                return@mapNotNull null
            }
            notationRegex.matchEntire(trimmedLine)?.let { match ->
                val notationInt = match.groupValues[1].toLong()
                notation = ChordsNotation.parseById(notationInt)
                return@mapNotNull null
            }
            return@mapNotNull line
        }

        val parsedLines = parsedFirstLines + lastLines
        val parsedContent = parsedLines.joinToString("\n")
        return ParsedSongContent(parsedContent, title, notation)
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result.orEmpty()
    }

    private fun isBinaryFile(uri: Uri): Boolean {
        activity.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            val size: Int = inputStream.available().limitTo(1024)
            val data = ByteArray(size)
            inputStream.read(data)
            inputStream.close()
            var ascii = 0
            var other = 0
            for (i in data.indices) {
                val b = data[i]
                when {
                    b.toInt() == 0x09 || b.toInt() == 0x0A || b.toInt() == 0x0C || b.toInt() == 0x0D -> ascii++
                    b in 0x20..0x7E -> ascii++
                    else -> other++
                }
            }
            if (other == 0)
                return false
            return 100 * other / size > 95
        }
        return false
    }

    private fun isVirtualFile(uri: Uri): Boolean {
        if (!DocumentsContract.isDocumentUri(activity, uri))
            return false

        val cursor: Cursor = activity.contentResolver.query(
            uri, arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
            null, null, null
        ) ?: return false
        var flags = 0
        if (cursor.moveToFirst()) {
            flags = cursor.getInt(0)
        }
        cursor.close()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0
        } else {
            flags and (1 shl 9) != 0
        }
    }


    data class ParsedSongContent(
        val content: String,
        val title: String?,
        val notation: ChordsNotation?,
    )
}
