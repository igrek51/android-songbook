package igrek.songbook.admin.antechamber

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import okhttp3.*
import java.io.IOException
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
    lateinit var contextMenuBuilder: ContextMenuBuilder
    @Inject
    lateinit var customSongService: CustomSongService

    private var itemsListView: AntechamberSongListView? = null
    private var experimentalSongs: List<Song> = emptyList()

    companion object {
        private const val apiUrl = "https://antechamber.chords.igrek.dev/api/v4"
        private const val getAllSongsUrl = "$apiUrl/songs"
        private const val authTokenHeader = "X-Auth-Token"
    }

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

        val request: Request = Request.Builder()
                .url(getAllSongsUrl)
                .header(authTokenHeader, adminService.get().userAuthToken)
                .build()

        okHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    onDownloadedSongs(response)
                }
            }
        })
    }

    private fun onDownloadedSongs(response: Response) {
        val json = response.body()?.string() ?: ""
        val mapper = jacksonObjectMapper()
        val allDtos: AllAntechamberSongsDto = mapper.readValue(json)
        logger.debug("downloaded songs: ", allDtos)
        val antechamberSongs = allDtos.toModel()
        experimentalSongs = antechamberSongs.map { antechamberSong -> antechamberSong.toSong() }
        Handler(Looper.getMainLooper()).post {
            uiInfoService.showInfo(R.string.admin_downloaded_antechamber)
            updateItemsList()
        }
    }

    private fun onErrorReceived(errorMessage: String?) {
        logger.error("Contact message sending error: $errorMessage")
        Handler(Looper.getMainLooper()).post {
            val message = uiResourceService.resString(R.string.admin_communication_breakdown, errorMessage)
            uiInfoService.showInfoIndefinite(message)
        }
    }

    private fun onSongClick(song: Song) {
        songOpener.openSongPreview(song)
    }

    private fun onSongLongClick(song: Song) {
        onMoreMenu(song)
    }

    private fun onMoreMenu(song: Song) {
        val actions = listOf(
                ContextMenuBuilder.Action(R.string.admin_antechamber_edit_action) {
                    customSongService.showEditSongScreen(song)
                },
                ContextMenuBuilder.Action(R.string.admin_antechamber_update_action) {

                },
                ContextMenuBuilder.Action(R.string.admin_antechamber_approve_action) {

                },
                ContextMenuBuilder.Action(R.string.admin_antechamber_delete_action) {

                }
        )
        contextMenuBuilder.showContextMenu(actions)
    }
}
