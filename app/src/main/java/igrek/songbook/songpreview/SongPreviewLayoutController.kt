package igrek.songbook.songpreview

import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.View
import android.view.View.OVER_SCROLL_ALWAYS
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import igrek.songbook.R
import igrek.songbook.chords.diagram.ChordsDiagramsService
import igrek.songbook.chords.lyrics.LyricsLoader
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songpreview.renderer.OverlayRecyclerAdapter
import igrek.songbook.songpreview.renderer.SongPreview
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.favourite.FavouriteSongsService
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.WindowManagerService
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class SongPreviewLayoutController(
        lyricsLoader: LazyInject<LyricsLoader> = appFactory.lyricsLoader,
        lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
        navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
        quickMenuTranspose: LazyInject<QuickMenuTranspose> = appFactory.quickMenuTranspose,
        quickMenuAutoscroll: LazyInject<QuickMenuAutoscroll> = appFactory.quickMenuAutoscroll,
        autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
        softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
        songDetailsService: LazyInject<SongDetailsService> = appFactory.songDetailsService,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        favouriteSongsService: LazyInject<FavouriteSongsService> = appFactory.favouriteSongsService,
        songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        chordsDiagramsService: LazyInject<ChordsDiagramsService> = appFactory.chordsDiagramsService,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) : MainLayout {
    private val lyricsLoader by LazyExtractor(lyricsLoader)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val layoutController by LazyExtractor(layoutController)
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val activity by LazyExtractor(appCompatActivity)
    private val quickMenuTranspose by LazyExtractor(quickMenuTranspose)
    private val quickMenuAutoscroll by LazyExtractor(quickMenuAutoscroll)
    private val autoscrollService by LazyExtractor(autoscrollService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songDetailsService by LazyExtractor(songDetailsService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val favouriteSongsService by LazyExtractor(favouriteSongsService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songsRepository by LazyExtractor(songsRepository)
    private val chordsDiagramsService by LazyExtractor(chordsDiagramsService)
    private val preferencesState by LazyExtractor(preferencesState)

    var songPreview: SongPreview? = null
        private set
    var currentSong: Song? = null
    private var overlayAdapter: OverlayRecyclerAdapter? = null
    private var overlayRecyclerView: RecyclerView? = null
    private var songTitleLabel: TextView? = null
    private var fullscreen = false
    private var appBarLayout: AppBarLayout? = null
    private var disableFullscreenButton: FloatingActionButton? = null
    private var transposeButton: ImageButton? = null
    private var autoscrollButton: ImageButton? = null
    private var setFavouriteButton: ImageButton? = null

    val isQuickMenuVisible: Boolean
        get() = quickMenuTranspose.isVisible || quickMenuAutoscroll.isVisible

    init {
        autoscrollService.get().scrollStateSubject
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    highlightPanelButtons()
                }
        favouriteSongsService.get().updateFavouriteSongSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    updateFavouriteButton()
                }
    }

    override fun showLayout(layout: View) {
        if (preferencesState.keepScreenOn)
            windowManagerService.keepScreenOn(true)

        softKeyboardService.hideSoftKeyboard()

        // Toolbar
        val toolbar1 = layout.findViewById<Toolbar>(R.id.toolbar1)
        activity.setSupportActionBar(toolbar1)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }
        // navigation menu button
        val navMenuButton = layout.findViewById<ImageButton>(R.id.navMenuButton)
        navMenuButton.setOnClickListener { navigationMenuController.navDrawerShow() }

        appBarLayout = layout.findViewById(R.id.appBarLayout)

        // create songPreview
        songPreview = SongPreview(activity).apply {
            reset()
        }
        val songPreviewContainer = layout.findViewById<ViewGroup>(R.id.songPreviewContainer)
        songPreviewContainer.addView(songPreview)

        // TODO lazy load quick menu panels - load on use only
        // create quick menu panels
        val quickMenuContainer = layout.findViewById<FrameLayout>(R.id.quickMenuContainer)
        val inflater = activity.layoutInflater
        // transpose panel
        val quickMenuTransposeView = inflater.inflate(R.layout.component_quick_menu_transpose, null)
        quickMenuTransposeView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        quickMenuContainer.addView(quickMenuTransposeView)
        quickMenuTranspose.setQuickMenuView(quickMenuTransposeView)
        quickMenuTranspose.isVisible = false
        // autoscroll panel
        val quickMenuAutoscrollView = inflater.inflate(R.layout.component_quick_menu_autoscroll, null)
        quickMenuAutoscrollView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        quickMenuContainer.addView(quickMenuAutoscrollView)
        quickMenuAutoscroll.setQuickMenuView(quickMenuAutoscrollView)
        quickMenuAutoscroll.isVisible = false

        // overlaying RecyclerView
        overlayRecyclerView = activity.findViewById<RecyclerView>(R.id.overlayRecyclerView).apply {
            setHasFixedSize(true) // improve performance
            layoutManager = LinearLayoutManager(activity)
            songPreview?.let { songPreview ->
                overlayAdapter = OverlayRecyclerAdapter(songPreview)
            }
            adapter = overlayAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    songPreview?.run {
                        scrollByPx(dy.toFloat())
                        onManuallyScrolled(dy)
                    }
                }
            })
            isVerticalScrollBarEnabled = false
            overScrollMode = OVER_SCROLL_ALWAYS
            setOnClickListener { songPreview?.onClick() }
            setOnTouchListener(songPreview)
        }
        resetOverlayScroll()

        songTitleLabel = layout.findViewById<TextView>(R.id.songTitleLabel)?.apply {
            text = currentSong?.displayName().orEmpty()
        }

        val goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)
        goBackButton.setOnClickListener { onBackClicked() }

        transposeButton = layout.findViewById<ImageButton>(R.id.transposeButton)?.apply {
            setOnClickListener { toggleTransposePanel() }
        }

        autoscrollButton = layout.findViewById<ImageButton>(R.id.autoscrollButton)?.apply {
            setOnClickListener { toggleAutoscrollPanel() }
        }

        setFavouriteButton = layout.findViewById<ImageButton>(R.id.setFavouriteButton)?.apply {
            setOnClickListener { toggleSongFavourite() }
        }
        updateFavouriteButton()

        layout.findViewById<ImageButton>(R.id.chordsHelpButton)?.run {
            setOnClickListener { showChordsGraphs() }
        }

        layout.findViewById<ImageButton>(R.id.songInfoButton)?.run {
            setOnClickListener {
                currentSong?.let {
                    songDetailsService.showSongDetails(it)
                }
            }
        }

        layout.findViewById<ImageButton>(R.id.fullscreenButton)?.run {
            setOnClickListener { setFullscreen(true) }
        }

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.run {
            setOnClickListener { showMoreActions() }
        }

        disableFullscreenButton = layout.findViewById<FloatingActionButton>(R.id.disableFullscreenButton)?.apply {
            setOnClickListener { setFullscreen(false) }
        }
        setFullscreen(false)
    }

    private fun showMoreActions() {
        currentSong?.let {
            songContextMenuBuilder.showSongActions(it)
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_song_preview
    }

    fun onGraphicsInitializedEvent(w: Int, paint: Paint?) {
        currentSong?.let {
            // load file and parse it
            val fileContent = it.content.orEmpty()
            val srcNotation = it.chordsNotation ?: ChordsNotation.default
            val transposed = songsRepository.transposeDao.getSongTransposition(it.songIdentifier())
            // initialize - first file loading
            lyricsLoader.load(fileContent, w, paint, transposed, srcNotation)

            songPreview?.setFontSizes(lyricsThemeService.fontsize)
            songPreview?.setCRDModel(lyricsLoader.lyricsModel)
            resetOverlayScroll()
        }
    }

    private fun resetOverlayScroll() {
        // refresh
        overlayRecyclerView?.layoutManager?.scrollToPosition(1)
        overlayRecyclerView?.scrollToPosition(1)
    }

    fun onLyricsModelUpdated() {
        songPreview?.setCRDModel(lyricsLoader.lyricsModel)
        resetOverlayScroll()
        highlightPanelButtons()
    }

    private fun highlightPanelButtons() {
        transposeButton?.let { transposeButton ->
            if (quickMenuTranspose.isFeatureActive) {
                highlightButton(transposeButton)
            } else {
                unhighlightButton(transposeButton)
            }
        }
        autoscrollButton?.let { autoscrollButton ->
            if (quickMenuAutoscroll.isFeatureActive) {
                highlightButton(autoscrollButton)
            } else {
                unhighlightButton(autoscrollButton)
            }
        }
    }

    fun onFontsizeChangedEvent(fontsize: Float) {
        lyricsThemeService.fontsize = fontsize
        // parse without reading a whole file again
        lyricsLoader.reparse()
        onLyricsModelUpdated()
    }

    private fun toggleTransposePanel() {
        quickMenuAutoscroll.isVisible = false
        quickMenuTranspose.isVisible = !quickMenuTranspose.isVisible
        songPreview?.repaint()
    }

    private fun toggleAutoscrollPanel() {
        quickMenuTranspose.isVisible = false
        quickMenuAutoscroll.isVisible = !quickMenuAutoscroll.isVisible
        songPreview?.repaint()
    }

    private fun goToBeginning() {
        resetOverlayScroll()
        if (songPreview?.scroll ?: 0f == 0f && !autoscrollService.isRunning) {
            uiInfoService.showInfo(R.string.scroll_at_the_beginning_already)
        }
        songPreview?.goToBeginning()
        if (autoscrollService.isRunning) {
            // restart autoscrolling
            autoscrollService.start()
        }
    }

    private fun setFullscreen(fullscreen: Boolean) {
        this.fullscreen = fullscreen
        windowManagerService.setFullscreen(fullscreen)

        if (fullscreen) {
            appBarLayout?.visibility = View.GONE
            disableFullscreenButton?.show()
        } else {
            appBarLayout?.visibility = View.VISIBLE
            disableFullscreenButton?.hide()
        }
    }

    override fun onBackClicked() {
        when {
            quickMenuTranspose.isVisible -> {
                quickMenuTranspose.isVisible = false
                songPreview?.repaint()
            }
            quickMenuAutoscroll.isVisible -> quickMenuAutoscroll.isVisible = false
            fullscreen -> setFullscreen(false)
            else -> layoutController.showPreviousLayoutOrQuit()
        }
    }

    override fun onLayoutExit() {
        autoscrollService.stop()
        windowManagerService.keepScreenOn(false)
        if (fullscreen)
            setFullscreen(false)
    }

    fun onPreviewSizeChange(w: Int) {
        lyricsLoader.onPreviewSizeChange(w, songPreview?.paint)
        onLyricsModelUpdated()
    }

    private fun highlightButton(button: ImageButton) {
        val color = ContextCompat.getColor(activity, R.color.activePanelButton)
        button.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun unhighlightButton(button: ImageButton) {
        button.clearColorFilter()
    }

    private fun toggleSongFavourite() {
        currentSong?.let { currentSong ->
            if (!favouriteSongsService.isSongFavourite(currentSong)) {
                favouriteSongsService.setSongFavourite(currentSong)
            } else {
                favouriteSongsService.unsetSongFavourite(currentSong)
            }
        }
    }

    private fun updateFavouriteButton() {
        currentSong?.let { currentSong ->
            if (favouriteSongsService.isSongFavourite(currentSong)) {
                setFavouriteButton?.setImageResource(R.drawable.star_filled)
            } else {
                setFavouriteButton?.setImageResource(R.drawable.star_border)
            }
        }
    }

    fun showChordsGraphs() {
        val crdModel = lyricsLoader.lyricsModel ?: return
        chordsDiagramsService.showLyricsChordsMenu(crdModel)
    }

}
