package igrek.songbook.admin.antechamber

import android.view.View
import android.widget.Button
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
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

    private var itemsListView: AntechamberSongListView? = null
    private var experimentalSongs: List<AntechamberSong> = emptyList()

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

    private fun downloadSongs() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun updateItemsList() {
        itemsListView?.setItems(experimentalSongs)
    }

    override fun onBackClicked() {
        layoutController.showSongTree()
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
