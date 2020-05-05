package igrek.songbook.layout

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.songbook.R
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.navigation.NavigationMenuController

open class InflatedLayout(
        private val _layoutResourceId: Int,
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
        navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
) : MainLayout {
    protected val layoutController by LazyExtractor(layoutController)
    protected val activity by LazyExtractor(appCompatActivity)
    private val navigationMenuController by LazyExtractor(navigationMenuController)

    protected val logger: Logger = LoggerFactory.logger

    override fun getLayoutResourceId(): Int {
        return _layoutResourceId
    }

    override fun showLayout(layout: View) {
        setupToolbar(layout)
        setupNavigationMenu(layout)
    }

    private fun setupNavigationMenu(layout: View) {
        layout.findViewById<ImageButton>(R.id.navMenuButton)?.run {
            setOnClickListener { navigationMenuController.navDrawerShow() }
        }
    }

    private fun setupToolbar(layout: View) {
        layout.findViewById<Toolbar>(R.id.toolbar1)?.let { toolbar ->
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.run {
                setDisplayHomeAsUpEnabled(false)
                setDisplayShowHomeEnabled(false)
            }
        }
    }

    override fun onBackClicked() {
        layoutController.showPreviousLayoutOrQuit()
    }

    override fun onLayoutExit() {}

    protected fun isLayoutVisible(): Boolean {
        return layoutController.isState(this::class)
    }
}