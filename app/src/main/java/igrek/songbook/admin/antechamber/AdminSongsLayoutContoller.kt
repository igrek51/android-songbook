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
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
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

    private var itemsListView: AntechamberSongListView? = null
    private var experimentalSongs: List<AntechamberSong> = emptyList()

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
        experimentalSongs = allDtos.toModel()
        Handler(Looper.getMainLooper()).post {
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

    private fun onSongClick(item: AntechamberSong) {
        val song = Song(
                id = item.id,
                title = item.title,
                categories = mutableListOf(),
                content = item.content,
                versionNumber = item.versionNumber,
                createTime = item.createTime,
                updateTime = item.updateTime,
                custom = true,
                comment = item.comment,
                preferredKey = item.preferredKey,
                author = item.author,
                state = SongStatus.PROPOSED,
                customCategoryName = item.categoryName,
                language = item.language,
                metre = item.metre,
                scrollSpeed = item.scrollSpeed,
                initialDelay = item.initialDelay,
                chordsNotation = item.chordsNotation,
                originalSongId = item.originalSongId,
                namespace = SongNamespace.Antechamber
        )

        songOpener.openSongPreview(song)
    }

    private fun onSongLongClick(item: AntechamberSong) {
        onMoreMenu(item)
    }

    private fun onMoreMenu(item: AntechamberSong) {
        TODO("not implemented")
//        songContextMenuBuilder.showSongActions(item.song!!)
    }
}
