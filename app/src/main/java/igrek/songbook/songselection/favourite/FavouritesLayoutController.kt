package igrek.songbook.songselection.favourite

import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.SongItemsContainer
import igrek.songbook.songselection.listview.SongListComposable
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
) {
    private val favouriteSongsService by LazyExtractor(favouriteSongsService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)

    private var emptyListLabel: TextView? = null
    private val subscriptions = mutableListOf<Disposable>()
    val state = LayoutState()

    class LayoutState {
        val itemsContainer: SongItemsContainer = SongItemsContainer()
        val scrollState: LazyListState = LazyListState()
    }

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        updateItemsList()

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }

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
        val items = songsSequence.toList()
        state.itemsContainer.replaceAll(items)

        emptyListLabel?.visibility = when (items.isEmpty()) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    fun onItemClick(item: SongTreeItem) {
        item.song?.let {
            songOpener.openSongPreview(it)
        }
    }

    fun onItemMore(item: SongTreeItem) {
        if (item.isSong) {
            songContextMenuBuilder.showSongActions(item.song!!)
        }
    }
}

@Composable
private fun MainComponent(controller: FavouritesLayoutController) {
    Column {
        SongListComposable(
            controller.state.itemsContainer,
            scrollState = controller.state.scrollState,
            onItemClick = controller::onItemClick,
            onItemMore = controller::onItemMore,
        )
    }
}
