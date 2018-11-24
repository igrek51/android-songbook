package igrek.songbook.songselection.favourite

import android.os.Handler
import android.view.View
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.model.songsdb.SongsDb
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongSearchItem
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.SongTreeItem
import javax.inject.Inject

class FavouritesLayoutController : SongSelectionLayoutController(), MainLayout {

    private var storedScroll: ListScrollPosition? = null

    @Inject
    lateinit var favouriteSongService: FavouriteSongService

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

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
    }

    override fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        // filter songs
        val songsSequence = favouriteSongService.getFavouriteSongs()
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
        if (item.isSong) {
            openSongPreview(item)
        }
    }

    override fun onLayoutExit() {}
}
