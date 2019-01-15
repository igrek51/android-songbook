package igrek.songbook.songselection.favourite

import android.os.Handler
import android.view.View
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.persistence.songsdb.SongsDb
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongSearchItem
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.SongTreeItem
import javax.inject.Inject

class FavouritesLayoutController : SongSelectionLayoutController(), MainLayout {

    @Inject
    lateinit var favouriteSongsRepository: FavouriteSongsRepository

    private var storedScroll: ListScrollPosition? = null
    private var emptyListLabel: TextView? = null

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        itemsListView!!.init(activity, this)
        updateSongItemsList()

        songsRepository.dbChangeSubject.subscribe {
            if (layoutController.isState(layoutState))
                updateSongItemsList()
        }
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.FAVOURITE_SONGS
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.favourite_songs
    }

    override fun updateSongItemsList() {
        super.updateSongItemsList()
        // restore Scroll Position
        if (storedScroll != null) {
            Handler().post { itemsListView?.restoreScrollPosition(storedScroll) }
        }

        if (itemsListView!!.count == 0) {
            emptyListLabel!!.visibility = View.VISIBLE
        } else {
            emptyListLabel!!.visibility = View.GONE
        }
    }

    override fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        // filter songs
        val songsSequence = favouriteSongsRepository.getFavouriteSongs()
                .asSequence()
                .map { song -> SongSearchItem.song(song) }
        return songsSequence.toMutableList()
    }

    override fun onBackClicked() {
        layoutController.showSongTree()
    }

    override fun onSongItemClick(item: SongTreeItem) {
        // store Scroll Position
        storedScroll = itemsListView?.currentScrollPosition
        super.onSongItemClick(item)
    }

    override fun onLayoutExit() {}
}
