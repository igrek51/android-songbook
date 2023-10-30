package igrek.songbook.custom


import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.custom.sync.EditorSessionService
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LocalFocusTraverser
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.spinner.SinglePicker
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomCategory
import igrek.songbook.persistence.user.custom.CustomSongsDb
import igrek.songbook.settings.enums.CustomSongsOrdering
import igrek.songbook.settings.enums.SettingsEnumService
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.listview.SongItemsContainer
import igrek.songbook.songselection.listview.SongListComposable
import igrek.songbook.songselection.listview.items.AbstractListItem
import igrek.songbook.songselection.listview.items.CustomCategoryListItem
import igrek.songbook.songselection.listview.items.SongListItem
import igrek.songbook.songselection.search.SongSearchFilter
import igrek.songbook.songselection.search.sortSongsByFilterRelevance
import igrek.songbook.songselection.tree.NoParentItemException
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.locale.InsensitiveNameComparator
import igrek.songbook.util.mainScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class CustomSongsListLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    exportFileChooser: LazyInject<ExportFileChooser> = appFactory.exportFileChooser,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    importFileChooser: LazyInject<ImportFileChooser> = appFactory.importFileChooser,
    songImportFileChooser: LazyInject<SongImportFileChooser> = appFactory.songImportFileChooser,
    settingsEnumService: LazyInject<SettingsEnumService> = appFactory.settingsEnumService,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    editorSessionService: LazyInject<EditorSessionService> = appFactory.editorSessionService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_custom_songs
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val customSongService by LazyExtractor(customSongService)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val songExportFileChooser by LazyExtractor(exportFileChooser)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val importFileChooser by LazyExtractor(importFileChooser)
    private val songImportFileChooser by LazyExtractor(songImportFileChooser)
    private val settingsEnumService by LazyExtractor(settingsEnumService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val preferencesState by LazyExtractor(settingsState)
    private val editorSessionService by LazyExtractor(editorSessionService)

    private var goBackButton: ImageButton? = null
    private var tabTitleLabel: TextView? = null
    private var emptyListLabel: TextView? = null
    private var customSearchBarLayout: FrameLayout? = null
    private var searchFilterEdit: EditText? = null
    private var searchFilterClearButton: ImageButton? = null
    private var searchSongButton: ImageButton? = null
    private var moreActionsButton: ImageButton? = null
    private var songsSortButton: ImageButton? = null

    var customCategory: CustomCategory? = null
    private var storedScroll: ListScrollPosition? = null
    private var sortPicker: SinglePicker<CustomSongsOrdering>? = null
    private var searchingOn: Boolean = false
    private var itemNameFilter: String? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var composeView: ComposeView? = null
    private var subscriptions = mutableListOf<Disposable>()
    val state = CustomSongsLayoutState()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        tabTitleLabel = layout.findViewById(R.id.tabTitleLabel)
        emptyListLabel = layout.findViewById(R.id.emptyListLabel)
        customSearchBarLayout = layout.findViewById(R.id.customSearchBarLayout)

        searchFilterEdit = layout.findViewById<EditText>(R.id.searchFilterEdit)?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchFilterSubject.onNext(s.toString())
                }
            })

            if (isFilterSet()) {
                setText(itemNameFilter, TextView.BufferType.EDITABLE)
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)
                    softKeyboardService.hideSoftKeyboard(this)
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

        moreActionsButton = layout.findViewById<ImageButton>(R.id.moreActionsButton)?.also {
            it.setOnClickListener {
                showMoreActions()
            }
        }

        goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)?.also {
            it.setOnClickListener { goUp() }
        }

        songsSortButton = layout.findViewById<ImageButton>(R.id.songsSortButton)?.apply {
            val title = uiResourceService.resString(R.string.song_list_ordering)
            sortPicker = SinglePicker(
                activity,
                entityNames = settingsEnumService.customSongsOrderingEnumEntries(),
                selected = settingsEnumService.preferencesState.customSongsOrdering,
                title = title,
            ) { selectedSorting ->
                if (settingsEnumService.preferencesState.customSongsOrdering != selectedSorting) {
                    settingsEnumService.preferencesState.customSongsOrdering = selectedSorting
                    updateHeader()
                    updateItemsList()
                }
            }
            setOnClickListener {
                sortPicker?.showChoiceDialog()
            }
        }

        searchSongButton = layout.findViewById<ImageButton>(R.id.searchSongButton)?.also {
            it.setOnClickListener {
                searchingOn = true
                updateHeader()
                updateItemsList()
                searchFilterEdit?.requestFocus()
                Handler(Looper.getMainLooper()).post {
                    softKeyboardService.showSoftKeyboard(searchFilterEdit)
                }
            }
        }

        searchFilterClearButton =
            layout.findViewById<ImageButton>(R.id.searchFilterClearButton)?.also {
                it.setOnClickListener {
                    onClearFilterClicked()
                }
            }

        updateHeader()
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

        val localFocus = LocalFocusTraverser(
            currentViewGetter = { composeView },
            currentFocusGetter = { appFactory.activity.get().currentFocus?.id },
            preNextFocus = { _: Int, _: View ->
                when {
                    appFactory.navigationMenuController.get().isDrawerShown() -> R.id.nav_view
                    else -> 0
                }
            },
            nextLeft = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemSongMoreButton, R.id.itemsListView -> {
                        (currentView as ViewGroup).descendantFocusability =
                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        composeView?.requestFocusFromTouch()
                    }
                }
                when {
                    currentFocusId == R.id.itemSongMoreButton -> -1
                    currentFocusId == R.id.itemsListView && customCategory != null -> R.id.goBackButton
                    currentFocusId == R.id.itemsListView && customCategory == null -> R.id.navMenuButton
                    else -> 0
                }
            },
            nextRight = { currentFocusId: Int, currentView: View ->
                when {
                    currentFocusId == R.id.itemsListView && currentView.findViewById<View>(R.id.itemSongMoreButton)?.isVisible == true -> {
                        (currentView as? ViewGroup)?.descendantFocusability =
                            ViewGroup.FOCUS_BEFORE_DESCENDANTS
                        R.id.itemSongMoreButton
                    }
                    else -> 0
                }
            },
            nextUp = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemSongMoreButton, R.id.itemsListView -> {
                        (currentView as ViewGroup).descendantFocusability =
                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        composeView?.requestFocusFromTouch()
                    }
                }
                when {
                    currentFocusId == R.id.itemSongMoreButton -> -1
                    customCategory != null -> R.id.goBackButton
                    else -> R.id.navMenuButton
                }
            },
            nextDown = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemSongMoreButton, R.id.itemsListView -> {
                        (currentView as ViewGroup).descendantFocusability =
                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        composeView?.requestFocusFromTouch()
                    }
                }
                0
            },
        )
        composeView?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (localFocus.handleKey(keyCode))
                    return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
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
                if (isLayoutVisible())
                    updateItemsList()
            }, UiErrorHandler::handleError)
        )
    }

    private fun isFilterSet(): Boolean {
        if (itemNameFilter.isNullOrEmpty())
            return false
        return (itemNameFilter?.length ?: 0) >= 3
    }

    private fun setSongFilter(itemNameFilter: String?) {
        this.itemNameFilter = itemNameFilter
        if (itemNameFilter == null)
            searchFilterEdit?.setText("", TextView.BufferType.EDITABLE)
        storedScroll = null
        updateItemsList()
    }

    private fun onClearFilterClicked() {
        if (!itemNameFilter.isNullOrEmpty()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
            searchingOn = false
            updateItemsList()
        }
        updateHeader()
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(
            mutableListOf(
                ContextMenuBuilder.Action(R.string.action_create_new_custom_song) {
                    addCustomSong()
                },
                ContextMenuBuilder.Action(R.string.custom_songs_synchornize) {
                    editorSessionService.synchronizeWithWeb()
                },
                ContextMenuBuilder.Action(R.string.import_content_from_file) {
                    importOneSong()
                },
                ContextMenuBuilder.Action(R.string.edit_song_import_custom_songs) {
                    importCustomSongs()
                },
                ContextMenuBuilder.Action(R.string.edit_song_export_all_custom_songs) {
                    exportAllCustomSongs()
                },
            )
        )
    }

    private fun exportAllCustomSongs() {
        val json = Json {
            ignoreUnknownKeys = true
            allowStructuredMapKeys = true
            prettyPrint = false
            useArrayPolymorphism = false
        }
        val exportSongsDb = CustomSongsDb(songs = songsRepository.customSongsDao.customSongs.songs)
        val content = json.encodeToString(CustomSongsDb.serializer(), exportSongsDb)
        songExportFileChooser.showFileChooser(content, "customsongs.json") {
            uiInfoService.showInfo(R.string.custom_songs_exported)
        }
    }

    private fun importOneSong() {
        customSongService.showAddSongScreen()
        songImportFileChooser.showFileChooser()
    }

    private fun importCustomSongs() {
        ConfirmDialogBuilder().confirmAction(uiInfoService.resString(R.string.custom_songs_mass_import_hint)) {
            importFileChooser.importFile(sizeLimit = 10 * 1024 * 1024) { content: String, _ ->
                val json = Json {
                    ignoreUnknownKeys = true
                    allowStructuredMapKeys = true
                    prettyPrint = false
                    useArrayPolymorphism = false
                }

                val importedCustomSongsDb =
                    json.decodeFromString(CustomSongsDb.serializer(), content)
                val added =
                    songsRepository.customSongsDao.addNewCustomSongs(importedCustomSongsDb.songs)
                uiInfoService.showInfo(R.string.custom_songs_imported, added.toString())
            }
        }
    }

    private fun addCustomSong() {
        customSongService.showAddSongScreen()
    }

    private fun updateHeader() {
        val groupingEnabled =
            settingsEnumService.preferencesState.customSongsOrdering == CustomSongsOrdering.GROUP_CATEGORIES
        if (!groupingEnabled)
            customCategory = null

        tabTitleLabel?.text = when {
            customCategory != null && groupingEnabled -> customCategory?.name.orEmpty()
            else -> uiResourceService.resString(R.string.nav_custom_song)
        }

        goBackButton?.visibility = when {
            customCategory != null && groupingEnabled -> View.VISIBLE
            else -> View.GONE
        }

        customSearchBarLayout?.visibility = when {
            searchingOn -> View.VISIBLE
            else -> View.GONE
        }
        tabTitleLabel?.visibility = when {
            !searchingOn -> View.VISIBLE
            else -> View.GONE
        }
        songsSortButton?.visibility = when {
            !searchingOn -> View.VISIBLE
            else -> View.GONE
        }
        searchSongButton?.visibility = when {
            !searchingOn -> View.VISIBLE
            else -> View.GONE
        }
        moreActionsButton?.visibility = when {
            !searchingOn -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun updateItemsList() {
        val groupingEnabled =
            settingsEnumService.preferencesState.customSongsOrdering == CustomSongsOrdering.GROUP_CATEGORIES
        if (!groupingEnabled)
            customCategory = null
        val filtering = isFilterSet()

        val songItems: List<AbstractListItem> = when {

            filtering -> {
                val songFilter =
                    SongSearchFilter(itemNameFilter.orEmpty(), preferencesState.songLyricsSearch)
                songsRepository.customSongsRepo.songs.get()
                    .filter { song -> songFilter.matchSong(song) }
                    .sortSongsByFilterRelevance(songFilter)
                    .map { SongListItem(it) }
            }

            groupingEnabled && customCategory == null -> {
                val locale = appLanguageService.getCurrentLocale()
                val categoryNameComparator =
                    InsensitiveNameComparator<CustomCategory>(locale) { category -> category.name }
                val categories = songsRepository.customSongsDao.customCategories
                    .sortedWith(categoryNameComparator)
                    .map { CustomCategoryListItem(it) }
                val uncategorized = songsRepository.customSongsRepo.uncategorizedSongs.get()
                    .sortSongs()
                    .map { SongListItem(it) }
                categories + uncategorized
            }

            groupingEnabled && customCategory != null -> {
                customCategory!!.songs
                    .sortSongs()
                    .map { SongListItem(it) }
            }

            else -> {
                songsRepository.customSongsRepo.songs.get()
                    .sortSongs()
                    .map { SongListItem(it) }
            }

        }
        state.itemsContainer.replaceAll(songItems)

        emptyListLabel?.visibility = when (songItems.isEmpty()) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun Iterable<Song>.sortSongs(): List<Song> {
        val locale = appLanguageService.getCurrentLocale()
        return when (settingsEnumService.preferencesState.customSongsOrdering) {
            CustomSongsOrdering.SORT_BY_TITLE, CustomSongsOrdering.GROUP_CATEGORIES -> {
                this.sortedBy { song -> song.displayName().lowercase(locale) }
            }
            CustomSongsOrdering.SORT_BY_ARTIST -> {
                this.sortedWith(
                    compareBy<Song> { song -> song.displayCategories().isEmpty() }
                        .thenBy { song -> song.displayCategories() }
                        .thenBy { song -> song.displayName().lowercase(locale) }
                )
            }
            CustomSongsOrdering.SORT_BY_LATEST -> {
                this.sortedWith(
                    compareBy(
                        { song -> -song.updateTime },
                        { song -> song.displayName().lowercase(locale) },
                    )
                )
            }
            CustomSongsOrdering.SORT_BY_OLDEST -> {
                this.sortedWith(
                    compareBy(
                        { song -> song.updateTime },
                        { song -> song.displayName().lowercase(locale) },
                    )
                )
            }
        }
    }

    override fun onBackClicked() {
        if (searchingOn) {
            onClearFilterClicked()
            return
        }
        goUp()
    }

    private fun goUp() {
        try {
            if (customCategory == null)
                throw NoParentItemException()
            customCategory = null

            updateHeader()
            updateItemsList()
            restoreScrollPosition()
        } catch (e: NoParentItemException) {
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    fun onItemClick(item: AbstractListItem) {
        if (item is CustomCategoryListItem) {
            rememberScrollPosition()
            mainScope.launch {
                state.scrollState.scrollToItem(0, 0)
            }

            customCategory = item.customCategory
            updateHeader()
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

class CustomSongsLayoutState {
    val itemsContainer: SongItemsContainer = SongItemsContainer()
    val scrollState: LazyListState = LazyListState()
}

@Composable
private fun MainComponent(controller: CustomSongsListLayoutController) {
    Column {
        SongListComposable(
            controller.state.itemsContainer,
            scrollState = controller.state.scrollState,
            onItemClick = controller::onItemClick,
            onItemMore = controller::onItemMore,
        )
    }
}