package igrek.songbook.layout.songselection

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.domain.songsdb.SongsDb
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.layout.songpreview.SongPreviewLayoutController
import igrek.songbook.layout.songtree.SongTreeItem
import igrek.songbook.layout.songtree.SongTreeSorter
import igrek.songbook.layout.view.ButtonClickEffect
import igrek.songbook.persistence.SongsDbRepository
import javax.inject.Inject

abstract class SongSelectionLayoutController : OnSongClickListener {

    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController
    @Inject
    lateinit var songsDbRepository: SongsDbRepository
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var songPreviewLayoutController: Lazy<SongPreviewLayoutController>

    protected val logger: Logger = LoggerFactory.getLogger()
    protected var actionBar: ActionBar? = null
    protected var itemsListView: SongListView? = null

    fun initSongSelectionLayout(layout: View) {
        // Toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(false)
            actionBar!!.setDisplayShowHomeEnabled(false)
        }
        // navigation menu button
        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { _ -> navigationMenuController.navDrawerShow() }
        ButtonClickEffect.addClickEffect(navMenuButton)

        itemsListView = layout.findViewById(R.id.filesList)
    }

    open fun updateSongItemsList() {
        val items: List<SongTreeItem> = getSongItems(songsDbRepository.songsDb!!)
        SongTreeSorter().sort(items)
        itemsListView!!.setItems(items)
    }

    open fun getSongItems(songsDb: SongsDb): List<SongTreeItem> {
        return mutableListOf()
    }

    fun openSongPreview(item: SongTreeItem) {
        songPreviewLayoutController.get().setCurrentSong(item.song)
        layoutController.showSongPreview()
    }
}
