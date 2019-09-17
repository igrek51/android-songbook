package igrek.songbook.songselection.search

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutState
import igrek.songbook.layout.MainLayout
import igrek.songbook.persistence.general.model.SongsDb
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.SoftKeyboardService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


open class SongSearchLayoutController : SongSelectionLayoutController(), MainLayout {

    private var searchFilterEdit: EditText? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemNameFilter: String? = null
    private var storedScroll: ListScrollPosition? = null
    private var subscriptions = mutableListOf<Disposable>()

    @Inject
    lateinit var softKeyboardService: SoftKeyboardService
    @Inject
    lateinit var songTreeLayoutController: dagger.Lazy<SongTreeLayoutController>

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        searchFilterEdit = layout.findViewById(R.id.searchFilterEdit)
        searchFilterEdit!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFilterSubject.onNext(s.toString())
            }
        })

        if (isFilterSet()) {
            searchFilterEdit!!.setText(itemNameFilter, TextView.BufferType.EDITABLE)
        }
        searchFilterEdit!!.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                softKeyboardService.hideSoftKeyboard(searchFilterEdit)
        }
        searchFilterEdit!!.requestFocus()
        Handler(Looper.getMainLooper()).post {
            softKeyboardService.showSoftKeyboard(searchFilterEdit)
        }

        searchFilterEdit!!.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFilterEdit?.clearFocus()
                softKeyboardService.hideSoftKeyboard(searchFilterEdit)
                return@setOnEditorActionListener true
            }
            false
        }

        val searchFilterClearButton: ImageButton = layout.findViewById(R.id.searchFilterClearButton)
        searchFilterClearButton.setOnClickListener { onClearFilterClicked() }

        itemsListView!!.init(activity, this)
        updateSongItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        // refresh only after some inactive time
        subscriptions.add(searchFilterSubject
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    setSongFilter(searchFilterEdit!!.text.toString())
                })
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (layoutController.isState(getLayoutState()))
                        updateSongItemsList()
                })
    }

    override fun getLayoutState(): LayoutState {
        return LayoutState.SEARCH_SONG
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.song_search
    }

    override fun updateSongItemsList() {
        super.updateSongItemsList()
        // restore Scroll Position
        if (storedScroll != null) {
            Handler(Looper.getMainLooper()).post {
                itemsListView?.restoreScrollPosition(storedScroll)
            }
        }
    }

    private fun setSongFilter(itemNameFilter: String?) {
        this.itemNameFilter = itemNameFilter
        if (itemNameFilter == null)
            searchFilterEdit?.setText("", TextView.BufferType.EDITABLE)
        // reset scroll
        storedScroll = null
        updateSongItemsList()
    }

    override fun getSongItems(songsDb: SongsDb): MutableList<SongTreeItem> {
        if (!isFilterSet()) { // no filter
            return songsDb.songs
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .toMutableList()
        } else {
            val songNameFilter = SongTreeFilter(itemNameFilter)
            // filter songs
            val songsSequence = songsDb.songs
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .filter { item -> songNameFilter.songMatchesNameFilter(item) }
            // filter categories
            val categoriesSequence = songsDb.categories
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
            layoutController.showSongTree()
        }
    }

    private fun onClearFilterClicked() {
        if (isFilterSet()) {
            setSongFilter(null)
        } else {
            softKeyboardService.hideSoftKeyboard(searchFilterEdit)
            layoutController.showSongTree()
        }
    }

    override fun onSongItemClick(item: SongTreeItem) {
        // store Scroll Position
        storedScroll = itemsListView?.currentScrollPosition
        if (item.isSong) {
            openSongPreview(item)
        } else {
            // move to selected category
            songTreeLayoutController.get().currentCategory = item.category
            layoutController.showSongTree()
        }
    }

    override fun onLayoutExit() {}
}
