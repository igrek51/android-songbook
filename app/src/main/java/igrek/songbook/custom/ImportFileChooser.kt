package igrek.songbook.custom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.google.common.io.CharStreams
import igrek.songbook.R
import igrek.songbook.activity.ActivityResultDispatcher
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class ImportFileChooser(
    activity: LazyInject<Activity> = appFactory.activity,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    activityResultDispatcher: LazyInject<ActivityResultDispatcher> = appFactory.activityResultDispatcher,
) {
    private val activity by LazyExtractor(activity)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val activityResultDispatcher by LazyExtractor(activityResultDispatcher)

    fun importFile(sizeLimit: Int? = null, onLoad: (content: String, filename: String) -> Unit) {
        safeExecute {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            try {
                activityResultDispatcher.startActivityForResult(intent) { resultCode: Int, data: Intent? ->
                    if (resultCode == Activity.RESULT_OK) {
                        onFileSelect(data?.data, onLoad, sizeLimit)
                    }
                }
            } catch (ex: android.content.ActivityNotFoundException) {
                uiInfoService.showToast(R.string.file_manager_not_found)
            }
        }
    }

    private fun onFileSelect(
        selectedUri: Uri?,
        onLoad: (content: String, filename: String) -> Unit,
        sizeLimit: Int?,
    ) {
        safeExecute {
            if (selectedUri != null) {
                activity.contentResolver.openInputStream(selectedUri)
                    ?.use { inputStream: InputStream ->
                        val filename = getFileNameFromUri(selectedUri)

                        val length = inputStream.available()
                        sizeLimit?.let { sizeLimit ->
                            if (length > sizeLimit) {
                                uiInfoService.showToast(R.string.selected_file_is_too_big)
                                return@safeExecute
                            }
                        }

                        val content = convert(inputStream, Charset.forName("UTF-8"))
                        onLoad(content, filename)
                    }
            }
        }
    }

    @Throws(IOException::class)
    private fun convert(inputStream: InputStream, charset: Charset): String {
        return CharStreams.toString(InputStreamReader(inputStream, charset))
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
}
