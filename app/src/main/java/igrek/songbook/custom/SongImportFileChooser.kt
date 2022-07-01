package igrek.songbook.custom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import com.google.common.io.CharStreams
import igrek.songbook.R
import igrek.songbook.activity.ActivityResultDispatcher
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.system.PermissionService
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class SongImportFileChooser(
    activity: LazyInject<Activity> = appFactory.activity,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    editSongLayoutController: LazyInject<EditSongLayoutController> = appFactory.editSongLayoutController,
    activityResultDispatcher: LazyInject<ActivityResultDispatcher> = appFactory.activityResultDispatcher,
) {
    private val activity by LazyExtractor(activity)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val editSongLayoutController by LazyExtractor(editSongLayoutController)
    private val activityResultDispatcher by LazyExtractor(activityResultDispatcher)

    private var fileChooserLauncher: ActivityResultLauncher<Intent>? = null

    companion object {
        const val FILE_IMPORT_LIMIT_B = 50 * 1024
    }

    fun init() {
        fileChooserLauncher = activityResultDispatcher.registerActivityResultLauncher { resultCode: Int, data: Intent? ->
            when (resultCode) {
                Activity.RESULT_OK -> {
                    onFileSelect(data?.data)
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
                        activityResultDispatcher.startOldActivityForResult(intent) { resultCode: Int, data: Intent? ->
                            when (resultCode) {
                                Activity.RESULT_OK -> {
                                    onFileSelect(data?.data)
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

    private fun onFileSelect(selectedUri: Uri?) {
        SafeExecutor {
            if (selectedUri != null) {
                activity.contentResolver.openInputStream(selectedUri)?.use { inputStream: InputStream ->
                    val filename = getFileNameFromUri(selectedUri)

                    val length = inputStream.available()
                    if (length > FILE_IMPORT_LIMIT_B) {
                        uiInfoService.showToast(R.string.selected_file_is_too_big)
                        return@SafeExecutor
                    }

                    val content = convert(inputStream, Charset.forName("UTF-8"))

                    editSongLayoutController.setupImportedSong(filename, content)
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
