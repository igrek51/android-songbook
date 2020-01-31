package igrek.songbook.songselection.favourite

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.MainLayout
import igrek.songbook.persistence.general.model.SongsDb
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class FavouritesLayoutController : SongSelectionLayoutController(), MainLayout {

    @Inject
    lateinit var favouriteSongsService: FavouriteSongsService

    private var storedScroll: ListScrollPosition? = null
    private var emptyListLabel: TextView? = null

    private val subscriptions = mutableListOf<Disposable>()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        itemsListView!!.init(activity, this)
        updateSongItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (layoutController.isState(this::class))
                        updateSongItemsList()
                })
        subscriptions.add(favouriteSongsService.updateFavouriteSongSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (layoutController.isState(this::class))
                        updateSongItemsList()
                })
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_favourite_songs
    }

    override fun updateSongItemsList() {
        super.updateSongItemsList()
        // restore Scroll Position
        if (storedScroll != null) {
            Handler(Looper.getMainLooper()).post {
                itemsListView?.restoreScrollPosition(storedScroll)
            }
        }

        if (itemsListView!!.count == 0) {
            emptyListLabel!!.visibility = View.VISIBLE
        } else {
            emptyListLabel!!.visibility = View.GONE
        }
    }

    override fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        // filter songs
        val songsSequence = favouriteSongsService.getFavouriteSongs()
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
