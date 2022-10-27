package igrek.songbook.about

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import igrek.songbook.R
import igrek.songbook.layout.InflatedLayout


class WebviewLayoutController(
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_webview
) {
    private var currentUrl: String = ""


    fun openUrl(url: String) {
        currentUrl = url
        layoutController.showLayout(WebviewLayoutController::class)
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
