package igrek.songbook.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast

class CopyToClipboardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        if (uri != null) {
            copyTextToClipboard(uri.toString())
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun copyTextToClipboard(url: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("URL", url)
        clipboard.setPrimaryClip(clip)
    }
}