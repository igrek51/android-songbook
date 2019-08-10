package igrek.songbook.songselection.contextmenu

import android.app.Activity
import android.support.v7.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.contact.SendFeedbackService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.songselection.favourite.FavouriteSongsService
import igrek.songbook.util.lookup.SimpleCache
import javax.inject.Inject


class SongContextMenuBuilder {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var favouriteSongsService: FavouriteSongsService
    @Inject
    lateinit var customSongService: CustomSongService
    @Inject
    lateinit var sendFeedbackService: SendFeedbackService
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var playlistService: PlaylistService

    private var allActions: SimpleCache<List<SongContextAction>> =
            SimpleCache { createAllActions() }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    private fun createAllActions(): List<SongContextAction> {
        val actions = mutableListOf(
                // EDIT
                SongContextAction(R.string.action_song_edit,
                        availableCondition = { song -> song.custom },
                        executor = { song ->
                            customSongService.showEditSongScreen(song)
                        }),
                // REMOVE
                SongContextAction(R.string.action_song_remove,
                        availableCondition = { song -> song.custom },
                        executor = { song ->
                            ConfirmDialogBuilder().confirmAction(R.string.confirm_remove_song) {
                                customSongService.removeSong(song)
                            }
                        }),
                // SET_FAVORITE
                SongContextAction(R.string.action_song_set_favourite,
                        availableCondition = { song -> !favouriteSongsService.isSongFavourite(song) },
                        executor = { song ->
                            favouriteSongsService.setSongFavourite(song)
                        }),
                // UNSET_FAVORITE
                SongContextAction(R.string.action_song_unset_favourite,
                        availableCondition = { song -> favouriteSongsService.isSongFavourite(song) },
                        executor = { song ->
                            favouriteSongsService.unsetSongFavourite(song)
                        }),
                // COPY
                SongContextAction(R.string.action_song_copy,
                        availableCondition = { song -> !song.custom },
                        executor = { song ->
                            customSongService.copySongAsCustom(song)
                        }),
                // AMEND
                SongContextAction(R.string.action_song_amend,
                        availableCondition = { song -> !song.custom },
                        executor = { song ->
                            sendFeedbackService.amendSong(song)
                        }),
                // ADD COMMENT
                SongContextAction(R.string.action_song_add_comment,
                        availableCondition = { song -> !song.custom },
                        executor = { song ->
                            sendFeedbackService.commentSong(song)
                        }),
                // PUBLISH
                SongContextAction(R.string.action_song_publish,
                        availableCondition = { song -> song.custom },
                        executor = { song ->
                            sendFeedbackService.publishSong(song)
                        }),
                // Add to playlist
                SongContextAction(R.string.action_add_to_playlist,
                        availableCondition = { true },
                        executor = { song ->
                            playlistService.showAddSongToPlaylist(song)
                        })
        )

        actions.forEach { action ->
            action.displayName = uiResourceService.resString(action.displayNameResId)
        }
        return actions
    }

    fun showSongActions(song: Song) {
        val songActions = allActions.get()
                .filter { action -> action.availableCondition(song) }
        val actionNames = songActions.map { action -> action.displayName }.toTypedArray()

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(uiResourceService.resString(R.string.song_action_choose))
        builder.setItems(actionNames) { _, item ->
            SafeExecutor().execute {
                songActions[item].executor(song)
            }
        }

        val alert = builder.create()
        alert.show()
    }

}