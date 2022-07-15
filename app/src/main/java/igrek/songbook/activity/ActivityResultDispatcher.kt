package igrek.songbook.activity

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import java.util.concurrent.atomic.AtomicInteger


class ActivityResultDispatcher(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
) {
    private val appCompatActivity by LazyExtractor(appCompatActivity)

    private val requestCodeSequence: AtomicInteger = AtomicInteger(10)
    private val requestCodeReactions: HashMap<Int, (resultCode: Int, data: Intent?) -> Unit> = hashMapOf()

    fun registerActivityResultLauncher(onResult: (resultCode: Int, data: Intent?) -> Unit): ActivityResultLauncher<Intent> {
        val activityResultLauncher: ActivityResultLauncher<Intent> =
            appCompatActivity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback<ActivityResult> { result ->
                    onResult(result.resultCode, result.data)
                })
        return activityResultLauncher
    }

    fun startRegisteredActivityForResult(intent: Intent, onResult: (resultCode: Int, data: Intent?) -> Unit) {
        val activityResultLauncher: ActivityResultLauncher<Intent> =
            appCompatActivity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback<ActivityResult> { result ->
                    onResult(result.resultCode, result.data)
                })
        activityResultLauncher.launch(intent)
    }

    @Suppress("DEPRECATION")
    fun startActivityForResult(intent: Intent, onResult: (resultCode: Int, data: Intent?) -> Unit) {
        val requestCode = requestCodeSequence.incrementAndGet()
        requestCodeReactions[requestCode] = onResult
        appCompatActivity.startActivityForResult(intent, requestCode)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        requestCodeReactions[requestCode]?.let { onResult ->
            onResult(resultCode, data)
        }
    }
}