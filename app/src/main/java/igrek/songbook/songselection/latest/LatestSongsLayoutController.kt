package igrek.songbook.songselection.latest


import android.view.View
import igrek.songbook.R
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.LazySongListView
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class LatestSongsLayoutController(
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
        songOpener: LazyInject<SongOpener> = appFactory.songOpener,
        appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_latest_songs
), SongClickListener {
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val appLanguageService by LazyExtractor(appLanguageService)

    private var itemsListView: LazySongListView? = null
    private var storedScroll: ListScrollPosition? = null
    private var subscriptions = mutableListOf<Disposable>()
    private val latestSongsCount = 200

    override fun showLayout(layout: View) {
        super.showLayout(layout)


        itemsListView = layout.findViewById<LazySongListView>(R.id.itemsList)?.also {
            it.init(activity, this, songContextMenuBuilder)
        }
        updateItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (isLayoutVisible())
                        updateItemsList()
                })
    }

    private fun updateItemsList() {
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null
        val latestSongs = songsRepository.publicSongsRepo.songs.get()
                .asSequence()
                .filter { it.isPublic() }
                .filter { song -> song.language in acceptedLangCodes }
                .sortedBy { song -> -song.updateTime }
                .take(latestSongsCount)
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
