package igrek.songbook.songpreview.quickmenu

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.cast.SongCastService
import igrek.songbook.compose.AppTheme
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

// Singleton
class QuickMenuCast(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songCastService by LazyExtractor(songCastService)

    var isVisible = false
        set(visible) {
            field = visible
            quickMenuView?.let { quickMenuView ->
                if (visible) {
                    quickMenuView.visibility = View.VISIBLE
                } else {
                    quickMenuView.visibility = View.GONE
                }
            }
        }
    private var quickMenuView: View? = null

    fun setQuickMenuView(quickMenuView: View) {
        this.quickMenuView = quickMenuView

        val thisMenu = this
        quickMenuView.findViewById<ComposeView>(R.id.compose_quick_menu_cast).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    MainPage(thisMenu)
                }
            }
        }
    }
}

@Composable
private fun MainPage(controller: QuickMenuCast) {
    Column(Modifier.padding(8.dp)) {
        Text("Song Cast settings")
    }
}
