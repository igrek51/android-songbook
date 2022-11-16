package igrek.songbook.custom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
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
        fileChooserLauncher = activityResultDispatcher.registerActivityResultLauncher { resultCode: Int, data: Intent? ->
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
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            try {
                // val title = uiResourceService.resString(R.string.select_file_to_import)
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
                val mimetype = activity.contentResolver.getType(uri)
                val binaryGuess = isBinaryFile(uri)
                if (binaryGuess)
                    logger.warn("File seems to be binary")

                activity.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                    val filename = getFileNameFromUri(uri)
                    val size = inputStream.available()

                    val content = extractFileContent(inputStream, filename, mimetype, size)
                    val title = File(filename).nameWithoutExtension.capitalize()

                    editSongLayoutController.setupImportedSong(title, content)
                }
            }
        }
    }

    private fun extractFileContent(inputStream: InputStream, filename: String, mimetype: String?, size: Int): String {
        logger.debug("Importing file $filename, type: $mimetype")

        if (size > FILE_IMPORT_LIMIT_B) {
            throw LocalizedError(R.string.selected_file_is_too_big)
        }

        val extension = File(filename).extension.lowercase()

        return when {
            mimetype == "application/pdf" || extension == "pdf" -> {
                logger.info("extracting content from PDF file $filename")
                extractPdfContent(inputStream)
            }
            else -> {
                CharStreams.toString(InputStreamReader(inputStream, Charset.forName("UTF-8")))
            }
        }
    }

    private fun extractPdfContent(inputStream: InputStream): String {
        PDFBoxResourceLoader.init(activity.applicationContext)
        val doc: PDDocument = PDDocument.load(inputStream)
        return PDFTextStripper().getText(doc).trimIndent().trim()
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
}
