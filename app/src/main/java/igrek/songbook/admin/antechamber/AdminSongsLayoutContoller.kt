package igrek.songbook.admin.antechamber


import android.view.View
import android.widget.Button
import igrek.songbook.R
import igrek.songbook.custom.CustomSongService
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.songpreview.SongOpener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class AdminSongsLayoutContoller(
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
    antechamberService: LazyInject<AntechamberService> = appFactory.antechamberService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_admin_songs
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songOpener by LazyExtractor(songOpener)
    private val customSongService by LazyExtractor(customSongService)
    private val antechamberService by LazyExtractor(antechamberService)

    private var itemsListView: AntechamberSongListView? = null
    private var experimentalSongs: MutableList<Song> = mutableListOf()

    private var subscriptions = mutableListOf<Disposable>()
    var fetchRequestSubject: PublishSubject<Boolean> = PublishSubject.create()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        itemsListView = layout.findViewById(R.id.itemsList)
        itemsListView!!.init(
            activity,
            onClick = this::onSongClick,
            onLongClick = this::onSongLongClick,
            onMore = this::onMoreMenu
        )
        updateItemsList()

        layout.findViewById<Button>(R.id.updateButton).setOnClickListener {
            downloadSongs()
        }

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(fetchRequestSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                GlobalScope.launch(Dispatchers.Main) {
                    val result = antechamberService.downloadSongsAsync().await()
                    result.fold(onSuccess = { downloadedSongs ->
                        experimentalSongs = downloadedSongs.toMutableList()
                    }, onFailure = { e ->
                        logger.error(e)
                    })
                }
            }, UiErrorHandler::handleError)
        )
    }

    private fun updateItemsList() {
        itemsListView?.setItems(experimentalSongs)
    }

    private fun downloadSongs() {
        uiInfoService.showInfo(R.string.admin_downloading_antechamber, indefinite = true)
        GlobalScope.launch(Dispatchers.Main) {
            val result = antechamberService.downloadSongsAsync().await()
            result.fold(onSuccess = { downloadedSongs ->
                experimentalSongs = downloadedSongs.toMutableList()
                uiInfoService.showInfo(R.string.admin_downloaded_antechamber)
                updateItemsList()
            }, onFailure = { e ->
                UiErrorHandler.handleError(e, R.string.admin_communication_breakdown)
            })
        }
    }

    private fun onSongClick(song: Song) {
        songOpener.openSongPreview(song)
    }

    private fun onSongLongClick(song: Song) {
        onMoreMenu(song)
    }

    private fun onMoreMenu(song: Song) {
        ContextMenuBuilder().showContextMenu(generateMenuOptions(song))
    }

    private fun deleteAntechamberSongUI(song: Song) {
        val message1 =
            uiResourceService.resString(R.string.admin_antechamber_confirm_delete, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfo(R.string.admin_sending, indefinite = true)
            GlobalScope.launch(Dispatchers.Main) {
                val result = antechamberService.deleteAntechamberSongAsync(song).await()
                result.fold(onSuccess = {
                    experimentalSongs.remove(song)
                    uiInfoService.showInfo(R.string.admin_success)
                    updateItemsList()
                }, onFailure = { e ->
                    UiErrorHandler.handleError(e, R.string.admin_communication_breakdown)
                })
            }
        }
    }

    private fun generateMenuOptions(song: Song): List<ContextMenuBuilder.Action> {
        return listOf(
            ContextMenuBuilder.Action(R.string.admin_antechamber_edit_action) {
                customSongService.showEditSongScreen(song)
            },
            ContextMenuBuilder.Action(R.string.admin_antechamber_update_action) {
                antechamberService.updateAntechamberSongUI(song)
            },
            ContextMenuBuilder.Action(R.string.admin_antechamber_approve_action) {
                antechamberService.approveAntechamberSongUI(song)
            },
            ContextMenuBuilder.Action(R.string.admin_antechamber_delete_action) {
                deleteAntechamberSongUI(song)
            }
        )
    }
}
