package igrek.songbook.songselection.top

import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.spinner.MultiPicker
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.language.SongLanguage
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.SongItemsContainer
import igrek.songbook.songselection.listview.SongListComposable
import igrek.songbook.songselection.listview.items.AbstractListItem
import igrek.songbook.songselection.listview.items.SongListItem
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.util.mainScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.launch

class TopSongsLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_top_songs
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val appLanguageService by LazyExtractor(appLanguageService)

    private var languagePicker: MultiPicker<SongLanguage>? = null
    private var loadingBar: ProgressBar? = null
    private var subscriptions = mutableListOf<Disposable>()
    private val topSongsCount = 500

    val state = LayoutState()

    class LayoutState {
        val itemsContainer: SongItemsContainer = SongItemsContainer()
        val scrollState: LazyListState = LazyListState()
    }

    override fun showLayout(layout: View) {
        initLayout(layout)
        updateLayout()
    }

    private fun initLayout(layout: View) {
        super.showLayout(layout)
        loadingBar = layout.findViewById(R.id.loadingBar)

        layout.findViewById<ImageButton>(R.id.searchSongButton)?.run {
            setOnClickListener { goToSearchSong() }
        }

        layout.findViewById<ImageButton>(R.id.languageFilterButton)?.apply {
            val songLanguageEntries = appLanguageService.songLanguageEntries()
            val selected = appLanguageService.selectedSongLanguages
            val title = uiResourceService.resString(R.string.song_languages)
            languagePicker = MultiPicker(
                activity,
                entityNames = songLanguageEntries,
                selected = selected,
                title = title,
            ) { selectedLanguages ->
                if (appLanguageService.selectedSongLanguages != selectedLanguages) {
                    appLanguageService.selectedSongLanguages = selectedLanguages.toSet()
                    updateItemsList()
                }
            }
            setOnClickListener { languagePicker?.showChoiceDialog() }
        }

        val thisLayout = this
        mainScope.launch {
            layout.findViewById<ComposeView>(R.id.compose_view).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    AppTheme {
                        MainComponent(thisLayout)
                    }
                }
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

    private fun updateLayout() {
        updateItemsList()
    }

    private fun goToSearchSong() {
        layoutController.showLayout(SongSearchLayoutController::class)
    }

    private fun updateItemsList() {
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null
        val latestSongs = songsRepository.publicSongsRepo.songs.get()
            .asSequence()
            .filter { it.isPublic() }
            .filter { song -> song.language in acceptedLangCodes }
            .filter { song -> song.rank != null }
            .sortedWith(compareBy({ -(it.rank ?: 0.0) }, { -it.updateTime }))
            .take(topSongsCount)
            .map { song -> SongListItem(song) }
            .toList()

        state.itemsContainer.replaceAll(latestSongs)
    }

    fun onItemClick(item: AbstractListItem) {
        if (item is SongListItem) {
            songOpener.openSongPreview(item.song)
        }
    }

    fun onItemMore(item: AbstractListItem) {
        if (item is SongListItem) {
            songContextMenuBuilder.showSongActions(item.song)
        }
    }

    fun stopLoading() {
        loadingBar?.visibility = View.GONE
    }
}

@Composable
private fun MainComponent(controller: TopSongsLayoutController) {
    Column {
        SongListComposable(
            controller.state.itemsContainer,
            scrollState = controller.state.scrollState,
            onItemClick = controller::onItemClick,
            onItemMore = controller::onItemMore,
        )
    }

    LaunchedEffect(Unit) {
        controller.stopLoading()
    }
}
