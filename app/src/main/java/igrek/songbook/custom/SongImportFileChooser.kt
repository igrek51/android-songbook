package igrek.songbook.custom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.google.common.io.CharStreams
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import javax.inject.Inject

class SongImportFileChooser {

    private val logger = LoggerFactory.logger
    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var editSongLayoutController: Lazy<EditSongLayoutController>

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            val title = uiResourceService.resString(R.string.select_file_to_import)
            activity.startActivityForResult(Intent.createChooser(intent, title), FILE_SELECT_CODE)
        } catch (ex: android.content.ActivityNotFoundException) {
            uiInfoService.showToast(R.string.file_manager_not_found)
        }

    }

    fun onFileSelect(selectedUri: Uri?) {
        try {
            if (selectedUri != null) {
                val inputStream = activity.contentResolver
                        .openInputStream(selectedUri)
                val filename = getFileNameFromUri(selectedUri)

                val length = inputStream!!.available()
                if (length > 50 * 1024) {
                    uiInfoService.showToast(R.string.selected_file_is_too_big)
                    return
                }

                val content = convert(inputStream, Charset.forName("UTF-8"))

                editSongLayoutController.get().setSongFromFile(filename, content)
            }
        } catch (e: IOException) {
            logger.error(e)
        } catch (e: UnsupportedCharsetException) {
            logger.error(e)
        }

    }

    @Throws(IOException::class)
    private fun convert(inputStream: InputStream, charset: Charset): String {
        return CharStreams.toString(InputStreamReader(inputStream, charset))
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            activity.contentResolver.query(uri, null, null, null, null)!!.use { cursor ->
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
        return result!!
    }

    companion object {
        const val FILE_SELECT_CODE = 7
    }
}
