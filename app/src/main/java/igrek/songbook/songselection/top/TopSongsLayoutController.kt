package igrek.songbook.songselection.top

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
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
import javax.inject.Inject

class TopSongsLayoutController : InflatedLayout(
        _layoutResourceId = R.layout.screen_top_songs
), SongClickListener {

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var uiResourceService: UiResourceService

    @Inject
    lateinit var songContextMenuBuilder: SongContextMenuBuilder

    @Inject
    lateinit var uiInfoService: UiInfoService

    @Inject
    lateinit var songOpener: SongOpener

    @Inject
    lateinit var appLanguageService: Lazy<AppLanguageService>

    private var itemsListView: LazySongListView? = null
    private var storedScroll: ListScrollPosition? = null
    private var languagePicker: MultiPicker<SongLanguage>? = null
    private var subscriptions = mutableListOf<Disposable>()
    private val topSongsCount = 500

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

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
            val songLanguageEntries = appLanguageService.get().songLanguageEntries()
            val selected = appLanguageService.get().selectedSongLanguages
            val title = uiResourceService.resString(R.string.song_languages)
            languagePicker = MultiPicker(
                    activity,
                    entityNames = songLanguageEntries,
                    selected = selected,
                    title = title,
            ) { selectedLanguages ->
                if (appLanguageService.get().selectedSongLanguages != selectedLanguages) {
                    appLanguageService.get().selectedSongLanguages = selectedLanguages.toSet()
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
        val acceptedLanguages = appLanguageService.get().selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + ""
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
            Handler(Looper.getMainLooper()).post {
                itemsListView?.restoreScrollPosition(storedScroll)
            }
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
