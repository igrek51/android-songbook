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

    private var itemsListView: CustomSongListView? = null
    private var goBackButton: ImageButton? = null
    private var storedScroll: ListScrollPosition? = null
    private var tabTitleLabel: TextView? = null
    private var emptyListLabel: TextView? = null

    private var customCategory: CustomCategory? = null

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
                ContextMenuBuilder.Action(R.string.edit_song_export_all_custom_songs) {
                    exportAllCustomSongs()
                },
                ContextMenuBuilder.Action(R.string.edit_song_import_custom_songs) {
                    importCustomSongs()
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
        val categoryNameComparator = InsensitiveNameComparator<CustomCategory>(locale) { category -> category.name }
        val songNameComparator = InsensitiveNameComparator<Song>(locale) { song -> song.displayName() }
        val groupingEnabled = customSongService.customSongsGroupCategories

        if (groupingEnabled) {
            itemsListView!!.items = if (customCategory == null) {
                val categories = songsRepository.customSongsDao.customCategories
                        .sortedWith(categoryNameComparator)
                        .map {
                            CustomSongListItem(customCategory = it)
                        }
                val uncategorized = songsRepository.customSongsRepo.songs.get()
                        .sortedWith(songNameComparator)
                        .map {
                            CustomSongListItem(song = it)
                        }
                categories + uncategorized
            } else {
                customCategory!!.songs
                        .sortedWith(songNameComparator)
                        .map {
                            CustomSongListItem(song = it)
                        }
            }
        } else {
            itemsListView!!.items = songsRepository.customSongsRepo.songs.get()
                    .sortedWith(songNameComparator)
                    .map {
                        CustomSongListItem(song = it)
                    }
        }

        if (storedScroll != null) {
            Handler(Looper.getMainLooper()).post {
                itemsListView?.restoreScrollPosition(storedScroll)
            }
        }

        val customSongsTitle = uiResourceService.resString(R.string.nav_custom_song)
        tabTitleLabel?.text = when (customCategory) {
            null -> customSongsTitle
            else -> "$customSongsTitle: ${customCategory?.name}"
        }

        emptyListLabel!!.visibility = if (itemsListView!!.count == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }

        goBackButton?.visibility = when (customCategory) {
            null -> View.GONE
            else -> View.VISIBLE
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

    override fun onItemClick(item: CustomSongListItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.song != null) {
            songOpener.openSongPreview(item.song)
        } else if (item.customCategory != null) {
            customCategory = item.customCategory
            updateItemsList()
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

}
