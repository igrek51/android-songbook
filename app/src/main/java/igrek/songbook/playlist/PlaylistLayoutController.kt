@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package igrek.songbook.playlist

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
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
import igrek.songbook.compose.ItemsContainer
import igrek.songbook.compose.ReorderListView
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.dialog.InputDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.Playlist
import igrek.songbook.persistence.user.playlist.PlaylistSong
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
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AppTheme {
                    MainComponent(thisLayout)
                }
            }
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
                val items = songsRepository.playlistDao.playlistDb.playlists
                state.playlistItems.replaceAll(items)
            }
            else -> {
                val items = playlist.songs
                    .mapNotNull { s ->
                        val namespace = when {
                            s.custom -> SongNamespace.Custom
                            else -> SongNamespace.Public
                        }
                        val id = SongIdentifier(s.songId, namespace)
                        val song = songsRepository.allSongsRepo.songFinder.find(id)
                        song
                    }.toMutableList()
                state.songItems.replaceAll(items)
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
            updateItemsList()
        } catch (e: NoParentItemException) {
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    suspend fun onPlaylistClick(playlist: Playlist) {
        playlistService.currentPlaylist = playlist
        mainScope.launch {
            state.songsScrollState.scrollTo(0)
        }
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
    val playlistsScrollState: ScrollState = ScrollState(0)
    val songsScrollState: ScrollState = ScrollState(0)
    val currentPlaylist: MutableState<Playlist?> = mutableStateOf(null)
    val playlistItems: ItemsContainer<Playlist> = ItemsContainer()
    val songItems: ItemsContainer<Song> = ItemsContainer()
}

@Composable
private fun MainComponent(controller: PlaylistLayoutController) {
    Column {
        val currentPlaylist = controller.state.currentPlaylist.value
        if (currentPlaylist == null) {
            if (controller.state.playlistItems.items.isNotEmpty()) {

                ReorderListView(
                    itemsContainer = controller.state.playlistItems,
                    scrollState = controller.state.playlistsScrollState,
                    onReorder = { newItems ->
                        controller.onPlaylistsReordered(newItems)
                    },
                    onLoad = {},
                ) { itemsContainer: ItemsContainer<Playlist>, id: Int, modifier: Modifier ->
                    PlaylistItemComposable(controller, itemsContainer, id, modifier)
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
            if (controller.state.songItems.items.isNotEmpty()) {

                ReorderListView(
                    itemsContainer = controller.state.songItems,
                    scrollState = controller.state.songsScrollState,
                    onReorder = { newItems ->
                        controller.onSongsReordered(newItems)
                    },
                    onLoad = {},
                ) { itemsContainer: ItemsContainer<Song>, id: Int, modifier: Modifier ->
                    SongItemComposable(controller, itemsContainer, id, modifier)
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

@SuppressLint("ModifierParameter")
@Composable
private fun PlaylistItemComposable(
    controller: PlaylistLayoutController,
    itemsContainer: ItemsContainer<Playlist>,
    id: Int,
    modifier: Modifier,
) {
    val playlist: Playlist = itemsContainer.items.getOrNull(id) ?: return
    val reorderButtonModifier: Modifier = itemsContainer.reorderButtonModifiers.getValue(id)

    Row(
        modifier.padding(0.dp)
            .combinedClickable(
                onClick = {
                    Handler(Looper.getMainLooper()).post {
                        mainScope.launch {
                            controller.onPlaylistClick(playlist)
                        }
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
            modifier = Modifier.padding(start = 2.dp).size(24.dp),
            tint = Color.White,
        )

        val tooltipState = remember { PlainTooltipState() }
        PlainTooltipBox(
            tooltip = { Text(stringResource(R.string.drag_to_reorder_hint)) },
            tooltipState = tooltipState,
        ) {
            IconButton(
                modifier = reorderButtonModifier
                    .padding(1.dp).size(24.dp).tooltipAnchor(),
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

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            text = playlist.name,
            fontWeight = FontWeight.Bold,
        )

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

@SuppressLint("ModifierParameter")
@Composable
private fun SongItemComposable(
    controller: PlaylistLayoutController,
    itemsContainer: ItemsContainer<Song>,
    id: Int,
    modifier: Modifier,
) {
    val song: Song = itemsContainer.items.getOrNull(id) ?: return
    val reorderButtonModifier: Modifier = itemsContainer.reorderButtonModifiers.getValue(id)

    Row (
        modifier.padding(0.dp)
            .combinedClickable(
                onClick = {
                    Handler(Looper.getMainLooper()).post {
                        mainScope.launch {
                            controller.onSongClick(song)
                        }
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
            contentDescription = null,
            modifier = Modifier.padding(start = 2.dp).size(24.dp),
            tint = Color.White,
        )

        val tooltipState = remember { PlainTooltipState() }
        PlainTooltipBox(
            tooltip = { Text(stringResource(R.string.drag_to_reorder_hint)) },
            tooltipState = tooltipState,
        ) {
            IconButton(
                modifier = reorderButtonModifier
                    .padding(1.dp).size(24.dp).tooltipAnchor(),
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

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp, horizontal = 4.dp),
            text = song.displayName(),
        )

        IconButton(
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
