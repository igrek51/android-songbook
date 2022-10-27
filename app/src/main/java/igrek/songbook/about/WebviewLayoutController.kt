package igrek.songbook.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import igrek.songbook.R
import igrek.songbook.layout.InflatedLayout


class WebviewLayoutController : InflatedLayout(
    _layoutResourceId = R.layout.screen_webview
) {
    private var currentUrl: String = ""
    private val builtInWebview: Boolean = false

    fun openUrlUserGuide() {
        openUrl("https://igrek51.github.io/android-songbook/quick-guide/")
    }

    fun openUrlChordFormat() {
        openUrl("https://igrek51.github.io/android-songbook/chord-format/")
    }

    private fun openUrl(url: String) {
        currentUrl = url
        when (builtInWebview) {
            true -> {
                layoutController.showLayout(WebviewLayoutController::class)
            }
            false -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity.startActivity(browserIntent)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun showLayout(layout: View) {
        super.showLayout(layout)

        val webView = layout.findViewById<WebView>(R.id.webView1)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(currentUrl)
    }

}
