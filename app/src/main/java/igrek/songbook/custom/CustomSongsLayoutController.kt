package igrek.songbook.custom

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.custom.list.CustomSongListItem
import igrek.songbook.custom.list.CustomSongListView
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomCategory
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.NoParentItemException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class CustomSongsLayoutController : InflatedLayout(
        _layoutResourceId = R.layout.screen_custom_songs,
        _layoutState = LayoutState.CUSTOM_SONGS
), ListItemClickListener<CustomSongListItem> {

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
    lateinit var customSongService: Lazy<CustomSongService>

    private var itemsListView: CustomSongListView? = null
    private var goBackButton: ImageButton? = null
    private var storedScroll: ListScrollPosition? = null
    private var tabTitleLabel: TextView? = null
    private var emptyListLabel: TextView? = null

    private var customCategory: CustomCategory? = null

    private var subscriptions = mutableListOf<Disposable>()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        val addCustomSongButton: ImageButton = layout.findViewById(R.id.addCustomSongButton)
        addCustomSongButton.setOnClickListener { addCustomSong() }

        tabTitleLabel = layout.findViewById(R.id.tabTitleLabel)
        emptyListLabel = layout.findViewById(R.id.emptyListLabel)

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton?.setOnClickListener { goUp() }

        itemsListView = layout.findViewById(R.id.itemsListView)
        itemsListView!!.init(activity, this)
        updateItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (isLayoutVisible())
                        updateItemsList()
                })
    }

    private fun addCustomSong() {
        customSongService.get().showAddSongScreen()
    }

    private fun updateItemsList() {
        val groupingEnabled = customSongService.get().customSongsGroupCategories

        if (groupingEnabled) {
            itemsListView!!.items = if (customCategory == null) {
                val categories = songsRepository.customSongsDao.customCategories.map {
                    CustomSongListItem(customCategory = it)
                }
                val uncategorized = songsRepository.customSongsDao.customSongsUncategorized.map {
                    CustomSongListItem(song = it)
                }
                categories + uncategorized
            } else {
                customCategory!!.songs.map {
                    CustomSongListItem(song = it)
                }
            }
        } else {
            itemsListView!!.items = songsRepository.songsDb?.customSongs?.get()?.map {
                CustomSongListItem(song = it)
            } ?: emptyList()
        }

        if (storedScroll != null) {
            Handler(Looper.getMainLooper()).post {
                itemsListView?.restoreScrollPosition(storedScroll)
            }
        }

        val customSongsTitle = uiResourceService.resString(R.string.nav_custom_song)
        tabTitleLabel?.text = when (customCategory) {
            null -> customSongsTitle
            else -> "$customSongsTitle: ${customCategory?.name}"
        }

        emptyListLabel!!.visibility = if (itemsListView!!.count == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }

        goBackButton?.visibility = when (customCategory) {
            null -> View.GONE
            else -> View.VISIBLE
        }
    }

    override fun onBackClicked() {
        goUp()
    }

    private fun goUp() {
        try {
            if (customCategory == null)
                throw NoParentItemException()
            customCategory = null
            updateItemsList()
        } catch (e: NoParentItemException) {
            layoutController.showSongTree()
        }
    }

    override fun onItemClick(item: CustomSongListItem) {
        storedScroll = itemsListView?.currentScrollPosition
        if (item.song != null) {
            songOpener.openSongPreview(item.song)
        } else if (item.customCategory != null) {
            customCategory = item.customCategory
            updateItemsList()
        }
    }

    override fun onItemLongClick(item: CustomSongListItem) {
        onMoreActions(item)
    }

    override fun onMoreActions(item: CustomSongListItem) {
        item.song?.let {
            songContextMenuBuilder.showSongActions(it)
        }
    }

}
