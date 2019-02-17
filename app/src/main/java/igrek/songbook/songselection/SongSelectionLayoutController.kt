package igrek.songbook.songselection

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.persistence.songsdb.SongsDb
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.songselection.tree.SongTreeSorter
import javax.inject.Inject

abstract class SongSelectionLayoutController : SongClickListener {

    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var songPreviewLayoutController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var songContextMenuBuilder: SongContextMenuBuilder

    protected val logger: Logger = LoggerFactory.logger
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
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }

        itemsListView = layout.findViewById(R.id.filesList)
    }

    open fun updateSongItemsList() {
        val items: MutableList<SongTreeItem> = getSongItems(songsRepository.songsDb!!)
        val sortedItems = SongTreeSorter().sort(items)
        itemsListView!!.setItems(sortedItems)
    }

    open fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        return mutableListOf()
    }

    fun openSongPreview(item: SongTreeItem) {
        songPreviewLayoutController.get().setCurrentSong(item.song)
        layoutController.showSongPreview()
    }

    override fun onSongItemClick(item: SongTreeItem) {
        if (item.isSong) {
            openSongPreview(item)
        }
    }

    override fun onSongItemLongClick(item: SongTreeItem) {
        if (item.isSong) {
            songContextMenuBuilder.showSongActions(item.song)
        } else {
            onSongItemClick(item)
        }
    }
}
