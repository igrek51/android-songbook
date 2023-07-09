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
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.repository.AllSongsRepository
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.LazySongListView
import igrek.songbook.songselection.search.SongSearchFilter
import igrek.songbook.songselection.search.sortSongsByFilterRelevance
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.locale.StringSimplifier
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class PlaylistFillLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_playlist_fill
), SongClickListener {
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val playlistService by LazyExtractor(playlistService)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private var itemsListView: LazySongListView? = null
    private var searchFilterEdit: EditText? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemFilter: String? = null
    private var subscriptions = mutableListOf<Disposable>()

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

        itemsListView = layout.findViewById<LazySongListView>(R.id.itemsList)?.also {
            it.init(activity, this, songContextMenuBuilder)
        }
        updateItemsList()

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
        val items: MutableList<SongTreeItem> = getSongItems(songsRepository.allSongsRepo)
        itemsListView?.setItems(items)
    }

    private fun setSongFilter(itemNameFilter: String?) {
        this.itemFilter = itemNameFilter
        if (itemNameFilter == null)
            searchFilterEdit?.setText("", TextView.BufferType.EDITABLE)
        updateItemsList()
    }

    private fun getSongItems(songsRepo: AllSongsRepository): MutableList<SongTreeItem> {
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null

        return if (isFilterSet()) {
            val songFilter = SongSearchFilter(itemFilter.orEmpty(), preferencesState.songLyricsSearch)
            songsRepo.songs.get()
                .filter { song -> song.language in acceptedLangCodes }
                .filter { song -> songFilter.matchSong(song) }
                .sortSongsByFilterRelevance(songFilter)
                .map { song -> PlaylistFillItem.song(song) }
                .toMutableList()
        } else {
            songsRepo.songs.get()
                .filter { song -> song.language in acceptedLangCodes }
                .sortedBy { it.displayName().lowercase(StringSimplifier.locale) }
                .map { song -> PlaylistFillItem.song(song) }
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

    override fun onSongItemClick(item: SongTreeItem) {
        playlistService.toggleSongInCurrentPlaylist(item.song!!)
    }

    override fun onSongItemLongClick(item: SongTreeItem) {
    }
}
