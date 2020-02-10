package igrek.songbook.songselection.tree

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.MainLayout
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.SongsDb
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.search.SongSearchLayoutController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

open class SongTreeLayoutController : SongSelectionLayoutController(), MainLayout {

    @Inject
    lateinit var scrollPosBuffer: Lazy<ScrollPosBuffer>

    var currentCategory: Category? = null
    private var toolbarTitle: TextView? = null
    private var goBackButton: ImageButton? = null
    private var searchSongButton: ImageButton? = null

    private var subscriptions = mutableListOf<Disposable>()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton?.setOnClickListener { onBackClicked() }

        searchSongButton = layout.findViewById(R.id.searchSongButton)
        searchSongButton?.setOnClickListener { goToSearchSong() }

        toolbarTitle = layout.findViewById(R.id.toolbarTitle)

        itemsListView!!.init(activity, this)
        updateSongItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (layoutController.isState(this::class))
                        updateSongItemsList()
                })
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_song_tree
    }

    override fun onBackClicked() {
        goUp()
    }

    private fun isCategorySelected(): Boolean {
        return currentCategory != null
    }

    override fun updateSongItemsList() {
        // reload current category
        if (isCategorySelected()) {
            val songsDb = songsRepository.songsDb!!
            currentCategory = songsDb.categories.firstOrNull { category -> category.id == currentCategory?.id }
        }
        super.updateSongItemsList()
        if (isCategorySelected()) {
            goBackButton!!.visibility = View.VISIBLE
            setTitle(currentCategory?.displayName)
        } else {
            goBackButton!!.visibility = View.GONE
            setTitle(uiResourceService.resString(R.string.nav_categories_list))
        }
        restoreScrollPosition(currentCategory)
    }

    override fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        return if (isCategorySelected()) {
            // selected category
            currentCategory!!.getUnlockedSongs()
                    .asSequence()
                    .map { song -> SongTreeItem.song(song) }
                    .toMutableList()
        } else {
            // all categories apart from custom
            songsDb.generalCategories.get()
                    .asSequence()
                    .map { category -> SongTreeItem.category(category) }
                    .toMutableList()
        }
    }

    private fun setTitle(title: String?) {
        actionBar!!.title = title
        toolbarTitle!!.text = title
    }

    private fun goToSearchSong() {
        layoutController.showLayout(SongSearchLayoutController::class)
    }

    private fun goUp() {
        try {
            if (currentCategory == null)
                throw NoParentItemException()
            // go to all categories
            currentCategory = null
            updateSongItemsList()
        } catch (e: NoParentItemException) {
            activityController.get().quit()
        }
    }

    private fun storeScrollPosition() {
        scrollPosBuffer.get().storeScrollPosition(currentCategory, itemsListView?.currentScrollPosition)
    }

    private fun restoreScrollPosition(category: Category?) {
        if (scrollPosBuffer.get().hasScrollPositionStored(category)) {
            itemsListView?.restoreScrollPosition(scrollPosBuffer.get().restoreScrollPosition(category))
        }
    }

    override fun onSongItemClick(item: SongTreeItem) {
        storeScrollPosition()
        if (item.isCategory) {
            // go to category
            currentCategory = item.category
            updateSongItemsList()
            // scroll to beginning
            itemsListView?.scrollToBeginning()
        } else {
            openSongPreview(item)
        }
    }

    override fun onLayoutExit() {}
}

