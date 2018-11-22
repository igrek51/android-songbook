package igrek.songbook.songselection.favourite

import android.os.Handler
import android.widget.EditText
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.model.songsdb.SongsDb
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongSearchItem
import igrek.songbook.songselection.SongTreeItem
import igrek.songbook.songselection.songsearch.SongSearchLayoutController
import igrek.songbook.songselection.songtree.SongTreeFilter
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class FavouritesLayoutController : SongSearchLayoutController(), MainLayout {

    private var searchFilterEdit: EditText? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemNameFilter: String? = null
    private var storedScroll: ListScrollPosition? = null

    @Inject
    lateinit var favouriteSongService: FavouriteSongService

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.FAVOURITE_SONGS
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.song_search
    }

    override fun updateSongItemsList() {
        super.updateSongItemsList()
        // restore Scroll Position
        if (storedScroll != null) {
            Handler().post { itemsListView?.restoreScrollPosition(storedScroll) }
        }
    }

    private fun setSongFilter(itemNameFilter: String?) {
        this.itemNameFilter = itemNameFilter
        if (itemNameFilter == null)
            searchFilterEdit?.setText("", TextView.BufferType.EDITABLE)
        // reset scroll
        storedScroll = null
        updateSongItemsList()
    }

    override fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        if (!isFilterSet()) { // no filter
            return favouriteSongService.getFavouriteSongs()
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .toMutableList()
        } else {
            val songNameFilter = SongTreeFilter(itemNameFilter)
            // filter songs
            val songsSequence = favouriteSongService.getFavouriteSongs()
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .filter { item -> songNameFilter.songMatchesNameFilter(item) }
            return songsSequence.toMutableList()
        }
    }

    private fun isFilterSet(): Boolean {
        return itemNameFilter != null && !itemNameFilter!!.isEmpty()
    }

    override fun onBackClicked() {
        if (isFilterSet()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
            layoutController.showSongTree()
        }
    }

    private fun onClearFilterClicked() {
        if (isFilterSet()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
        }
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
