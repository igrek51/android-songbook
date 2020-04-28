package igrek.songbook.songselection.top

import android.view.View
import android.widget.ImageButton
import igrek.songbook.R
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.spinner.MultiPicker
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.language.SongLanguage
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.LazySongListView
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.tree.SongTreeItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class TopSongsLayoutController(
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
        songOpener: LazyInject<SongOpener> = appFactory.songOpener,
        appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_top_songs
), SongClickListener {
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val appLanguageService by LazyExtractor(appLanguageService)

    private var itemsListView: LazySongListView? = null
    private var storedScroll: ListScrollPosition? = null
    private var languagePicker: MultiPicker<SongLanguage>? = null
    private var subscriptions = mutableListOf<Disposable>()
    private val topSongsCount = 500

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        itemsListView = layout.findViewById<LazySongListView>(R.id.itemsList)?.also {
            it.init(activity, this, songContextMenuBuilder)
        }
        updateItemsList()

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

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (isLayoutVisible())
                        updateItemsList()
                })
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
                .sortedWith(compareBy({ -(it.rank ?: 0.0) }, { -it.updateTime }))
                .take(topSongsCount)
                .map { song -> SongSearchItem.song(song) }
                .toList()
        itemsListView?.setItems(latestSongs)

        if (storedScroll != null) {
            itemsListView?.restoreScrollPosition(storedScroll)
        }
    }

    override fun onSongItemClick(item: SongTreeItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.isSong) {
            songOpener.openSongPreview(item.song!!)
        }
    }

    override fun onSongItemLongClick(item: SongTreeItem) {
        if (item.isSong) {
            songContextMenuBuilder.showSongActions(item.song!!)
        }
    }
}
