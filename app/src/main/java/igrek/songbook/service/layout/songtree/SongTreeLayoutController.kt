package igrek.songbook.service.layout.songtree

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.exception.NoParentItemException
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongsDb
import igrek.songbook.service.layout.LayoutState
import igrek.songbook.service.layout.MainLayout
import igrek.songbook.service.layout.SongSelectionLayoutController
import igrek.songbook.service.songtree.ScrollPosBuffer
import igrek.songbook.service.songtree.SongTreeItem
import igrek.songbook.service.songtree.SongTreeWalker
import javax.inject.Inject

class SongTreeLayoutController : SongSelectionLayoutController(), MainLayout {

    @Inject
    lateinit var songTreeWalker: SongTreeWalker
    @Inject
    lateinit var scrollPosBuffer: ScrollPosBuffer

    private var toolbarTitle: TextView? = null
    private var goBackButton: ImageButton? = null

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { _ -> navigationMenuController.navDrawerShow() }

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton!!.setOnClickListener { _ -> onBackClicked() }

        toolbarTitle = layout.findViewById(R.id.toolbarTitle)

        itemsListView!!.init(activity, this)
        updateSongItemsList()

        songsDbRepository.dbChangeSubject.subscribe { _ ->
            if (layoutController.isState(LayoutState.SONGS_TREE))
                updateSongItemsList()
        }
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.SONGS_TREE
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.song_tree
    }

    fun onBackClicked() {
        goUp()
    }

    override fun updateSongItemsList() {
        super.updateSongItemsList()
        if (songTreeWalker.isCategorySelected) {
            goBackButton!!.visibility = View.VISIBLE
            setTitle(songTreeWalker.currentCategory.displayName)
        } else {
            goBackButton!!.visibility = View.INVISIBLE
            setTitle(uiResourceService.resString(R.string.nav_songs_list))
        }
        restoreScrollPosition(songTreeWalker.currentCategory)
    }

    override fun getSongItems(songsDb: SongsDb): List<SongTreeItem> {
        return if (!songTreeWalker.isCategorySelected) {
            // all categories list
            songsDb.getAllUnlockedCategories()
                    .map { category -> SongTreeItem.category(category) }
        } else {
            // selected category
            songTreeWalker.currentCategory.getUnlockedSongs()
                    .map { song -> SongTreeItem.song(song) }
        }
    }

    private fun setTitle(title: String?) {
        actionBar!!.title = title
        toolbarTitle!!.text = title
    }

    private fun goUp() {
        try {
            songTreeWalker.goUp()
            updateSongItemsList()
        } catch (e: NoParentItemException) {
            activityController.get().quit()
        }
    }

    private fun storeScrollPosition() {
        scrollPosBuffer.storeScrollPosition(songTreeWalker.currentCategory, itemsListView!!.currentScrollPosition)
    }

    private fun restoreScrollPosition(category: SongCategory?) {
        val savedScrollPos = scrollPosBuffer.restoreScrollPosition(category)
        if (savedScrollPos != null) {
            itemsListView!!.scrollToPosition(savedScrollPos)
        }
    }

    override fun onSongItemClick(item: SongTreeItem) {
        storeScrollPosition()
        if (item.isCategory) {
            songTreeWalker.goToCategory(item.category)
            updateSongItemsList()
            // scroll to beginning
            itemsListView!!.scrollTo(0)
        } else {
            openSongPreview(item)
        }
    }
}
