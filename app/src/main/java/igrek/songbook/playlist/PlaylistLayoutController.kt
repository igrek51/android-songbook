package igrek.songbook.playlist


import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import igrek.songbook.R
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
import igrek.songbook.layout.dialog.InputDialogBuilder
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.Playlist
import igrek.songbook.playlist.list.PlaylistListItem
import igrek.songbook.playlist.list.PlaylistListView
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.tree.NoParentItemException
import igrek.songbook.util.ListMover
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class PlaylistLayoutController(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    contextMenuBuilder: LazyInject<ContextMenuBuilder> = appFactory.contextMenuBuilder,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_playlists
), ListItemClickListener<PlaylistListItem> {
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val contextMenuBuilder by LazyExtractor(contextMenuBuilder)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songOpener by LazyExtractor(songOpener)
    private val playlistService by LazyExtractor(playlistService)

    private var itemsListView: PlaylistListView? = null
    private var addPlaylistButton: ImageButton? = null
    private var emptyListLabel: TextView? = null
    private var playlistTitleLabel: TextView? = null
    private var goBackButton: ImageButton? = null

    private var playlist: Playlist? = null
    private var storedScroll: ListScrollPosition? = null
    private var subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        itemsListView = layout.findViewById(R.id.playlistListView)

        addPlaylistButton = layout.findViewById(R.id.addPlaylistButton)
        addPlaylistButton?.setOnClickListener { playlistService.addNewPlaylist() }

        playlistTitleLabel = layout.findViewById(R.id.playlistTitleLabel)
        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton?.setOnClickListener { goUp() }

        itemsListView!!.init(activity, this, ::itemMoved)
        updateItemsList()

        val localFocus = LocalFocusTraverser(
            currentViewGetter = { itemsListView?.selectedView },
            currentFocusGetter = { appFactory.activity.get().currentFocus?.id },
            preNextFocus = { _: Int, _: View ->
                when {
                    appFactory.navigationMenuController.get().isDrawerShown() -> R.id.nav_view
                    else -> 0
                }
            },
            nextLeft = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemMoreButton, R.id.playlistListView, R.id.itemMoveButton -> {
                        (currentView as ViewGroup).descendantFocusability =
                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        itemsListView?.requestFocusFromTouch()
                    }
                }
                when {
                    currentFocusId == R.id.itemSongMoreButton -> R.id.itemMoveButton
                    currentFocusId == R.id.itemMoreButton -> -1
                    currentFocusId == R.id.itemMoveButton -> -1
                    currentFocusId == R.id.playlistListView && playlist != null -> R.id.goBackButton
                    currentFocusId == R.id.main_content && playlist != null -> R.id.goBackButton
                    currentFocusId == R.id.playlistListView && playlist == null -> R.id.navMenuButton
                    else -> 0
                }
            },
            nextRight = { currentFocusId: Int, currentView: View ->
                when {
                    currentFocusId == R.id.playlistListView -> when {
                        currentView.findViewById<View>(R.id.itemMoveButton)?.isVisible == true -> {
                            (currentView as? ViewGroup)?.descendantFocusability =
                                ViewGroup.FOCUS_BEFORE_DESCENDANTS
                            R.id.itemMoveButton
                        }
                        currentView.findViewById<View>(R.id.itemSongMoreButton)?.isVisible == true -> {
                            (currentView as? ViewGroup)?.descendantFocusability =
                                ViewGroup.FOCUS_BEFORE_DESCENDANTS
                            R.id.itemSongMoreButton
                        }
                        currentView.findViewById<View>(R.id.itemMoreButton)?.isVisible == true -> {
                            (currentView as? ViewGroup)?.descendantFocusability =
                                ViewGroup.FOCUS_BEFORE_DESCENDANTS
                            R.id.itemMoreButton
                        }
                        else -> 0
                    }
                    currentFocusId == R.id.itemMoveButton -> R.id.itemSongMoreButton
                    else -> 0
                }
            },
            nextUp = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemSongMoreButton, R.id.itemMoreButton, R.id.playlistListView, R.id.itemMoveButton -> {
                        (currentView as ViewGroup).descendantFocusability =
                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        itemsListView?.requestFocusFromTouch()
                    }
                }
                when {
                    currentFocusId == R.id.itemSongMoreButton -> -1
                    currentFocusId == R.id.itemMoreButton -> -1
                    (itemsListView?.selectedItemPosition ?: 0) <= 0 -> {
                        when {
                            playlist != null -> R.id.goBackButton
                            else -> R.id.navMenuButton
                        }
                    }
                    else -> 0
                }
            },
            nextDown = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemSongMoreButton, R.id.itemMoreButton, R.id.playlistListView, R.id.itemMoveButton -> {
                        (currentView as ViewGroup).descendantFocusability =
                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        itemsListView?.requestFocusFromTouch()
                    }
                }
                0
            },
        )
        itemsListView?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (localFocus.handleKey(keyCode))
                    return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (isLayoutVisible())
                    updateItemsList()
            }, UiErrorHandler::handleError)
        )
        subscriptions.add(songsRepository.playlistDao.playlistDbSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (isLayoutVisible())
                    updateItemsList()
            }, UiErrorHandler::handleError)
        )
    }

    private fun updateItemsList() {
        val items = if (playlist == null) {
            songsRepository.playlistDao.playlistDb.playlists
                .map { p -> PlaylistListItem(playlist = p) }
                .toMutableList()
        } else {
            playlist?.songs
                ?.mapNotNull { s ->
                    val namespace = when {
                        s.custom -> SongNamespace.Custom
                        else -> SongNamespace.Public
                    }
                    val id = SongIdentifier(s.songId, namespace)
                    val song = songsRepository.allSongsRepo.songFinder.find(id)
                    when {
                        song != null -> PlaylistListItem(song = song)
                        else -> null
                    }
                }
                ?.toMutableList()
        }

        itemsListView?.items = items

        if (storedScroll != null) {
            Handler(Looper.getMainLooper()).post {
                itemsListView?.restoreScrollPosition(storedScroll)
            }
        }

        val playlistsTitle = uiResourceService.resString(R.string.nav_playlists)
        playlistTitleLabel?.text = when (playlist) {
            null -> playlistsTitle
            else -> "$playlistsTitle: ${playlist?.name}"
        }

        emptyListLabel?.text = when (playlist) {
            null -> uiResourceService.resString(R.string.empty_playlists)
            else -> uiResourceService.resString(R.string.empty_playlist_songs)
        }
        emptyListLabel?.visibility = when (itemsListView!!.count) {
            0 -> View.VISIBLE
            else -> View.GONE
        }

        addPlaylistButton?.visibility = when (playlist) {
            null -> View.VISIBLE
            else -> View.GONE
        }

        goBackButton?.visibility = when (playlist) {
            null -> View.GONE
            else -> View.VISIBLE
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
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    override fun onItemClick(item: PlaylistListItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.song != null) {
            songOpener.openSongPreview(item.song, playlist = playlist)
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

    @Synchronized
    fun itemMoved(position: Int, step: Int): List<PlaylistListItem> {
        val existingSongs = playlist!!.songs
            .filter { s ->
                val namespace = when {
                    s.custom -> SongNamespace.Custom
                    else -> SongNamespace.Public
                }
                val id = SongIdentifier(s.songId, namespace)
                val song = songsRepository.allSongsRepo.songFinder.find(id)
                song != null
            }.toMutableList()
        ListMover(existingSongs).move(position, step)
        playlist?.songs = existingSongs
        val items = existingSongs
            .mapNotNull { s ->
                val namespace = when {
                    s.custom -> SongNamespace.Custom
                    else -> SongNamespace.Public
                }
                val id = SongIdentifier(s.songId, namespace)
                val song = songsRepository.allSongsRepo.songFinder.find(id)
                when {
                    song != null -> PlaylistListItem(song = song)
                    else -> null
                }
            }
            .toMutableList()
        itemsListView?.items = items
        return items
    }

}
