package igrek.songbook.room

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class RoomLobbyLayoutController(
        roomLobby: LazyInject<RoomLobby> = appFactory.roomLobby,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        songOpener: LazyInject<SongOpener> = appFactory.songOpener,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_room_lobby
) {
    private val roomLobby by LazyExtractor(roomLobby)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songOpener by LazyExtractor(songOpener)
    private val songsRepository by LazyExtractor(songsRepository)

    private var chatListView: RoomChatListView? = null
    private var chatMessageEdit: EditText? = null
    private var membersTextView: TextView? = null
    private var selectedSongTextView: TextView? = null
    private var openSelectedSongButton: Button? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.setOnClickListener(SafeClickListener {
            showMoreActions()
        })

        chatListView = layout.findViewById<RoomChatListView>(R.id.itemsListView)?.also {
            it.onClickCallback = {}
            it.items = listOf()
            it.enableNestedScrolling()
        }

        chatMessageEdit = layout.findViewById(R.id.chatMessageEdit)

        layout.findViewById<Button>(R.id.chatSendButton)?.setOnClickListener(SafeClickListener {
            roomLobby.sendChatMessage(chatMessageEdit?.text?.toString().orEmpty())
            chatMessageEdit?.setText("")
        })

        openSelectedSongButton = layout.findViewById<Button>(R.id.openSelectedSongButton)?.also {
            it.setOnClickListener(SafeClickListener {
                GlobalScope.launch {
                    roomLobby.currentSongId?.let { currentSongId ->
                        songOpener.openSongIdentifier(currentSongId)
                    }
                }
            })
        }
        selectedSongTextView = layout.findViewById(R.id.selectedSongTextView)
        updateOpenSelectedSongWidgets()

        roomLobby.newChatMessageCallback = { chatMessage: ChatMessage ->
            chatListView?.add(chatMessage)
            chatListView?.scrollToBottom()
        }

        membersTextView = layout.findViewById(R.id.membersTextView)
        updateMembers(roomLobby.clients)
        roomLobby.updateMembersCallback = ::updateMembers

        roomLobby.onDroppedCallback = ::onDropped
        roomLobby.onSelectedSongChange = ::onSelectedSongChange
        roomLobby.onSongFetched = ::onSongFetched
    }

    private fun updateOpenSelectedSongWidgets() {
        openSelectedSongButton?.visibility = when (roomLobby.currentSongId) {
            null -> View.INVISIBLE
            else -> View.VISIBLE
        }
        selectedSongTextView?.let { selectedSongTextView ->
            val currentSongName = roomLobby.currentSongId?.let { currentSongId ->
                currentSongName(currentSongId)
            } ?: uiInfoService.resString(R.string.room_current_song_waiting)
            selectedSongTextView.text = uiInfoService.resString(R.string.room_current_song, currentSongName)
        }
    }

    private fun currentSongName(currentSongId: SongIdentifier): String? {
        return songsRepository.allSongsRepo.songFinder.find(currentSongId)?.displayName()
    }

    private fun onDropped() {
        uiInfoService.showInfo(R.string.room_dropped_from_host)
        if (isLayoutVisible()) {
            layoutController.showLayout(RoomListLayoutController::class)
        }
    }

    private fun updateMembers(members: List<PeerClient>) {
        val membersStr = members.map { it.displayMember() }.joinToString("\n") { "- $it" }
        membersTextView?.text = uiInfoService.resString(R.string.room_members, membersStr)
    }

    private fun PeerClient.displayMember(): String {
        val hostname = uiInfoService.resString(R.string.room_host)
        val role = when (this.status) {
            PeerStatus.Master -> " ($hostname)"
            else -> ""
        }
        return this.username + role
    }

    private fun showMoreActions() {
        ContextMenuBuilder().showContextMenu(mutableListOf(
                ContextMenuBuilder.Action(R.string.room_close_room) {
                    GlobalScope.launch {
                        closeAndReturn()
                    }
                },
                ContextMenuBuilder.Action(R.string.room_make_discoverable) {
                    GlobalScope.launch {
                        roomLobby.makeDiscoverable()
                    }
                },
        ))
    }

    private fun onSelectedSongChange(songId: SongIdentifier) {
        if (isLayoutVisible() || layoutController.isState(SongPreviewLayoutController::class)) {
            GlobalScope.launch {
                if (songId.namespace != SongNamespace.Public) {
                    uiInfoService.showInfo(R.string.room_downloading_song, indefinite = true)
                    roomLobby.fetchSong(songId)
                    return@launch
                }

                val result = songOpener.openSongIdentifier(songId)
                if (!result) {
                    logger.error("cant find selected song locally: $songId")
                    uiInfoService.showInfo(R.string.room_downloading_song, indefinite = true)
                    roomLobby.fetchSong(songId)
                }
            }
        }
    }

    private fun onSongFetched(songId: SongIdentifier, categoryName: String, title: String, chordsNotation: ChordsNotation, content: String) {
        val now: Long = Date().time
        val ephemeralSong = Song(
                id = songId.songId,
                title = title,
                categories = mutableListOf(),
                content = content,
                versionNumber = 1,
                createTime = now,
                updateTime = now,
                status = SongStatus.PUBLISHED,
                customCategoryName = categoryName,
                chordsNotation = chordsNotation,
                namespace = SongNamespace.Ephemeral,
        )
        songOpener.openSongPreview(ephemeralSong)
    }

    override fun onBackClicked() {
        when (roomLobby.peerStatus) {
            PeerStatus.Master, PeerStatus.Slave -> {
                ConfirmDialogBuilder().confirmAction(R.string.room_confirm_leave_lobby) {
                    GlobalScope.launch {
                        closeAndReturn()
                    }
                }
            }
            PeerStatus.Disconnected -> super.onBackClicked()
        }
    }

    private suspend fun closeAndReturn() {
        roomLobby.close()
        layoutController.showLayout(RoomListLayoutController::class, disableReturn = true)
    }
}