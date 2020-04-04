package igrek.songbook.songselection.contextmenu

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.admin.AdminService
import igrek.songbook.contact.PublishSongService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.songpreview.SongDetailsService
import igrek.songbook.songpreview.SongPreviewLayoutController
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
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var playlistService: PlaylistService
    @Inject
    lateinit var layoutController: Lazy<LayoutController>
    @Inject
    lateinit var songPreviewLayoutController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var songDetailsService: SongDetailsService
    @Inject
    lateinit var publishSongService: PublishSongService
    @Inject
    lateinit var adminService: Lazy<AdminService>

    private var allActions: SimpleCache<List<SongContextAction>> =
            SimpleCache { createAllActions() }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    private fun createAllActions(): List<SongContextAction> {
        val actions = mutableListOf(
                SongContextAction(R.string.action_song_edit,
                        availableCondition = { song -> song.isCustom() },
                        executor = { song ->
                            customSongService.showEditSongScreen(song)
                        }),
                SongContextAction(R.string.action_song_remove,
                        availableCondition = { song -> song.isCustom() },
                        executor = { song ->
                            ConfirmDialogBuilder().confirmAction(R.string.confirm_remove_song) {
                                customSongService.removeSong(song)
                            }
                        }),
                SongContextAction(R.string.action_song_publish,
                        availableCondition = { song -> song.isCustom() },
                        executor = { song ->
                            publishSongService.publishSong(song)
                        }),
                SongContextAction(R.string.action_song_set_favourite,
                        availableCondition = { song ->
                            !favouriteSongsService.isSongFavourite(song)
                        },
                        executor = { song ->
                            favouriteSongsService.setSongFavourite(song)
                        }),
                SongContextAction(R.string.action_song_unset_favourite,
                        availableCondition = { song ->
                            favouriteSongsService.isSongFavourite(song)
                        },
                        executor = { song ->
                            favouriteSongsService.unsetSongFavourite(song)
                        }),
                SongContextAction(R.string.action_add_to_playlist,
                        availableCondition = { true },
                        executor = { song ->
                            playlistService.showAddSongToPlaylistDialog(song)
                        }),
                SongContextAction(R.string.action_remove_from_playlist,
                        availableCondition = { song ->
                            songsRepository.playlistDao.isSongOnAnyPlaylist(song)
                        },
                        executor = { song ->
                            playlistService.removeFromPlaylist(song)
                        }),
                SongContextAction(R.string.show_chords_definitions,
                        availableCondition = { layoutController.get().isState(SongPreviewLayoutController::class) },
                        executor = {
                            songPreviewLayoutController.get().showChordsGraphs()
                        }),
                SongContextAction(R.string.song_details_title,
                        availableCondition = { true },
                        executor = { song ->
                            songDetailsService.showSongDetails(song)
                        }),
                SongContextAction(R.string.action_song_copy,
                        availableCondition = { song -> !song.isCustom() },
                        executor = { song ->
                            customSongService.copySongAsCustom(song)
                        }),
                SongContextAction(R.string.admin_antechamber_edit_action,
                        availableCondition = { adminService.get().isAdminEnabled() },
                        executor = { song ->
                            customSongService.showEditSongScreen(song)
                        }),
                SongContextAction(R.string.admin_song_content_update_action,
                        availableCondition = { song -> song.isPublic() && adminService.get().isAdminEnabled() },
                        executor = { song ->
                            adminService.get().updatePublicSongUi(song)
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
        builder.setItems(actionNames) { _, item ->
            SafeExecutor {
                songActions[item].executor(song)
            }
        }

        val alert = builder.create()
        if (!activity.isFinishing) {
            alert.show()
        }
    }

}