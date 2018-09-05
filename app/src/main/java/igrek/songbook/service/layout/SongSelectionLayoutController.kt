package igrek.songbook.service.layout

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongsDb
import igrek.songbook.logger.Logger
import igrek.songbook.logger.LoggerFactory
import igrek.songbook.service.activity.ActivityController
import igrek.songbook.service.info.UiResourceService
import igrek.songbook.service.layout.songpreview.SongPreviewLayoutController
import igrek.songbook.service.navmenu.NavigationMenuController
import igrek.songbook.service.persistence.SongsDbRepository
import igrek.songbook.service.songtree.ScrollPosBuffer
import igrek.songbook.service.songtree.SongTreeItem
import igrek.songbook.service.songtree.SongTreeSorter
import igrek.songbook.service.songtree.SongTreeWalker
import igrek.songbook.view.songselection.OnSongClickListener
import igrek.songbook.view.songselection.SongListView
import javax.inject.Inject

abstract class SongSelectionLayoutController : OnSongClickListener {

    @Inject
    lateinit var songTreeWalker: SongTreeWalker
    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var scrollPosBuffer: ScrollPosBuffer
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

        itemsListView = layout.findViewById(R.id.filesList)
    }

    open fun updateSongItemsList() {
        val items: List<SongTreeItem> = getSongItems(songsDbRepository.songsDb!!)
        SongTreeSorter().sort(items)
        songTreeWalker.currentItems = items
        itemsListView!!.setItems(items)
    }

    open fun getSongItems(songsDb: SongsDb): List<SongTreeItem> {
        return mutableListOf()
    }

    fun storeScrollPosition() {
        scrollPosBuffer.storeScrollPosition(songTreeWalker.currentCategory, itemsListView!!.currentScrollPosition)
    }

    fun restoreScrollPosition(category: SongCategory?) {
        val savedScrollPos = scrollPosBuffer.restoreScrollPosition(category)
        if (savedScrollPos != null) {
            itemsListView!!.scrollToPosition(savedScrollPos)
        }
    }

    fun openSongPreview(item: SongTreeItem) {
        songPreviewLayoutController.get().currentSong = item.song
        layoutController.showSongPreview()
    }

    override fun onSongItemClick(item: SongTreeItem) {
        storeScrollPosition()
        if (item.isCategory) {
            songTreeWalker.goToCategory(item.category)
            updateSongItemsList()
            // scroll to beginning
            itemsListView!!.scrollTo(0)
        } else {
            openSongPreview(item)
        }
    }
}
