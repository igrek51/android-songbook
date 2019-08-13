package igrek.songbook.custom

import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.persistence.general.model.SongsDb
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class CustomSongsLayoutController : SongSelectionLayoutController(), MainLayout {

    @Inject
    lateinit var customSongService: Lazy<CustomSongService>

    private var storedScroll: ListScrollPosition? = null
    private var emptyListLabel: TextView? = null
    private var subscription: Disposable? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        val addCustomSongButton: ImageButton = layout.findViewById(R.id.addCustomSongButton)
        addCustomSongButton.setOnClickListener { addCustomSong() }

        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        itemsListView!!.init(activity, this)
        updateSongItemsList()

        subscription?.dispose()
        subscription = songsRepository.dbChangeSubject.subscribe {
            if (layoutController.isState(getLayoutState()))
                updateSongItemsList()
        }
    }

    private fun addCustomSong() {
        customSongService.get().showAddSongScreen()
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.CUSTOM_SONGS
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.custom_songs
    }

    override fun updateSongItemsList() {
        super.updateSongItemsList()
        // restore Scroll Position
        if (storedScroll != null) {
            Handler().post { itemsListView?.restoreScrollPosition(storedScroll) }
        }

        emptyListLabel!!.visibility = if (itemsListView!!.count == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        // filter songs
        val songsSequence = songsDb.customSongs.get()
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
