package igrek.songbook.songselection.search

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import igrek.songbook.persistence.repository.AllSongsRepository
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.send.SendMessageService
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.listview.SongItemsContainer
import igrek.songbook.songselection.listview.SongListComposable
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.locale.StringSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class SongSearchLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    songTreeLayoutController: LazyInject<SongTreeLayoutController> = appFactory.songTreeLayoutController,
    sendMessageService: LazyInject<SendMessageService> = appFactory.sendMessageService,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_song_search
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songTreeLayoutController by LazyExtractor(songTreeLayoutController)
    private val sendMessageService by LazyExtractor(sendMessageService)
    private val preferencesState by LazyExtractor(settingsState)
    private val appLanguageService by LazyExtractor(appLanguageService)

    private var searchFilterEdit: EditText? = null
    private var emptySearchButton: Button? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemFilter: String? = null
    private var storedScroll: ListScrollPosition? = null
    private var subscriptions = mutableListOf<Disposable>()
    val state = SongSearchLayoutState()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        emptySearchButton = layout.findViewById<Button>(R.id.emptySearchButton)?.apply {
            setOnClickListener {
                sendMessageService.requestMissingSong()
            }
        }

        searchFilterEdit = layout.findViewById<EditText>(R.id.searchFilterEdit)?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchFilterSubject.onNext(s.toString())
                }
            })

            if (isFilterSet()) {
                setText(itemFilter, TextView.BufferType.EDITABLE)
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)
                    softKeyboardService.hideSoftKeyboard(this)
            }
            requestFocus()
            Handler(Looper.getMainLooper()).post {
                softKeyboardService.showSoftKeyboard(this)
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    clearFocus()
                    softKeyboardService.hideSoftKeyboard(this)
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        layout.findViewById<ImageButton>(R.id.searchFilterClearButton)?.run {
            setOnClickListener { onClearFilterClicked() }
        }

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
        // refresh only after some inactive time
        subscriptions.add(searchFilterSubject
            .debounce(400, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setSongFilter(searchFilterEdit?.text?.toString())
            }, UiErrorHandler::handleError)
        )
        subscriptions.add(songsRepository.dbChangeSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (layoutController.isState(this::class))
                    updateItemsList()
            }, UiErrorHandler::handleError)
        )
    }

    private fun updateItemsList() {
        val items: MutableList<SongTreeItem> = getSongItems(songsRepository.allSongsRepo)
        state.itemsContainer.replaceAll(items)

        emptySearchButton?.visibility = when (items.isEmpty()) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setSongFilter(itemNameFilter: String?) {
        this.itemFilter = itemNameFilter
        if (itemNameFilter == null)
            searchFilterEdit?.setText("", TextView.BufferType.EDITABLE)
        storedScroll = null
        updateItemsList()
    }

    private fun getSongItems(songsRepo: AllSongsRepository): MutableList<SongTreeItem> {
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null

        if (!isFilterSet()) { // no filter
            return songsRepo.songs.get()
                .filter { song -> song.language in acceptedLangCodes }
                .sortedBy { it.displayName().lowercase(StringSimplifier.locale) }
                .map { song -> SongSearchItem.song(song) }
                .toMutableList()
        } else {
            val songFilter =
                SongSearchFilter(itemFilter.orEmpty(), preferencesState.songLyricsSearch)
            // filter songs
            val songsSequence = songsRepo.songs.get()
                .filter { song -> song.language in acceptedLangCodes }
                .filter { song -> songFilter.matchSong(song) }
                .sortSongsByFilterRelevance(songFilter)
                .map { song -> SongSearchItem.song(song) }
            // filter categories
            val categoriesSequence = songsRepo.categories.get()
                .filter { category -> songFilter.matchCategory(category) }
                .map { category -> SongTreeItem.category(category) }
            // display union
            return categoriesSequence.plus(songsSequence)
                .toMutableList()
        }
    }

    private fun isFilterSet(): Boolean {
        if (itemFilter.isNullOrEmpty())
            return false
        return (itemFilter?.length ?: 0) >= 3
    }

    override fun onBackClicked() {
        if (isFilterSet()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    private fun onClearFilterClicked() {
        if (isFilterSet()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    fun openSongPreview(item: SongTreeItem) {
        item.song?.let {  song ->
            songOpener.openSongPreview(song)
        }
    }

    fun onItemClick(item: SongTreeItem) {
        val song = item.song
        if (song != null) {
            openSongPreview(item)
        } else {
            // move to selected category
            songTreeLayoutController.currentCategory = item.category
            layoutController.showLayout(SongTreeLayoutController::class)
        }
    }

    fun onItemMore(item: SongTreeItem) {
        val song = item.song
        if (song != null) {
            songContextMenuBuilder.showSongActions(item.song!!)
        } else {
            onItemClick(item)
        }
    }
}

class SongSearchLayoutState {
    val itemsContainer: SongItemsContainer = SongItemsContainer()
    val scrollState: LazyListState = LazyListState()
}

@Composable
private fun MainComponent(controller: SongSearchLayoutController) {
    Column {
        SongListComposable(
            controller.state.itemsContainer,
            scrollState = controller.state.scrollState,
            onItemClick = controller::onItemClick,
            onItemMore = controller::onItemMore,
        )
    }
}