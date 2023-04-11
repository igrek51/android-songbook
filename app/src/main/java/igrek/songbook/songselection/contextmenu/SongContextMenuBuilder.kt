package igrek.songbook.songselection.contextmenu

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.admin.AdminService
import igrek.songbook.admin.antechamber.AntechamberService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.custom.share.ShareSongService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.send.PublishSongService
import igrek.songbook.songpreview.SongDetailsService
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.favourite.FavouriteSongsService
import igrek.songbook.util.lookup.SimpleCache
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class SongContextMenuBuilder(
    activity: LazyInject<Activity> = appFactory.activity,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    favouriteSongsService: LazyInject<FavouriteSongsService> = appFactory.favouriteSongsService,
    customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    songDetailsService: LazyInject<SongDetailsService> = appFactory.songDetailsService,
    publishSongService: LazyInject<PublishSongService> = appFactory.publishSongService,
    adminService: LazyInject<AdminService> = appFactory.adminService,
    antechamberService: LazyInject<AntechamberService> = appFactory.antechamberService,
    shareSongService: LazyInject<ShareSongService> = appFactory.shareSongService,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val favouriteSongsService by LazyExtractor(favouriteSongsService)
    private val customSongService by LazyExtractor(customSongService)
    private val playlistService by LazyExtractor(playlistService)
    private val layoutController by LazyExtractor(layoutController)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val songDetailsService by LazyExtractor(songDetailsService)
    private val publishSongService by LazyExtractor(publishSongService)
    private val adminService by LazyExtractor(adminService)
    private val antechamberService by LazyExtractor(antechamberService)
    private val shareSongService by LazyExtractor(shareSongService)

    private var allActions: SimpleCache<List<SongContextAction>> =
        SimpleCache { createAllActions() }

    private fun createAllActions(): List<SongContextAction> {
        val actions = mutableListOf(
            SongContextAction(R.string.action_song_edit,
                availableCondition = { song -> song.isCustom() },
                executor = { song ->
                    customSongService.showEditSongScreen(song)
                }),
            SongContextAction(R.string.action_remove_from_this_playlist,
                availableCondition = { song ->
                    playlistService.isSongOnCurrentPlaylist(song)
                },
                executor = { song ->
                    playlistService.removeFromThisPlaylist(song)
                }),
            SongContextAction(R.string.action_add_to_playlist,
                availableCondition = { true },
                executor = { song ->
                    playlistService.showAddSongToPlaylistDialog(song)
                }),
            SongContextAction(R.string.action_song_set_favourite,
                availableCondition = { song ->
                    !favouriteSongsService.isSongFavourite(song)
                            && !layoutController.isState(SongPreviewLayoutController::class)
                },
                executor = { song ->
                    favouriteSongsService.setSongFavourite(song)
                }),
            SongContextAction(R.string.action_song_unset_favourite,
                availableCondition = { song ->
                    favouriteSongsService.isSongFavourite(song)
                            && !layoutController.isState(SongPreviewLayoutController::class)
                },
                executor = { song ->
                    favouriteSongsService.unsetSongFavourite(song)
                }),
            SongContextAction(R.string.action_song_remove,
                availableCondition = { song -> song.isCustom() },
                executor = { song ->
                    ConfirmDialogBuilder().confirmAction(R.string.confirm_remove_song) {
                        customSongService.removeSong(song)
                    }
                }),
            SongContextAction(R.string.action_song_copy,
                availableCondition = { true },
                executor = { song ->
                    customSongService.copySongAsCustom(song)
                }),
            SongContextAction(R.string.action_share_song,
                availableCondition = { true },
                executor = { song ->
                    shareSongService.shareSong(song)
                }),
            SongContextAction(R.string.action_song_publish,
                availableCondition = { song -> song.isCustom() },
                executor = { song ->
                    publishSongService.publishSong(song)
                }),
            SongContextAction(R.string.export_content_to_file,
                availableCondition = { song -> song.isCustom() },
                executor = { song ->
                    customSongService.exportSong(song)
                }),
            SongContextAction(R.string.song_details_title,
                availableCondition = { !layoutController.isState(SongPreviewLayoutController::class) },
                executor = { song ->
                    songDetailsService.showSongDetails(song)
                }),
            SongContextAction(R.string.song_show_fullscreen,
                availableCondition = { layoutController.isState(SongPreviewLayoutController::class) },
                executor = {
                    songPreviewLayoutController.toggleFullscreen()
                }),
            SongContextAction(R.string.admin_antechamber_edit_action,
                availableCondition = { adminService.isAdminEnabled() },
                executor = { song ->
                    customSongService.showEditSongScreen(song)
                }),
            SongContextAction(R.string.admin_song_content_update_action,
                availableCondition = { song -> song.isPublic() && adminService.isAdminEnabled() },
                executor = { song ->
                    adminService.updatePublicSongUi(song)
                }),
            SongContextAction(R.string.admin_antechamber_update_action,
                availableCondition = { song -> song.isAntechamber() && adminService.isAdminEnabled() },
                executor = { song ->
                    antechamberService.updateAntechamberSongUI(song)
                }),
            SongContextAction(R.string.admin_antechamber_approve_action,
                availableCondition = { song -> song.isAntechamber() && adminService.isAdminEnabled() },
                executor = { song ->
                    antechamberService.approveAntechamberSongUI(song)
                }),
            SongContextAction(R.string.admin_antechamber_approve_action,
                availableCondition = { song -> song.isCustom() && adminService.isAdminEnabled() },
                executor = { song ->
                    antechamberService.approveCustomSongUI(song)
                }),
            SongContextAction(R.string.admin_antechamber_delete_action,
                availableCondition = { song -> song.isAntechamber() && adminService.isAdminEnabled() },
                executor = { song ->
                    antechamberService.deleteAntechamberSongUI(song)
                }),
            SongContextAction(R.string.admin_update_rank,
                availableCondition = { song -> song.isPublic() && adminService.isAdminEnabled() },
                executor = { song ->
                    adminService.updateRankDialog(song)
                }),
        )

        actions.forEach { action ->
            action.displayName = uiResourceService.resString(action.displayNameResId)
        }
        return actions
    }

    fun showSongActions(song: Song) {
        GlobalScope.launch(Dispatchers.Main) {
            val songActions = allActions.get()
                .filter { action -> action.availableCondition(song) }
            val actionNames = songActions.map { action -> action.displayName }.toTypedArray()

            val builder = AlertDialog.Builder(activity)
            builder.setItems(actionNames) { _, item ->
                safeExecute {
                    songActions[item].executor(song)
                }
            }

            val alert = builder.create()
            if (!activity.isFinishing) {
                alert.show()
            }
        }
    }

}