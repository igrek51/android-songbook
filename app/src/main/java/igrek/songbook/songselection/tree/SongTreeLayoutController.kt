package igrek.songbook.songselection.tree

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.spinner.MultiPicker
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.repository.AllSongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.language.SongLanguage
import igrek.songbook.songselection.SongSelectionLayoutController
import igrek.songbook.songselection.search.SongSearchLayoutController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class SongTreeLayoutController(
        scrollPosBuffer: LazyInject<ScrollPosBuffer> = appFactory.scrollPosBuffer,
        appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
) : SongSelectionLayoutController(), MainLayout {
    private val scrollPosBuffer by LazyExtractor(scrollPosBuffer)
    private val appLanguageService by LazyExtractor(appLanguageService)

    var currentCategory: Category? = null
    private var toolbarTitle: TextView? = null
    private var goBackButton: ImageButton? = null
    private var languagePicker: MultiPicker<SongLanguage>? = null
    private var subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        initSongSelectionLayout(layout)

        goBackButton = layout.findViewById(R.id.goBackButton)
        goBackButton?.setOnClickListener { onBackClicked() }

        layout.findViewById<ImageButton>(R.id.searchSongButton)?.run {
            setOnClickListener { goToSearchSong() }
        }

        toolbarTitle = layout.findViewById(R.id.toolbarTitle)

        itemsListView!!.init(activity, this, songContextMenuBuilder)
        updateSongItemsList()

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(songsRepository.dbChangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (layoutController.isState(this::class))
                        updateSongItemsList()
                })

        layout.findViewById<ImageButton>(R.id.languageFilterButton)?.apply {
            val songLanguageEntries = appLanguageService.songLanguageEntries()
            val selected = appLanguageService.selectedSongLanguages
            val title = uiResourceService.resString(R.string.song_languages)
            languagePicker = MultiPicker(
                    activity,
                    entityNames = songLanguageEntries,
                    selected = selected,
                    title = title,
            ) { selectedLanguages ->
                if (appLanguageService.selectedSongLanguages != selectedLanguages) {
                    appLanguageService.selectedSongLanguages = selectedLanguages.toSet()
                    updateSongItemsList()
                }
            }
            setOnClickListener { languagePicker?.showChoiceDialog() }
        }
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
            currentCategory = songsRepository.allSongsRepo.categories.get()
                    .firstOrNull { category -> category.id == currentCategory?.id }
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

    override fun getSongItems(songsRepo: AllSongsRepository): MutableList<SongTreeItem> {
        val acceptedLanguages = appLanguageService.selectedSongLanguages
        val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + ""
        return if (isCategorySelected()) {
            // selected category
            currentCategory?.getUnlockedSongs().orEmpty()
                    .filter { song -> song.language in acceptedLangCodes }
                    .asSequence()
                    .map { song -> SongTreeItem.song(song) }
                    .toMutableList()
        } else {
            // all categories apart from custom
            songsRepo.publicCategories.get()
                    .filter { category ->
                        category.songs.any { song -> song.language in acceptedLangCodes }
                    }
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
            layoutController.showPreviousLayoutOrQuit()
        }
    }

    private fun storeScrollPosition() {
        scrollPosBuffer.storeScrollPosition(currentCategory, itemsListView?.currentScrollPosition)
    }

    private fun restoreScrollPosition(category: Category?) {
        if (scrollPosBuffer.hasScrollPositionStored(category)) {
            itemsListView?.restoreScrollPosition(scrollPosBuffer.restoreScrollPosition(category))
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

