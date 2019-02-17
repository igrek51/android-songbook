package igrek.songbook.songselection.search

import android.os.Handler
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
import igrek.songbook.persistence.songsdb.SongsDb
import igrek.songbook.songselection.ListScrollPosition
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.SoftKeyboardService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


open class SongSearchLayoutController : SongSelectionLayoutController(), MainLayout {

    private var searchFilterEdit: EditText? = null
    private var searchFilterSubject: PublishSubject<String> = PublishSubject.create()
    private var itemNameFilter: String? = null
    private var storedScroll: ListScrollPosition? = null

    @Inject
    lateinit var softKeyboardService: SoftKeyboardService
    @Inject
    lateinit var songTreeLayoutController: dagger.Lazy<SongTreeLayoutController>

    init {
        DaggerIoc.getFactoryComponent().inject(this)
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
        // refresh only after some inactive time
        searchFilterSubject.debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { setSongFilter(searchFilterEdit!!.text.toString()) }
        if (isFilterSet()) {
            searchFilterEdit!!.setText(itemNameFilter, TextView.BufferType.EDITABLE)
        }
        searchFilterEdit!!.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                softKeyboardService.hideSoftKeyboard(searchFilterEdit)
        }
        searchFilterEdit!!.requestFocus()
        Handler().post { softKeyboardService.showSoftKeyboard(searchFilterEdit) }

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

        songsRepository.dbChangeSubject.subscribe {
            if (layoutController.isState(layoutState))
                updateSongItemsList()
        }
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
            Handler().post { itemsListView?.restoreScrollPosition(storedScroll) }
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
            return songsDb.getAllUnlockedSongs()
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .toMutableList()
        } else {
            val songNameFilter = SongTreeFilter(itemNameFilter)
            // filter songs
            val songsSequence = songsDb.getAllUnlockedSongs()
                    .asSequence()
                    .map { song -> SongSearchItem.song(song) }
                    .filter { item -> songNameFilter.songMatchesNameFilter(item) }
            // filter categories
            val categoriesSequence = songsDb.getAllUnlockedCategories()
                    .asSequence()
                    .map { category -> SongTreeItem.category(category) }
                    .filter { item -> songNameFilter.categoryMatchesNameFilter(item) }
            // display union
            return songsSequence.plus(categoriesSequence)
                    .toMutableList()
        }
    }

    private fun isFilterSet(): Boolean {
        return itemNameFilter != null && !itemNameFilter!!.isEmpty()
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
            songTreeLayoutController.get().setCurrentCategory(item.category)
            layoutController.showSongTree()
        }
    }

    override fun onLayoutExit() {}
}
