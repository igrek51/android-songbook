package igrek.songbook.songselection.latest

import android.os.Handler
import android.view.View
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LayoutState
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.SongListView
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class LatestSongsLayoutController : InflatedLayout(
        _layoutResourceId = R.layout.latest_songs,
        _layoutState = LayoutState.LATEST
), SongClickListener {

    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var songPreviewLayoutController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var songContextMenuBuilder: SongContextMenuBuilder
    @Inject
    lateinit var uiInfoService: UiInfoService

    private var itemsListView: SongListView? = null

    private var storedScroll: ListScrollPosition? = null

    private var subscriptions = mutableListOf<Disposable>()

    private val latestSongsCount = 100

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
        subscriptions.add(songsRepository.dbChangeSubject.subscribe {
            if (isLayoutVisible())
                updateItemsList()
        })
    }

    private fun updateItemsList() {
        val latestSongs = songsRepository.songsDb!!.songs
                .sortedBy { song -> -song.updateTime }
                .take(latestSongsCount)
                .map { song -> SongSearchItem.song(song) }
        itemsListView!!.setItems(latestSongs)

        if (storedScroll != null) {
            Handler().post { itemsListView?.restoreScrollPosition(storedScroll) }
        }
    }

    override fun onBackClicked() {
        layoutController.showSongTree()
    }

    private fun openSongPreview(item: SongTreeItem) {
        songPreviewLayoutController.get().currentSong = item.song
        layoutController.showSongPreview()
    }

    override fun onSongItemClick(item: SongTreeItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.isSong) {
            openSongPreview(item)
        }
    }

    override fun onSongItemLongClick(item: SongTreeItem) {
        if (item.isSong) {
            songContextMenuBuilder.showSongActions(item.song!!)
        }
    }
}
