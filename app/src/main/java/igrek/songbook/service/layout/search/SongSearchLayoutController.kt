package igrek.songbook.service.layout.search

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.SongsDb
import igrek.songbook.service.layout.SongSelectionLayoutController
import igrek.songbook.service.songtree.SongTreeFilter
import igrek.songbook.service.songtree.SongTreeItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SongSearchLayoutController : SongSelectionLayoutController() {

    private var searchFilterEdit: EditText? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemNameFilter: String? = null

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        searchFilterEdit = layout.findViewById(R.id.searchFilterEdit)
        searchFilterEdit!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFilterSubject.onNext(s.toString())
            }
        })
        // refresh only after some inactive time
        searchFilterSubject.debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { _ -> setSongFilter() }

        songTreeWalker.goToAllSongs()
        itemsListView!!.init(activity, this)
        updateSongItemsList()

        restoreScrollPosition(null)
    }

    private fun setSongFilter() {
        itemNameFilter = searchFilterEdit!!.text.toString()
        scrollPosBuffer.storeScrollPosition(null, 0)
        updateSongItemsList()
    }

    override fun getSongItems(songsDb: SongsDb): List<SongTreeItem> {
        val songNameFilter = SongTreeFilter(itemNameFilter)
        return songsDb.allSongs
                .map { song -> SongTreeItem.song(song) }
                .filter { item -> songNameFilter.matchesNameFilter(item) }
    }

    fun onBackClicked() {
        activityController.get().quit()
    }

}
