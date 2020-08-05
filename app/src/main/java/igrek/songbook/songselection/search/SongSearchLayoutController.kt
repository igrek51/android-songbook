package igrek.songbook.songselection.search

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.persistence.repository.AllSongsRepository
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.send.SendMessageService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.listview.LazySongListView
import igrek.songbook.songselection.listview.ListScrollPosition
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.songselection.tree.SongTreeSorter
import igrek.songbook.system.SoftKeyboardService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class SongSearchLayoutController(
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
        songOpener: LazyInject<SongOpener> = appFactory.songOpener,
        softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
        songTreeLayoutController: LazyInject<SongTreeLayoutController> = appFactory.songTreeLayoutController,
        sendMessageService: LazyInject<SendMessageService> = appFactory.sendMessageService,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_song_search
), SongClickListener {
    private val songsRepository by LazyExtractor(songsRepository)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songTreeLayoutController by LazyExtractor(songTreeLayoutController)
    private val sendMessageService by LazyExtractor(sendMessageService)

    private var itemsListView: LazySongListView? = null
    private var searchFilterEdit: EditText? = null
    private var emptySearchButton: Button? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemNameFilter: String? = null
    private var storedScroll: ListScrollPosition? = null
    private var subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        emptySearchButton = layout.findViewById<Button>(R.id.emptySearchButton)?.apply {
            setOnClickListener {
                sendMessageService.requestMissingSong()
            }
        }

        searchFilterEdit = layout.findViewById<EditText>(R.id.searchFilterEdit)?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchFilterSubject.onNext(s.toString())
                }
            })

            if (isFilterSet()) {
                setText(itemNameFilter, TextView.BufferType.EDITABLE)
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus)
                    softKeyboardService.hideSoftKeyboard(this)
            }
            requestFocus()
            Handler(Looper.getMainLooper()).post {
                softKeyboardService.showSoftKeyboard(this)
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    clearFocus()
                    softKeyboardService.hideSoftKeyboard(this)
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        layout.findViewById<ImageButton>(R.id.searchFilterClearButton)?.run {
            setOnClickListener { onClearFilterClicked() }
        }

        itemsListView = layout.findViewById<LazySongListView>(R.id.itemsList)?.also {
            it.init(activity, this, songContextMenuBuilder)
        }
        updateItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        // refresh only after some inactive time
        subscriptions.add(searchFilterSubject
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setSongFilter(searchFilterEdit?.text?.toString())
                }, UiErrorHandler::handleError))
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (layoutController.isState(this::class))
                        updateItemsList()
                }, UiErrorHandler::handleError))
    }

    private fun updateItemsList() {
        val items: MutableList<SongTreeItem> = getSongItems(songsRepository.allSongsRepo)
        val sortedItems = SongTreeSorter().sort(items)
        itemsListView?.setItems(sortedItems)

        // restore Scroll Position
        if (storedScroll != null) {
            itemsListView?.restoreScrollPosition(storedScroll)
        }

        emptySearchButton?.visibility = when (itemsListView?.count) {
            0 -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setSongFilter(itemNameFilter: String?) {
        this.itemNameFilter = itemNameFilter
        if (itemNameFilter == null)
            searchFilterEdit?.setText("", TextView.BufferType.EDITABLE)
        storedScroll = null
        updateItemsList()
    }

    private fun getSongItems(songsRepo: AllSongsRepository): MutableList<SongTreeItem> {
        if (!isFilterSet()) { // no filter
            return songsRepo.songs.get()
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .toMutableList()
        } else {
            val songNameFilter = SongTreeFilter(itemNameFilter)
            // filter songs
            val songsSequence = songsRepo.songs.get()
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .filter { item -> songNameFilter.songMatchesNameFilter(item) }
            // filter categories
            val categoriesSequence = songsRepo.categories.get()
                    .asSequence()
                    .map { category -> SongTreeItem.category(category) }
                    .filter { item -> songNameFilter.categoryMatchesNameFilter(item) }
            // display union
            return songsSequence.plus(categoriesSequence)
                    .toMutableList()
        }
    }

    private fun isFilterSet(): Boolean {
        if (itemNameFilter.isNullOrEmpty())
            return false
        return itemNameFilter?.length ?: 0 >= 3
    }

    override fun onBackClicked() {
        if (isFilterSet()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    private fun onClearFilterClicked() {
        if (isFilterSet()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    fun openSongPreview(item: SongTreeItem) {
        songOpener.openSongPreview(item.song!!)
    }

    override fun onSongItemClick(item: SongTreeItem) {
        // store Scroll Position
        storedScroll = itemsListView?.currentScrollPosition
        if (item.isSong) {
            openSongPreview(item)
        } else {
            // move to selected category
            songTreeLayoutController.currentCategory = item.category
            layoutController.showLayout(SongTreeLayoutController::class)
        }
    }

    override fun onSongItemLongClick(item: SongTreeItem) {
        if (item.isSong) {
            songContextMenuBuilder.showSongActions(item.song!!)
        } else {
            onSongItemClick(item)
        }
    }
}
