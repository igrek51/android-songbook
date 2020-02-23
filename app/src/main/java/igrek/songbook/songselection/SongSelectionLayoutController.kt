package igrek.songbook.songselection

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.repository.AllSongsRepository
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongOpener
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
    lateinit var songContextMenuBuilder: SongContextMenuBuilder
    @Inject
    lateinit var songOpener: SongOpener

    protected val logger: Logger = LoggerFactory.logger
    protected var actionBar: ActionBar? = null
    protected var itemsListView: SongListView? = null

    fun initSongSelectionLayout(layout: View) {
        // Toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        if (toolbar1 != null) {
            activity.setSupportActionBar(toolbar1)
            actionBar = activity.supportActionBar
            if (actionBar != null) {
                actionBar!!.setDisplayHomeAsUpEnabled(false)
                actionBar!!.setDisplayShowHomeEnabled(false)
            }
            // navigation menu button
            val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
            navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }
        }

        itemsListView = layout.findViewById(R.id.filesList)
    }

    open fun updateSongItemsList() {
        val items: MutableList<SongTreeItem> = getSongItems(songsRepository.allSongsRepo)
        val sortedItems = SongTreeSorter().sort(items)
        itemsListView!!.setItems(sortedItems)
    }

    open fun getSongItems(songsRepo: AllSongsRepository): MutableList<SongTreeItem> {
        return mutableListOf()
    }

    fun openSongPreview(item: SongTreeItem) {
        songOpener.openSongPreview(item.song!!)
    }

    override fun onSongItemClick(item: SongTreeItem) {
        if (item.isSong) {
            openSongPreview(item)
        }
    }

    override fun onSongItemLongClick(item: SongTreeItem) {
        if (item.isSong) {
            songContextMenuBuilder.showSongActions(item.song!!)
        } else {
            onSongItemClick(item)
        }
    }
}
