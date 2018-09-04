package igrek.songbook.service.layout.songtree

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.exception.NoParentDirException
import igrek.songbook.logger.LoggerFactory
import igrek.songbook.service.activity.ActivityController
import igrek.songbook.service.filetree.FileItem
import igrek.songbook.service.filetree.FileTreeManager
import igrek.songbook.service.filetree.ScrollPosBuffer
import igrek.songbook.service.info.UiInfoService
import igrek.songbook.service.info.UiResourceService
import igrek.songbook.service.layout.LayoutController
import igrek.songbook.service.layout.LayoutState
import igrek.songbook.service.navmenu.NavigationMenuController
import igrek.songbook.service.preferences.PreferencesService
import igrek.songbook.service.window.WindowManagerService
import igrek.songbook.view.songselection.SongListView
import javax.inject.Inject

class SongTreeController {

    @Inject
    lateinit var fileTreeManager: FileTreeManager
    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var windowManagerService: WindowManagerService
    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var scrollPosBuffer: ScrollPosBuffer
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var homePathService: HomePathService
    @Inject
    lateinit var navigationMenuController: NavigationMenuController

    private val logger = LoggerFactory.getLogger()
    private var actionBar: ActionBar? = null
    private var itemsListView: SongListView? = null
    private var toolbarTitle: TextView? = null

    val currentScrollPos: Int?
        get() = itemsListView!!.currentScrollPosition

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showSongTree(layout: View) {
        val currentDir = fileTreeManager.currentDirName
        val items = fileTreeManager.items

        // toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(false)
            actionBar!!.setDisplayShowHomeEnabled(false)
        }

        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { _ -> navigationMenuController.navDrawerShow() }

        val goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)
        goBackButton.setOnClickListener { _ -> onToolbarBackClickedEvent() }

        toolbarTitle = layout.findViewById(R.id.toolbarTitle)

        itemsListView = layout.findViewById(R.id.filesList)
        itemsListView!!.init(activity)

        updateFileList(currentDir, items)
    }

    fun updateFileList(currentDir: String?, items: List<FileItem>) {
        setTitle(currentDir)
        //lista elementów
        itemsListView!!.setItems(items)
    }

    fun scrollToItem(position: Int) {
        itemsListView!!.scrollTo(position)
    }

    fun setTitle(title: String?) {
        actionBar!!.title = title
        toolbarTitle!!.text = title
    }

    fun scrollToPosition(y: Int) {
        itemsListView!!.scrollToPosition(y)
    }

    fun goUp() {
        try {
            fileTreeManager.goUp()
            updateFileList()
            //scrollowanie do ostatnio otwartego folderu
            restoreScrollPosition(fileTreeManager.currentPath)
        } catch (e: NoParentDirException) {
            activityController.get().quit()
        }

    }

    private fun updateFileList() {
        updateFileList(fileTreeManager.currentDirName, fileTreeManager.items)
        layoutController.setState(LayoutState.SONGS_LIST)
    }

    private fun showFileContent(filename: String) {
        layoutController.setState(LayoutState.SONG_PREVIEW)
        fileTreeManager.currentFileName = filename
        layoutController.showSongPreview()
        windowManagerService.keepScreenOn(true)
    }

    fun restoreScrollPosition(path: String) {
        val savedScrollPos = scrollPosBuffer.restoreScrollPosition(path)
        if (savedScrollPos != null) {
            scrollToPosition(savedScrollPos)
        }
    }

    fun onToolbarBackClickedEvent() {
        goUp()
    }

    fun onItemClickedEvent(item: FileItem) {
        scrollPosBuffer.storeScrollPosition(fileTreeManager.currentPath, currentScrollPos)
        if (item.isDirectory) {
            fileTreeManager.goInto(item.name)
            updateFileList()
            scrollToItem(0)
        } else {
            showFileContent(item.name)
        }
    }

    fun setHomePath() {
        homePathService.homePath = fileTreeManager.currentPath
        uiInfoService.showInfo(R.string.starting_directory_saved, R.string.action_info_ok)
    }

    private fun homeClicked() {
        if (homePathService.isInHomeDir(fileTreeManager.currentPath)) {
            activityController.get().quit()
        } else {
            val homePath = homePathService.homePath
            if (homePath == null) {
                uiInfoService.showInfo(R.string.message_home_not_set)
            } else {
                fileTreeManager.goTo(homePath)
                updateFileList()
            }
        }
    }
}
