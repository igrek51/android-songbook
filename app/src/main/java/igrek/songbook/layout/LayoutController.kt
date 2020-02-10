package igrek.songbook.layout

import android.app.Activity
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.contact.ContactLayoutController
import igrek.songbook.contact.MissingSongLayoutController
import igrek.songbook.contact.PublishSongLayoutController
import igrek.songbook.custom.CustomSongEditLayoutController
import igrek.songbook.custom.CustomSongsLayoutController
import igrek.songbook.custom.editor.ChordsEditorLayoutController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.history.OpenHistoryLayoutController
import igrek.songbook.songselection.latest.LatestSongsLayoutController
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.tree.SongTreeLayoutController
import javax.inject.Inject
import kotlin.reflect.KClass

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
    lateinit var chordsEditorLayoutController: Lazy<ChordsEditorLayoutController>
    @Inject
    lateinit var customSongsLayoutController: Lazy<CustomSongsLayoutController>
    @Inject
    lateinit var favouritesLayoutController: Lazy<FavouritesLayoutController>
    @Inject
    lateinit var playlistLayoutController: Lazy<PlaylistLayoutController>
    @Inject
    lateinit var latestSongsLayoutController: Lazy<LatestSongsLayoutController>
    @Inject
    lateinit var openHistoryLayoutController: Lazy<OpenHistoryLayoutController>
    @Inject
    lateinit var missingSongLayoutController: Lazy<MissingSongLayoutController>
    @Inject
    lateinit var publishSongLayoutController: Lazy<PublishSongLayoutController>
    @Inject
    lateinit var adminSongsLayoutContoller: Lazy<AdminSongsLayoutContoller>
    @Inject
    lateinit var activity: Activity

    private var mainContentLayout: CoordinatorLayout? = null
    private var previouslyShownLayout: MainLayout? = null
    private var currentlyShownLayout: MainLayout? = null
    private var lastSongSelectionLayout: MainLayout? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
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

    fun showCustomSong() {
        showMainLayout(customSongEditLayoutController.get())
    }

    fun showSongChordEditor() {
        showMainLayout(chordsEditorLayoutController.get())
    }

    fun showCustomSongs() {
        showMainLayout(customSongsLayoutController.get())
        lastSongSelectionLayout = customSongsLayoutController.get()
    }

    fun showFavourites() {
        showMainLayout(favouritesLayoutController.get())
        lastSongSelectionLayout = favouritesLayoutController.get()
    }

    fun showPlaylists() {
        showMainLayout(playlistLayoutController.get())
        lastSongSelectionLayout = playlistLayoutController.get()
    }

    fun showLatestSongs() {
        showMainLayout(latestSongsLayoutController.get())
        lastSongSelectionLayout = latestSongsLayoutController.get()
    }

    fun showOpenHistory() {
        showMainLayout(openHistoryLayoutController.get())
        lastSongSelectionLayout = openHistoryLayoutController.get()
    }

    fun showPublishSong() {
        showMainLayout(publishSongLayoutController.get())
    }

    fun showContactMissingSong() {
        showMainLayout(missingSongLayoutController.get())
    }

    fun showAdminAntechamberSongs() {
        showMainLayout(adminSongsLayoutContoller.get())
    }


    private fun showMainLayout(mainLayout: MainLayout) {
        // leave previous (current) layout
        if (currentlyShownLayout != null)
            currentlyShownLayout!!.onLayoutExit()

        previouslyShownLayout = currentlyShownLayout
        currentlyShownLayout = mainLayout

        val layoutResource = mainLayout.getLayoutResourceId()

        // replace main content with brand new inflated layout
        mainContentLayout!!.removeAllViews()
        val inflater = activity.layoutInflater
        val layoutView = inflater.inflate(layoutResource, null)
        layoutView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mainContentLayout!!.addView(layoutView)

        mainLayout.showLayout(layoutView)
    }

    fun showLastSongSelectionLayout() {
        if (lastSongSelectionLayout != null) {
            showMainLayout(lastSongSelectionLayout!!)
        }
    }

    fun isState(compareLayoutClass: KClass<out MainLayout>): Boolean {
        return compareLayoutClass.isInstance(currentlyShownLayout)
    }

    fun onBackClicked() {
        if (navigationMenuController.get().isDrawerShown()) {
            navigationMenuController.get().navDrawerHide()
            return
        }
        currentlyShownLayout!!.onBackClicked()
    }

}
