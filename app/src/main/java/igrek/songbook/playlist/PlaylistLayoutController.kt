package igrek.songbook.playlist

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.cast.AnimalNameFeeder
import igrek.songbook.compose.AppTheme
import igrek.songbook.compose.ReorderListView
import igrek.songbook.info.UiInfoService
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
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    contextMenuBuilder: LazyInject<ContextMenuBuilder> = appFactory.contextMenuBuilder,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_playlists
), ListItemClickListener<PlaylistListItem> {
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val contextMenuBuilder by LazyExtractor(contextMenuBuilder)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songOpener by LazyExtractor(songOpener)
    private val playlistService by LazyExtractor(playlistService)

    private var itemsListView: PlaylistListView? = null
    private var addButton: ImageButton? = null
    private var emptyListLabel: TextView? = null
    private var playlistTitleLabel: TextView? = null
    private var goBackButton: ImageButton? = null

    private var storedScroll: ListScrollPosition? = null
    private var subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

//        itemsListView = layout.findViewById(R.id.playlistListView)

        addButton = layout.findViewById(R.id.addPlaylistButton)
        addButton?.setOnClickListener { handleAddButton() }

        playlistTitleLabel = layout.findViewById(R.id.playlistTitleLabel)
//        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton?.setOnClickListener { goUp() }

        itemsListView?.init(activity, this, ::itemMoved)
        updateItemsList()

        val thisLayout = this
        layout.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
        }

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
                    R.id.itemMoreButton, R.id.compose_view, R.id.itemMoveButton -> {
                        (currentView as ViewGroup).descendantFocusability =
                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        itemsListView?.requestFocusFromTouch()
                    }
                }
                when {
                    currentFocusId == R.id.itemSongMoreButton -> R.id.itemMoveButton
                    currentFocusId == R.id.itemMoreButton -> -1
                    currentFocusId == R.id.itemMoveButton -> -1
                    currentFocusId == R.id.compose_view && playlistService.currentPlaylist != null -> R.id.goBackButton
                    currentFocusId == R.id.main_content && playlistService.currentPlaylist != null -> R.id.goBackButton
                    currentFocusId == R.id.compose_view && playlistService.currentPlaylist == null -> R.id.navMenuButton
                    else -> 0
                }
            },
            nextRight = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemMoveButton -> R.id.itemSongMoreButton
                    else -> 0
                }
            },
            nextUp = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemSongMoreButton, R.id.itemMoreButton, R.id.compose_view, R.id.itemMoveButton -> {
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
                            playlistService.currentPlaylist != null -> R.id.goBackButton
                            else -> R.id.navMenuButton
                        }
                    }
                    else -> 0
                }
            },
            nextDown = { currentFocusId: Int, currentView: View ->
                when (currentFocusId) {
                    R.id.itemSongMoreButton, R.id.itemMoreButton, R.id.compose_view, R.id.itemMoveButton -> {
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

    private fun handleAddButton() {
        when (playlistService.currentPlaylist) {
            null -> playlistService.addNewPlaylist()
            else -> layoutController.showLayout(PlaylistFillLayoutController::class)
        }
    }

    private fun updateItemsList() {
        val items = if (playlistService.currentPlaylist == null) {
            songsRepository.playlistDao.playlistDb.playlists
                .map { p -> PlaylistListItem(playlist = p) }
                .toMutableList()
        } else {
            playlistService.currentPlaylist?.songs
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

        playlistTitleLabel?.text = when (playlistService.currentPlaylist) {
            null -> uiInfoService.resString(R.string.nav_playlists)
            else -> playlistService.currentPlaylist?.name
        }

        emptyListLabel?.text = when (playlistService.currentPlaylist) {
            null -> uiInfoService.resString(R.string.empty_playlists)
            else -> uiInfoService.resString(R.string.empty_playlist_songs)
        }
        emptyListLabel?.visibility = when (itemsListView?.count) {
            0 -> View.VISIBLE
            else -> View.GONE
        }

        addButton?.visibility = View.VISIBLE

        goBackButton?.visibility = when (playlistService.currentPlaylist) {
            null -> View.GONE
            else -> View.VISIBLE
        }
    }

    override fun onBackClicked() {
        goUp()
    }

    private fun goUp() {
        try {
            if (playlistService.currentPlaylist == null)
                throw NoParentItemException()

            playlistService.currentPlaylist = null
            updateItemsList()
        } catch (e: NoParentItemException) {
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    override fun onItemClick(item: PlaylistListItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.song != null) {
            songOpener.openSongPreview(item.song, playlist = playlistService.currentPlaylist)
        } else if (item.playlist != null) {
            playlistService.currentPlaylist = item.playlist
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
        val songs = playlistService.currentPlaylist?.songs ?: return emptyList()
        val existingSongs = songs
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
        playlistService.currentPlaylist?.songs = existingSongs
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

@Composable
private fun MainComponent(controller: PlaylistLayoutController) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Text("DUPA")
        }

        val entities = remember {
            mutableListOf("Dupa").also { l ->
                val a = AnimalNameFeeder()
                repeat(50) {
                    l.add(a.generateName())
                }
            }
        }

        ReorderListView(entities) { item: String, reorderButtonModifier: Modifier ->
            Row (Modifier.padding(vertical = 8.dp, horizontal = 2.dp)) {
                IconButton(
                    modifier = reorderButtonModifier.align(Alignment.CenterVertically),
                    onClick = {}
                ) {
                    Icon(
                        painterResource(id = R.drawable.reorder),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        tint = Color.White,
                    )
                }
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = item,
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
