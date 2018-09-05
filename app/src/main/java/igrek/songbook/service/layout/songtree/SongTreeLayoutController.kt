package igrek.songbook.service.layout.songtree

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.exception.NoParentItemException
import igrek.songbook.logger.LoggerFactory
import igrek.songbook.service.activity.ActivityController
import igrek.songbook.service.info.UiInfoService
import igrek.songbook.service.info.UiResourceService
import igrek.songbook.service.layout.LayoutController
import igrek.songbook.service.layout.songpreview.SongPreviewLayoutController
import igrek.songbook.service.navmenu.NavigationMenuController
import igrek.songbook.service.persistence.SongsDbRepository
import igrek.songbook.service.songtree.ScrollPosBuffer
import igrek.songbook.service.songtree.SongTreeItem
import igrek.songbook.service.songtree.SongTreeWalker
import igrek.songbook.service.window.WindowManagerService
import igrek.songbook.view.songselection.SongListView
import javax.inject.Inject

class SongTreeLayoutController {

    @Inject
    lateinit var songTreeWalker: SongTreeWalker
    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var windowManagerService: WindowManagerService
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var scrollPosBuffer: ScrollPosBuffer
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var navigationMenuController: NavigationMenuController
    @Inject
    lateinit var songsDbRepository: SongsDbRepository
    @Inject
    lateinit var songPreviewLayoutController: Lazy<SongPreviewLayoutController>

    private val logger = LoggerFactory.getLogger()
    private var actionBar: ActionBar? = null
    private var itemsListView: SongListView? = null
    private var toolbarTitle: TextView? = null

    val currentScrollPos: Int?
        get() = itemsListView!!.currentScrollPosition

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showSongTree(layout: View) {
        songTreeWalker.goToAllCategories()

        // toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(false)
            actionBar!!.setDisplayShowHomeEnabled(false)
        }

        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { _ -> navigationMenuController.navDrawerShow() }

        val goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)
        goBackButton.setOnClickListener { _ -> onToolbarBackClickedEvent() }

        toolbarTitle = layout.findViewById(R.id.toolbarTitle)

        itemsListView = layout.findViewById(R.id.filesList)
        itemsListView!!.init(activity)

        updateItemsList()
    }

    fun updateItemsList() {
        songTreeWalker.updateItems(songsDbRepository.songsDb)
        itemsListView!!.setItems(songTreeWalker.currentItems)
        if (songTreeWalker.isCategorySelected) {
            setTitle(songTreeWalker.currentCategory.name)
        } else {
            setTitle("Songs")
        }

    }

    fun setTitle(title: String?) {
        actionBar!!.title = title
        toolbarTitle!!.text = title
    }

    fun scrollToItem(position: Int) {
        itemsListView!!.scrollTo(position)
    }

    fun scrollToPosition(y: Int) {
        itemsListView!!.scrollToPosition(y)
    }

    fun goUp() {
        try {
            songTreeWalker.goUp()
            updateItemsList()
            //restoreScrollPosition(songTreeWalker.currentPath)
        } catch (e: NoParentItemException) {
            activityController.get().quit()
        }
    }

    fun restoreScrollPosition(path: String) {
        val savedScrollPos = scrollPosBuffer.restoreScrollPosition(path)
        if (savedScrollPos != null) {
            scrollToPosition(savedScrollPos)
        }
    }

    fun onToolbarBackClickedEvent() {
        goUp()
    }

    fun onItemClickedEvent(item: SongTreeItem) {
        // TODO store / restore scroll position
        //scrollPosBuffer.storeScrollPosition(songTreeWalker.currentPath, currentScrollPos)
        if (item.isCategory) {
            songTreeWalker.goToCategory(item.category)
            updateItemsList()
            scrollToItem(0)
        } else {
            openSongPreview(item)
        }
    }

    private fun openSongPreview(item: SongTreeItem) {
        songPreviewLayoutController.get().currentSong = item.song
        layoutController.showSongPreview()
    }
}
