package igrek.songbook.playlist

import android.os.Handler
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.custom.CustomSongService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.dialog.InputDialogBuilder
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.Playlist
import igrek.songbook.playlist.list.PlaylistListItem
import igrek.songbook.playlist.list.PlaylistListView
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.NoParentItemException
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class PlaylistLayoutController : InflatedLayout(
        _layoutResourceId = R.layout.playlists,
        _layoutState = LayoutState.PLAYLISTS
), ListItemClickListener<PlaylistListItem> {

    @Inject
    lateinit var customSongService: Lazy<CustomSongService>
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
    lateinit var playlistService: PlaylistService
    @Inject
    lateinit var contextMenuBuilder: ContextMenuBuilder
    @Inject
    lateinit var uiInfoService: UiInfoService

    private var itemsListView: PlaylistListView? = null
    private var playlist: Playlist? = null

    private var storedScroll: ListScrollPosition? = null
    private var emptyListLabel: TextView? = null
    private var subscriptions = mutableListOf<Disposable>()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }

        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }

        itemsListView = layout.findViewById(R.id.playlistListView)

        val addPlaylistButton: ImageButton = layout.findViewById(R.id.addPlaylistButton)
        addPlaylistButton.setOnClickListener { addPlaylist() }

        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        itemsListView!!.init(activity, this)
        updateItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject.subscribe {
            if (isLayoutVisible())
                updateItemsList()
        })
        subscriptions.add(songsRepository.playlistDao.playlistDbSubject.subscribe {
            if (isLayoutVisible())
                updateItemsList()
        })
    }

    private fun addPlaylist() {
        InputDialogBuilder().input("New playlist name", null) { name ->
            val playlist = Playlist(0, name)
            songsRepository.playlistDao.savePlaylist(playlist)
        }
    }

    private fun updateItemsList() {
        val items = if (playlist == null) {
            songsRepository.playlistDao.playlistDb.playlists
                    .map { p -> PlaylistListItem(playlist = p) }
                    .toMutableList()
        } else {
            playlist!!.songs
                    .mapNotNull { s ->
                        val id = SongIdentifier(s.songId, s.custom)
                        val song = songsRepository.songsDb?.songFinder?.find(id)
                        when {
                            song != null -> PlaylistListItem(song = song)
                            else -> null
                        }
                    }
                    .toMutableList()
        }

        itemsListView!!.setItems(items)

        if (storedScroll != null) {
            Handler().post { itemsListView?.restoreScrollPosition(storedScroll) }
        }

        emptyListLabel?.visibility = when {
            itemsListView!!.count == 0 -> View.VISIBLE
            else -> View.GONE
        }
    }

    override fun onBackClicked() {
        goUp()
    }

    private fun goUp() {
        try {
            if (playlist == null)
                throw NoParentItemException()

            playlist = null
            updateItemsList()
        } catch (e: NoParentItemException) {
            activityController.get().quit()
        }
    }

    private fun openSongPreview(song: Song) {
        songPreviewLayoutController.get().currentSong = song
        layoutController.showSongPreview()
    }

    override fun onItemClick(item: PlaylistListItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.song != null) {
            openSongPreview(item.song)
        } else if (item.playlist != null) {
            playlist = item.playlist
            updateItemsList()
        }
    }

    override fun onItemLongClick(item: PlaylistListItem) {
        onMoreActions(item)
    }

    override fun onMoreActions(item: PlaylistListItem) {
        if (item.song != null) {
            songContextMenuBuilder.showSongActions(item.song)
        } else if (item.playlist != null) {
            showPlaylistActions(item.playlist)
        }
    }

    private fun showPlaylistActions(playlist: Playlist) {
        val actions = mutableListOf(
                ContextMenuBuilder.Action(R.string.rename_playlist) {
                    renamePlaylist(playlist)
                },
                ContextMenuBuilder.Action(R.string.remove_playlist) {
                    ConfirmDialogBuilder().confirmAction(R.string.confirm_remove_playlist) {
                        songsRepository.playlistDao.removePlaylist(playlist)
                        uiInfoService.showInfo(R.string.playlist_removed)
                    }
                }
        )

        contextMenuBuilder.showContextMenu(R.string.choose_playlist, actions)
    }

    private fun renamePlaylist(playlist: Playlist) {
        InputDialogBuilder().input("Edit playlist name", playlist.name) { name ->
            playlist.name = name
            songsRepository.playlistDao.savePlaylist(playlist)
        }
    }
}
