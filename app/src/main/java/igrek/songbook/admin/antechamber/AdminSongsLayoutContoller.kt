package igrek.songbook.admin.antechamber

import android.view.View
import android.widget.Button
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.AdminService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.OkHttpClient
import javax.inject.Inject


class AdminSongsLayoutContoller : InflatedLayout(
        _layoutResourceId = R.layout.screen_admin_songs
) {

    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var songContextMenuBuilder: SongContextMenuBuilder
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songOpener: SongOpener
    @Inject
    lateinit var okHttpClient: Lazy<OkHttpClient>
    @Inject
    lateinit var adminService: Lazy<AdminService>
    @Inject
    lateinit var customSongService: CustomSongService
    @Inject
    lateinit var antechamberService: AntechamberService

    private var itemsListView: AntechamberSongListView? = null
    private var experimentalSongs: MutableList<Song> = mutableListOf()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        itemsListView = layout.findViewById(R.id.itemsList)
        itemsListView!!.init(activity,
                onClick = this::onSongClick,
                onLongClick = this::onSongLongClick,
                onMore = this::onMoreMenu)
        updateItemsList()

        layout.findViewById<Button>(R.id.updateButton).setOnClickListener {
            downloadSongs()
        }
    }

    private fun updateItemsList() {
        itemsListView?.setItems(experimentalSongs)
    }

    private fun downloadSongs() {
        uiInfoService.showInfoIndefinite(R.string.admin_downloading_antechamber)
        antechamberService.downloadSongs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ downloadedSongs ->
                    experimentalSongs = downloadedSongs.toMutableList()
                    uiInfoService.showInfo(R.string.admin_downloaded_antechamber)
                    updateItemsList()
                }, { error ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                    uiInfoService.showInfoIndefinite(message)
                })
    }

    private fun onSongClick(song: Song) {
        songOpener.openSongPreview(song)
    }

    private fun onSongLongClick(song: Song) {
        onMoreMenu(song)
    }

    private fun onMoreMenu(song: Song) {
        ContextMenuBuilder().showContextMenu(listOf(
                ContextMenuBuilder.Action(R.string.admin_antechamber_edit_action) {
                    customSongService.showEditSongScreen(song)
                },
                ContextMenuBuilder.Action(R.string.admin_antechamber_update_action) {
                    updateAntechamberSong(song)
                },
                ContextMenuBuilder.Action(R.string.admin_antechamber_approve_action) {
                    ConfirmDialogBuilder().confirmAction(R.string.admin_antechamber_confirm_approve) {
                        // Todo
                    }
                },
                ContextMenuBuilder.Action(R.string.admin_antechamber_delete_action) {
                    ConfirmDialogBuilder().confirmAction(R.string.admin_antechamber_confirm_delete) {
                        deleteAntechamberSong(song)
                    }
                }
        ))
    }

    private fun updateAntechamberSong(song: Song) {
        uiInfoService.showInfoIndefinite(R.string.admin_sending)
        antechamberService.updateAntechamberSong(song)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiInfoService.showInfo(R.string.admin_success)
                }, { error ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                    uiInfoService.showInfoIndefinite(message)
                })
    }

    private fun deleteAntechamberSong(song: Song) {
        uiInfoService.showInfoIndefinite(R.string.admin_sending)
        antechamberService.deleteAntechamberSong(song)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    experimentalSongs.remove(song)
                    uiInfoService.showInfo(R.string.admin_success)
                    updateItemsList()
                }, { error ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                    uiInfoService.showInfoIndefinite(message)
                })
    }
}
