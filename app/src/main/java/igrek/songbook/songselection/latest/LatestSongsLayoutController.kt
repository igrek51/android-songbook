package igrek.songbook.songselection.latest


import android.view.View
import android.widget.ImageButton
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
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.SongItemsContainer
import igrek.songbook.songselection.listview.SongListComposable
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.listview.items.SongTreeItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class LatestSongsLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    songsUpdater: LazyInject<SongsUpdater> = appFactory.songsUpdater,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_latest_songs
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val songsUpdater by LazyExtractor(songsUpdater)

    private var subscriptions = mutableListOf<Disposable>()
    private val latestSongsCount = 200
    val state = LayoutState()

    class LayoutState {
        val itemsContainer: SongItemsContainer = SongItemsContainer()
        val scrollState: LazyListState = LazyListState()
    }

    override fun showLayout(layout: View) {
        super.showLayout(layout)

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

        layout.findViewById<ImageButton>(R.id.updateLatestSongs)?.let {
            it.setOnClickListener {
                songsUpdater.updateSongsDbAsync(forced = true)
            }
        }

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (isLayoutVisible())
                    updateItemsList()
            }, UiErrorHandler::handleError)
        )
    }

    private fun updateItemsList() {
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null
        val items = songsRepository.publicSongsRepo.songs.get()
            .asSequence()
            .filter { it.isPublic() }
            .filter { song -> song.language in acceptedLangCodes }
            .sortedBy { song -> -song.updateTime }
            .take(latestSongsCount)
            .map { song -> SongSearchItem.song(song) }
            .toList()
        state.itemsContainer.replaceAll(items)
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
private fun MainComponent(controller: LatestSongsLayoutController) {
    Column {
        SongListComposable(
            controller.state.itemsContainer,
            scrollState = controller.state.scrollState,
            onItemClick = controller::onItemClick,
            onItemMore = controller::onItemMore,
        )
    }
}
