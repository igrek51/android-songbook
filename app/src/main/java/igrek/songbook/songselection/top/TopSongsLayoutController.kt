package igrek.songbook.songselection.latest

import android.os.Handler
import android.os.Looper
import android.view.View
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.SongListView
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.search.SongSearchItem
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

    private var itemsListView: SongListView? = null

    private var storedScroll: ListScrollPosition? = null

    private var subscriptions = mutableListOf<Disposable>()

    private val topSongsCount = 300

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        itemsListView = layout.findViewById(R.id.itemsList)
        itemsListView!!.init(activity, this)
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
        itemsListView!!.setItems(latestSongs)

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
