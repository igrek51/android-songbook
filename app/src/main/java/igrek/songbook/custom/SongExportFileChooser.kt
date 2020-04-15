package igrek.songbook.custom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import kotlinx.serialization.toUtf8Bytes
import java.io.OutputStream
import javax.inject.Inject

class SongExportFileChooser {

    @Inject
    lateinit var activity: Activity

    @Inject
    lateinit var uiInfoService: UiInfoService

    @Inject
    lateinit var uiResourceService: UiResourceService

    private var contentToBeSaved: String = ""
    private var onSuccess: (Uri) -> Unit = {}

    companion object {
        const val FILE_EXPORT_SELECT_CODE = 8
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun showFileChooser(contentToBeSaved: String, filename: String, onSuccess: (Uri) -> Unit) {
        this.contentToBeSaved = contentToBeSaved
        this.onSuccess = onSuccess
        SafeExecutor {
            try {
                val title = uiResourceService.resString(R.string.select_file_to_export)
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, filename)
                }
                activity.startActivityForResult(Intent.createChooser(intent, title), FILE_EXPORT_SELECT_CODE)

            } catch (ex: android.content.ActivityNotFoundException) {
                uiInfoService.showToast(R.string.file_manager_not_found)
            }
        }
    }

    fun onFileSelect(selectedUri: Uri?) {
        SafeExecutor {
            if (selectedUri != null) {
                activity.contentResolver.openOutputStream(selectedUri)?.use { outputStream: OutputStream ->
                    outputStream.write(contentToBeSaved.toUtf8Bytes())
                    onSuccess.invoke(selectedUri)
                }
            }
        }
    }

}
