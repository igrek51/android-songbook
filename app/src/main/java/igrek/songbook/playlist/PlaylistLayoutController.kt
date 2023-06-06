@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package igrek.songbook.playlist

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import igrek.songbook.R
import igrek.songbook.compose.AppTheme
import igrek.songbook.compose.ReorderListView
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LocalFocusTraverser
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.dialog.InputDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.Playlist
import igrek.songbook.persistence.user.playlist.PlaylistSong
import igrek.songbook.playlist.list.PlaylistListView
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.NoParentItemException
import igrek.songbook.util.mainScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.launch

class PlaylistLayoutController : InflatedLayout(
    _layoutResourceId = R.layout.screen_playlists
) {
    private val songsRepository: SongsRepository by LazyExtractor(appFactory.songsRepository)
    private val songContextMenuBuilder: SongContextMenuBuilder by LazyExtractor(appFactory.songContextMenuBuilder)
    private val contextMenuBuilder: ContextMenuBuilder by LazyExtractor(appFactory.contextMenuBuilder)
    private val uiInfoService: UiInfoService by LazyExtractor(appFactory.uiInfoService)
    private val songOpener: SongOpener by LazyExtractor(appFactory.songOpener)
    private val playlistService: PlaylistService by LazyExtractor(appFactory.playlistService)

    private var itemsListView: PlaylistListView? = null
    private var addButton: ImageButton? = null
    private var playlistTitleLabel: TextView? = null
    private var goBackButton: ImageButton? = null

    val state = PlaylistLayoutState()
    private var subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        addButton = layout.findViewById(R.id.addPlaylistButton)
        addButton?.setOnClickListener { handleAddButton() }

        playlistTitleLabel = layout.findViewById(R.id.playlistTitleLabel)

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton?.setOnClickListener { goUp() }

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
        val playlist: Playlist? = playlistService.currentPlaylist
        state.currentPlaylist.value = playlist
        when (playlist) {
            null -> {
                state.playlistItems.value = songsRepository.playlistDao.playlistDb.playlists
            }
            else -> {
                state.songItems.value = playlist.songs
                    .mapNotNull { s ->
                        val namespace = when {
                            s.custom -> SongNamespace.Custom
                            else -> SongNamespace.Public
                        }
                        val id = SongIdentifier(s.songId, namespace)
                        val song = songsRepository.allSongsRepo.songFinder.find(id)
                        song
                    }.toMutableList()
            }
        }

        playlistTitleLabel?.text = when (playlist) {
            null -> uiInfoService.resString(R.string.nav_playlists)
            else -> playlist.name
        }

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
            mainScope.launch {
                state.scrollState.scrollTo(0)
            }
            updateItemsList()
        } catch (e: NoParentItemException) {
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    suspend fun onPlaylistClick(playlist: Playlist) {
        playlistService.currentPlaylist = playlist
        state.scrollState.scrollTo(0)
        updateItemsList()
    }

    fun onSongClick(song: Song) {
        songOpener.openSongPreview(song, playlist = playlistService.currentPlaylist)
    }

    fun onPlaylistMore(playlist: Playlist) {
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
        contextMenuBuilder.showContextMenu(actions)
    }

    fun onSongMore(song: Song) {
        songContextMenuBuilder.showSongActions(song)
    }

    fun onSongsReordered(newItems: MutableList<Song>) {
        val newPlaylistSongs: MutableList<PlaylistSong> = newItems.map { song ->
            PlaylistSong(song.id, song.isCustom())
        }.toMutableList()
        playlistService.currentPlaylist?.songs = newPlaylistSongs
    }

    fun onPlaylistsReordered(newItems: MutableList<Playlist>) {
        val newPlaylists: MutableList<Playlist> = newItems.toMutableList()
        songsRepository.playlistDao.playlistDb.playlists = newPlaylists
    }

    private fun renamePlaylist(playlist: Playlist) {
        InputDialogBuilder().input(uiInfoService.resString(R.string.edit_playlist_name), playlist.name) { name ->
            playlist.name = name
            songsRepository.playlistDao.savePlaylist(playlist)
        }
    }
}

class PlaylistLayoutState {
    val scrollState: ScrollState = ScrollState(0)
    val currentPlaylist: MutableState<Playlist?> = mutableStateOf(null)
    val playlistItems: MutableState<MutableList<Playlist>> = mutableStateOf(mutableListOf())
    val songItems: MutableState<MutableList<Song>> = mutableStateOf(mutableListOf())
}

@Composable
private fun MainComponent(controller: PlaylistLayoutController) {
    Column {
        val currentPlaylist = controller.state.currentPlaylist.value
        if (currentPlaylist == null) {
            if (controller.state.playlistItems.value.isNotEmpty()) {

                ReorderListView(
                    items = controller.state.playlistItems.value,
                    scrollState = controller.state.scrollState,
                    onReorder = { newItems ->
                        controller.onPlaylistsReordered(newItems)
                    },
                ) { playlist: Playlist, reorderButtonModifier: Modifier ->
                    PlaylistItemComposable(controller, playlist, reorderButtonModifier)
                }

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.empty_playlists),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            if (controller.state.songItems.value.isNotEmpty()) {

                ReorderListView(
                    items = controller.state.songItems.value,
                    scrollState = controller.state.scrollState,
                    onReorder = { newItems ->
                        controller.onSongsReordered(newItems)
                    },
                ) { song: Song, reorderButtonModifier: Modifier ->
                    SongItemComposable(controller, song, reorderButtonModifier)
                }

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.empty_playlist_songs),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemComposable(controller: PlaylistLayoutController, playlist: Playlist, reorderButtonModifier: Modifier) {
    Row(
        Modifier.padding(vertical = 2.dp)
            .combinedClickable(
                onClick = {
                    mainScope.launch {
                        controller.onPlaylistClick(playlist)
                    }
                },
                onLongClick = {
                    mainScope.launch {
                        controller.onPlaylistMore(playlist)
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(id = R.drawable.playlist),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp, horizontal = 4.dp),
            text = playlist.name,
            fontWeight = FontWeight.Bold,
        )
        IconButton(
            modifier = reorderButtonModifier.size(32.dp).padding(4.dp),
            onClick = {},
        ) {
            Icon(
                painterResource(id = R.drawable.reorder),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }
        IconButton(
            onClick = {
              mainScope.launch {
                  controller.onPlaylistMore(playlist)
              }
            },
        ) {
            Icon(
                painterResource(id = R.drawable.more),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun SongItemComposable(controller: PlaylistLayoutController, song: Song, reorderButtonModifier: Modifier) {
    Row (
        Modifier.padding(0.dp)
            .combinedClickable(
                onClick = {
                    mainScope.launch {
                        controller.onSongClick(song)
                    }
                },
                onLongClick = {
                    mainScope.launch {
                        controller.onSongMore(song)
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(id = R.drawable.note),
            modifier = Modifier.size(24.dp).padding(0.dp),
            contentDescription = null,
            tint = Color.White,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp, horizontal = 4.dp),
            text = song.displayName(),
        )

        val tooltipState = remember { PlainTooltipState() }
        PlainTooltipBox(
            tooltip = { Text(stringResource(R.string.drag_to_reorder_hint)) },
            tooltipState = tooltipState,
        ) {
            IconButton(
                modifier = reorderButtonModifier.size(32.dp).padding(4.dp).tooltipAnchor(),
                onClick = { mainScope.launch { tooltipState.show() } },
            ) {
                Icon(
                    painterResource(id = R.drawable.reorder),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White,
                )
            }
        }

        IconButton(
            modifier = Modifier.size(32.dp).padding(4.dp),
            onClick = {
                mainScope.launch {
                    controller.onSongMore(song)
                }
            },
        ) {
            Icon(
                painterResource(id = R.drawable.more),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
            )
        }
    }
}
