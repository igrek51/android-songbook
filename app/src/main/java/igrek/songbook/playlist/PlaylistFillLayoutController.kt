package igrek.songbook.playlist

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.repository.AllSongsRepository
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.songselection.listview.PostButtonComposable
import igrek.songbook.songselection.listview.SongItemsContainer
import igrek.songbook.songselection.listview.SongListComposable
import igrek.songbook.songselection.listview.items.AbstractListItem
import igrek.songbook.songselection.listview.items.SongListItem
import igrek.songbook.songselection.search.SongSearchFilter
import igrek.songbook.songselection.search.sortSongsByFilterRelevance
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.locale.StringSimplifier
import igrek.songbook.util.mainScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class PlaylistFillLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_playlist_fill
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val preferencesState by LazyExtractor(settingsState)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val playlistService by LazyExtractor(playlistService)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private var searchFilterEdit: EditText? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemFilter: String? = null
    private var subscriptions = mutableListOf<Disposable>()
    val state = LayoutState()

    class LayoutState {
        val itemsContainer: SongItemsContainer = SongItemsContainer()
        val scrollState: LazyListState = LazyListState()
    }

    override fun showLayout(layout: View) {
        super.showLayout(layout)

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

        uiInfoService.showInfo(R.string.playlist_fill_search_song_to_add)

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
        val items: MutableList<AbstractListItem> = getSongItems(songsRepository.allSongsRepo)
        state.itemsContainer.replaceAll(items)
    }

    private fun setSongFilter(itemNameFilter: String?) {
        this.itemFilter = itemNameFilter
        if (itemNameFilter == null)
            searchFilterEdit?.setText("", TextView.BufferType.EDITABLE)
        updateItemsList()
    }

    private fun getSongItems(songsRepo: AllSongsRepository): MutableList<AbstractListItem> {
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null

        return if (isFilterSet()) {
            val songFilter = SongSearchFilter(itemFilter.orEmpty(), preferencesState.songLyricsSearch)
            songsRepo.songs.get()
                .filter { song -> song.language in acceptedLangCodes }
                .filter { song -> songFilter.matchSong(song) }
                .sortSongsByFilterRelevance(songFilter)
                .map { song -> PlaylistFillItem(song) }
                .toMutableList()
        } else {
            songsRepo.songs.get()
                .filter { song -> song.language in acceptedLangCodes }
                .sortedBy { it.displayName().lowercase(StringSimplifier.locale) }
                .map { song -> PlaylistFillItem(song) }
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

    fun onItemClick(item: AbstractListItem) {
        if (item is SongListItem) {
            playlistService.toggleSongInCurrentPlaylist(item.song)
            state.itemsContainer.notifyItemChange(item)
        }
    }
}

@Composable
private fun MainComponent(controller: PlaylistFillLayoutController) {
    Column {
        SongListComposable(
            controller.state.itemsContainer,
            scrollState = controller.state.scrollState,
            onItemClick = controller::onItemClick,
            onItemMore = null,
            postButtonContent = { item, onItemClick, onItemMore ->
                PlaylistFillItemPostButtonComposable(item, onItemClick, onItemMore)
            },
        )
    }
}

@Composable
fun PlaylistFillItemPostButtonComposable(
    item: AbstractListItem,
    onItemClick: (item: AbstractListItem) -> Unit,
    onItemMore: ((item: AbstractListItem) -> Unit)? = null,
) {
    val item2 = item as? PlaylistFillItem ?: return
    val playlistService: PlaylistService = appFactory.playlistService.get()
    if (!playlistService.isSongOnCurrentPlaylist(item2.song)) {
        IconButton(
            modifier = Modifier.padding(0.dp).size(40.dp, 40.dp),
            onClick = {
                mainScope.launch {
                    onItemClick(item2)
                }
            },
        ) {
            Icon(
                painterResource(id = R.drawable.add),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }
    }
}