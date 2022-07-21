package igrek.songbook.custom


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.custom.list.CustomSongListItem
import igrek.songbook.custom.list.CustomSongListView
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.layout.spinner.SinglePicker
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomCategory
import igrek.songbook.persistence.user.custom.CustomSongsDb
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.tree.NoParentItemException
import igrek.songbook.system.locale.InsensitiveNameComparator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.serialization.json.Json

class CustomSongsListLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    exportFileChooser: LazyInject<ExportFileChooser> = appFactory.songExportFileChooser,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    importFileChooser: LazyInject<ImportFileChooser> = appFactory.allSongsImportFileChooser,
    songImportFileChooser: LazyInject<SongImportFileChooser> = appFactory.songImportFileChooser,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_custom_songs
), ListItemClickListener<CustomSongListItem> {
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

    private var itemsListView: CustomSongListView? = null
    private var goBackButton: ImageButton? = null
    private var storedScroll: ListScrollPosition? = null
    private var tabTitleLabel: TextView? = null
    private var emptyListLabel: TextView? = null

    private var customCategory: CustomCategory? = null
    private var sortPicker: SinglePicker<SongSorting>? = null
    private var currentSorting: SongSorting = SongSorting.BY_TITLE
    private var subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<ImageButton>(R.id.addCustomSongButton)?.setOnClickListener {
            addCustomSong()
        }

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.setOnClickListener {
            showMoreActions()
        }

        tabTitleLabel = layout.findViewById(R.id.tabTitleLabel)
        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)?.also {
            it.setOnClickListener { goUp() }
        }

        layout.findViewById<ImageButton>(R.id.songsSortButton)?.apply {
            val title = uiResourceService.resString(R.string.song_list_sorting)
            sortPicker = SinglePicker(
                activity,
                entityNames = songSortingEntries(),
                selected = currentSorting,
                title = title,
            ) { selectedSorting ->
                if (currentSorting != selectedSorting) {
                    currentSorting = selectedSorting
                    updateItemsList()
                }
            }
            setOnClickListener {
                sortPicker?.showChoiceDialog()
            }
        }

        itemsListView = layout.findViewById(R.id.itemsListView)
        itemsListView!!.init(activity as Context, this)
        updateItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (isLayoutVisible())
                    updateItemsList()
            }, UiErrorHandler::handleError))
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(mutableListOf(
            ContextMenuBuilder.Action(R.string.import_content_from_file) {
                importOneSong()
            },
            ContextMenuBuilder.Action(R.string.edit_song_import_custom_songs) {
                importCustomSongs()
            },
            ContextMenuBuilder.Action(R.string.edit_song_export_all_custom_songs) {
                exportAllCustomSongs()
            },
        ))
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
            importFileChooser.importFile() { content: String, _ ->
                val json = Json {
                    ignoreUnknownKeys = true
                    allowStructuredMapKeys = true
                    prettyPrint = false
                    useArrayPolymorphism = false
                }

                val importedCustomSongsDb = json.decodeFromString(CustomSongsDb.serializer(), content)
                val added = songsRepository.customSongsDao.addNewCustomSongs(importedCustomSongsDb.songs)
                uiInfoService.showInfo(R.string.custom_songs_imported, added.toString())
            }
        }
    }

    private fun addCustomSong() {
        customSongService.showAddSongScreen()
    }

    private fun updateItemsList() {
        val locale = appLanguageService.getCurrentLocale()

        val songItems: List<CustomSongListItem>
        val groupingEnabled = customSongService.customSongsGroupCategories
        if (groupingEnabled) {

            val categoryNameComparator = InsensitiveNameComparator<CustomCategory>(locale) { category -> category.name }
            if (customCategory == null) {
                val categories = songsRepository.customSongsDao.customCategories
                    .sortedWith(categoryNameComparator)
                    .map { CustomSongListItem(customCategory = it) }
                val uncategorized = songsRepository.customSongsRepo.uncategorizedSongs.get()
                    .sortSongs()
                    .map { CustomSongListItem(song = it) }
                songItems = categories + uncategorized
            } else {
                songItems = customCategory!!.songs
                    .sortSongs()
                    .map { CustomSongListItem(song = it) }
            }

        } else {
            songItems = songsRepository.customSongsRepo.songs.get()
                .sortSongs()
                .map { CustomSongListItem(song = it) }
        }

        itemsListView?.items = songItems

        if (storedScroll != null) {
            Handler(Looper.getMainLooper()).post {
                itemsListView?.restoreScrollPosition(storedScroll)
            }
        }

        val customSongsTitle = uiResourceService.resString(R.string.nav_custom_song)
        tabTitleLabel?.text = when {
            customCategory != null && groupingEnabled -> "$customSongsTitle: ${customCategory?.name}"
            else -> customSongsTitle
        }

        emptyListLabel!!.visibility = when {
            itemsListView!!.count == 0 -> View.VISIBLE
            else -> View.GONE
        }

        goBackButton?.visibility = when {
            customCategory != null && groupingEnabled -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun List<Song>.sortSongs(): List<Song> {
        val locale = appLanguageService.getCurrentLocale()

        return when (currentSorting) {
            SongSorting.BY_TITLE -> {
                this.sortedBy { song -> song.displayName().lowercase(locale) }
            }
            SongSorting.BY_ARTIST -> {
                this.sortedWith(
                    compareBy<Song> { song -> song.displayCategories().isEmpty() }
                        .thenBy { song -> song.displayCategories() }
                        .thenBy { song -> song.displayName().lowercase(locale) }
                )
            }
            SongSorting.BY_LATEST -> {
                this.sortedWith(compareBy (
                    { song -> -song.updateTime },
                    { song -> song.displayName().lowercase(locale) },
                ))
            }
            SongSorting.BY_OLDEST -> {
                this.sortedWith(compareBy (
                    { song -> song.updateTime },
                    { song -> song.displayName().lowercase(locale) },
                ))
            }
        }
    }

    override fun onItemClick(item: CustomSongListItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.song != null) {
            songOpener.openSongPreview(item.song)
        } else if (item.customCategory != null) {
            customCategory = item.customCategory
            updateItemsList()
        }
    }

    override fun onBackClicked() {
        goUp()
    }

    private fun goUp() {
        try {
            if (customCategory == null)
                throw NoParentItemException()
            customCategory = null
            updateItemsList()
        } catch (e: NoParentItemException) {
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    override fun onItemLongClick(item: CustomSongListItem) {
        onMoreActions(item)
    }

    override fun onMoreActions(item: CustomSongListItem) {
        item.song?.let {
            songContextMenuBuilder.showSongActions(it)
        }
    }

    enum class SongSorting(val resId: Int) {

        BY_TITLE(R.string.song_sorting_by_title),
        BY_ARTIST(R.string.song_sorting_by_artist),
        BY_LATEST(R.string.song_sorting_by_latest),
        BY_OLDEST(R.string.song_sorting_by_oldest),
        ;
    }

    private fun songSortingEntries(): LinkedHashMap<SongSorting, String> {
        val map = LinkedHashMap<SongSorting, String>()
        SongSorting.values().forEach { item ->
            val displayName = uiResourceService.resString(item.resId)
            map[item] = displayName
        }
        return map
    }

}
