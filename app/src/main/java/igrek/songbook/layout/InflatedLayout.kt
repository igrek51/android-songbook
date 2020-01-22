package igrek.songbook.layout

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.navigation.NavigationMenuController
import javax.inject.Inject

open class InflatedLayout(
        private val _layoutResourceId: Int,
        private val _layoutState: LayoutState
) : MainLayout {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController

    protected val logger: Logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun getLayoutState(): LayoutState {
        return _layoutState
    }

    override fun getLayoutResourceId(): Int {
        return _layoutResourceId
    }

    override fun showLayout(layout: View) {
        setupToolbar(layout)
        setupNavigationMenu(layout)
    }

    private fun setupNavigationMenu(layout: View) {
        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }
    }

    private fun setupToolbar(layout: View) {
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }
    }

    override fun onBackClicked() {
        layoutController.showLastSongSelectionLayout()
    }

    override fun onLayoutExit() {}

    protected fun isLayoutVisible(): Boolean {
        return layoutController.isState(_layoutState)
    }
}