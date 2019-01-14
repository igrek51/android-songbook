package igrek.songbook.layout

import android.app.Activity
import android.support.design.widget.CoordinatorLayout
import android.view.ViewGroup
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.contact.ContactLayoutController
import igrek.songbook.custom.CustomSongEditLayoutController
import igrek.songbook.custom.CustomSongsLayoutController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.songsearch.SongSearchLayoutController
import igrek.songbook.songselection.songtree.SongTreeLayoutController
import javax.inject.Inject

class LayoutController {

    @Inject
    lateinit var songTreeController: Lazy<SongTreeLayoutController>
    @Inject
    lateinit var songSearchController: Lazy<SongSearchLayoutController>
    @Inject
    lateinit var songPreviewController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var contactLayoutController: Lazy<ContactLayoutController>
    @Inject
    lateinit var navigationMenuController: Lazy<NavigationMenuController>
    @Inject
    lateinit var settingsLayoutController: Lazy<SettingsLayoutController>
    @Inject
    lateinit var customSongEditLayoutController: Lazy<CustomSongEditLayoutController>
    @Inject
    lateinit var customSongsLayoutController: Lazy<CustomSongsLayoutController>
    @Inject
    lateinit var favouritesLayoutController: Lazy<FavouritesLayoutController>
    @Inject
    lateinit var activity: Activity

    private var mainContentLayout: CoordinatorLayout? = null
    private var previouslyShownLayout: MainLayout? = null
    private var currentlyShownLayout: MainLayout? = null
    private var lastSongSelectionLayout: MainLayout? = null

    private var state = LayoutState.SONGS_TREE

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun init() {
        activity.setContentView(R.layout.main_layout)
        mainContentLayout = activity.findViewById(R.id.main_content)
        navigationMenuController.get().init()
    }

    fun showSongTree() {
        showMainLayout(songTreeController.get())
        lastSongSelectionLayout = songTreeController.get()
    }

    fun showSongSearch() {
        showMainLayout(songSearchController.get())
        lastSongSelectionLayout = songSearchController.get()
    }

    fun showSongPreview() {
        showMainLayout(songPreviewController.get())
    }

    fun showContact() {
        showMainLayout(contactLayoutController.get())
    }

    fun showSettings() {
        showMainLayout(settingsLayoutController.get())
    }

    fun showEditSong() {
        showMainLayout(customSongEditLayoutController.get())
    }

    fun showCustomSongs() {
        showMainLayout(customSongsLayoutController.get())
        lastSongSelectionLayout = customSongsLayoutController.get()
    }

    fun showFavourites() {
        showMainLayout(favouritesLayoutController.get())
        lastSongSelectionLayout = favouritesLayoutController.get()
    }


    private fun showMainLayout(mainLayout: MainLayout) {
        // leave previous (current) layout
        if (currentlyShownLayout != null)
            currentlyShownLayout!!.onLayoutExit()

        previouslyShownLayout = currentlyShownLayout
        currentlyShownLayout = mainLayout

        val layoutResource = mainLayout.layoutResourceId
        state = mainLayout.layoutState

        // replace main content with brand new inflated layout
        mainContentLayout!!.removeAllViews()
        val inflater = activity.layoutInflater
        val layoutView = inflater.inflate(layoutResource, null)
        layoutView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mainContentLayout!!.addView(layoutView)

        mainLayout.showLayout(layoutView)
    }

    fun showPreviousLayout() {
        if (previouslyShownLayout != null) {
            showMainLayout(previouslyShownLayout!!)
        }
    }

    fun showLastSongSelectionLayout() {
        if (lastSongSelectionLayout != null) {
            showMainLayout(lastSongSelectionLayout!!)
        }
    }

    fun isState(compare: LayoutState): Boolean {
        return state == compare
    }

    fun onBackClicked() {
        if (navigationMenuController.get().isDrawerShown()) {
            navigationMenuController.get().navDrawerHide()
            return
        }
        currentlyShownLayout!!.onBackClicked()
    }
}
