package igrek.songbook.songselection.tree

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LocalFocusTraverser
import igrek.songbook.layout.spinner.MultiPicker
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.repository.AllSongsRepository
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.language.SongLanguage
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.listview.SongItemsContainer
import igrek.songbook.songselection.listview.SongListComposable
import igrek.songbook.songselection.listview.items.AbstractListItem
import igrek.songbook.songselection.listview.items.CategoryListItem
import igrek.songbook.songselection.listview.items.SongListItem
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.util.mainScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.launch

class SongTreeLayoutController(
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_song_tree
) {
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songOpener by LazyExtractor(songOpener)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)

    var currentCategory: Category? = null
    private var toolbarTitle: TextView? = null
    private var goBackButton: ImageButton? = null
    private var languagePicker: MultiPicker<SongLanguage>? = null
    private var composeView: ComposeView? = null
    private var subscriptions = mutableListOf<Disposable>()
    private var actionBar: ActionBar? = null
    private var storedScroll: ListScrollPosition? = null
    val state = SongTreeLayoutState()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        actionBar = activity.supportActionBar
        toolbarTitle = layout.findViewById(R.id.toolbarTitle)

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton?.setOnClickListener { onBackClicked() }

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

        updateItemsList()

        val thisLayout = this
        composeView = layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }

        restoreScrollPosition()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (layoutController.isState(this::class))
                    updateItemsList()
            }, UiErrorHandler::handleError)
        )

//        composeView?.setOnKeyListener { _, keyCode, event ->
//            if (event.action == KeyEvent.ACTION_DOWN) {
//                if (localFocus.handleKey(keyCode))
//                    return@setOnKeyListener true
//            }
//            return@setOnKeyListener false
//        }
    }

    override fun onBackClicked() {
        goUp()
    }

    private fun isCategorySelected(): Boolean {
        return currentCategory != null
    }

    private fun updateItemsList() {
        if (isCategorySelected()) { // reload current category
            currentCategory = songsRepository.allSongsRepo.categories.get()
                .firstOrNull { category -> category.id == currentCategory?.id }
        }

        val items: MutableList<AbstractListItem> = getSongItems()
        val sortedItems: List<AbstractListItem> = SongTreeSorter().sort(items)

        state.itemsContainer.replaceAll(sortedItems)

        if (isCategorySelected()) {
            goBackButton?.visibility = View.VISIBLE
            setTitle(currentCategory?.displayName)
        } else {
            goBackButton?.visibility = View.GONE
            setTitle(uiResourceService.resString(R.string.nav_categories_list))
        }
    }

    private fun getSongItems(): MutableList<AbstractListItem> {
        val songsRepo: AllSongsRepository = songsRepository.allSongsRepo
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null
        return if (isCategorySelected()) {
            // selected category
            currentCategory?.getUnlockedSongs().orEmpty()
                .filter { song -> song.language in acceptedLangCodes }
                .asSequence()
                .map { song -> SongListItem(song) }
                .toMutableList()
        } else {
            // all categories apart from custom
            songsRepo.publicCategories.get()
                .filter { category ->
                    category.songs.any { song -> song.language in acceptedLangCodes }
                }
                .asSequence()
                .map { category -> CategoryListItem(category) }
                .toMutableList()
        }
    }

    private fun setTitle(title: String?) {
        actionBar?.title = title
        toolbarTitle?.text = title
    }

    private fun goToSearchSong() {
        layoutController.showLayout(SongSearchLayoutController::class)
    }

    private fun goUp() {
        try {
            if (currentCategory == null)
                throw NoParentItemException()
            currentCategory = null

            updateItemsList()
            restoreScrollPosition()
        } catch (e: NoParentItemException) {
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    fun onItemClick(item: AbstractListItem) {
        if (item is CategoryListItem) {
            rememberScrollPosition()
            mainScope.launch {
                state.scrollState.scrollToItem(0, 0)
            }

            currentCategory = item.category
            updateItemsList()

            mainScope.launch {
                state.scrollState.scrollToItem(0, 0)
            }
        } else if (item is SongListItem) {
            songOpener.openSongPreview(item.song)
        }
    }

    fun onItemMore(item: AbstractListItem) {
        if (item is SongListItem) {
            songContextMenuBuilder.showSongActions(item.song)
        }
    }

    private fun rememberScrollPosition() {
        storedScroll = ListScrollPosition(
            state.scrollState.firstVisibleItemIndex,
            state.scrollState.firstVisibleItemScrollOffset,
        )
    }

    private fun restoreScrollPosition() {
        val storedScroll = storedScroll ?: return
        mainScope.launch {
            state.scrollState.scrollToItem(
                storedScroll.firstVisiblePosition,
                storedScroll.yOffsetPx,
            )
        }
        Handler(Looper.getMainLooper()).post {
            mainScope.launch {
                state.scrollState.scrollToItem(
                    storedScroll.firstVisiblePosition,
                    storedScroll.yOffsetPx,
                )
            }
        }
    }
}

class SongTreeLayoutState {
    val itemsContainer: SongItemsContainer = SongItemsContainer()
    val scrollState: LazyListState = LazyListState()
}

@Composable
private fun MainComponent(controller: SongTreeLayoutController) {
    Column {
        SongListComposable(
            controller.state.itemsContainer,
            scrollState = controller.state.scrollState,
            onItemClick = controller::onItemClick,
            onItemMore = controller::onItemMore,
        )
    }
}
