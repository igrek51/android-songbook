package igrek.songbook.test

import android.content.Context
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.screenshot.ScreenCapture
import androidx.test.runner.screenshot.Screenshot
import java.io.File

class ScreenshotCapture {

    companion object {
        fun takeScreenshot(name: String) {
            val capture: ScreenCapture = Screenshot.capture()
            val bmp: Bitmap = capture.bitmap
            val trimTop = 0
            val trimBottom = when {
                bmp.width > bmp.height -> 0 // horizontal
                else -> 132
            }
            val cropped: Bitmap = Bitmap.createBitmap(bmp, 0, trimTop, bmp.width, bmp.height - trimTop - trimBottom)

            val testContext: Context = InstrumentationRegistry.getInstrumentation().context
            var packageName = testContext.packageName
            if (packageName.endsWith(".test"))
                packageName = packageName.removeSuffix(".test")

            val testDataDir = File("/storage/emulated/0/Android/data/${packageName}/files/screenshots")
            testDataDir.mkdirs()
            val screenshotFile = File(testDataDir, "${name}.png")

            writeBitmap(screenshotFile, cropped, Bitmap.CompressFormat.PNG, 90)
        }

        private fun writeBitmap(file: File, bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
            file.outputStream().use { out ->
                bitmap.compress(format, quality, out)
                out.flush()
            }
        }
    }

}