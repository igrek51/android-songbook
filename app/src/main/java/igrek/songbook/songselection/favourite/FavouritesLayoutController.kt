package igrek.songbook.songselection.favourite

import android.view.View
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.LazySongListView
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class FavouritesLayoutController(
    favouriteSongsService: LazyInject<FavouriteSongsService> = appFactory.favouriteSongsService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_favourite_songs
), SongClickListener {
    private val favouriteSongsService by LazyExtractor(favouriteSongsService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)

    private var itemsListView: LazySongListView? = null
    private var storedScroll: ListScrollPosition? = null
    private var emptyListLabel: TextView? = null
    private val subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        itemsListView = layout.findViewById<LazySongListView>(R.id.itemsList)?.also {
            it.init(activity, this, songContextMenuBuilder)
        }
        updateItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(
            songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (isLayoutVisible())
                        updateItemsList()
                }, UiErrorHandler::handleError)
        )
        subscriptions.add(
            favouriteSongsService.updateFavouriteSongSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (isLayoutVisible())
                        updateItemsList()
                }, UiErrorHandler::handleError)
        )
    }

    private fun updateItemsList() {
        val songsSequence = favouriteSongsService.getFavouriteSongs()
            .asSequence()
            .map { song -> SongSearchItem.song(song) }

        itemsListView?.setItems(songsSequence.toList())

        if (storedScroll != null) {
            itemsListView?.restoreScrollPosition(storedScroll)
        }

        if (itemsListView!!.count == 0) {
            emptyListLabel!!.visibility = View.VISIBLE
        } else {
            emptyListLabel!!.visibility = View.GONE
        }
    }

    override fun onSongItemClick(item: SongTreeItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.isSong) {
            songOpener.openSongPreview(item.song!!)
        }
    }

    override fun onSongItemLongClick(item: SongTreeItem) {
        if (item.isSong) {
            songContextMenuBuilder.showSongActions(item.song!!)
        }
    }
}
