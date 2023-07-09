package igrek.songbook.songpreview

import android.annotation.SuppressLint
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
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
import igrek.songbook.activity.ActivityController
import igrek.songbook.cast.SongCastService
import igrek.songbook.chords.diagram.ChordDiagramsService
import igrek.songbook.chords.loader.LyricsLoader
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuCast
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songpreview.renderer.OverlayRecyclerAdapter
import igrek.songbook.songpreview.renderer.SongPreview
import igrek.songbook.songpreview.scroll.AutoscrollService
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.favourite.FavouriteSongsService
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.WindowManagerService
import igrek.songbook.util.mainScope
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@SuppressLint("CheckResult")
class SongPreviewLayoutController(
    lyricsLoader: LazyInject<LyricsLoader> = appFactory.lyricsLoader,
    lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    quickMenuTranspose: LazyInject<QuickMenuTranspose> = appFactory.quickMenuTranspose,
    quickMenuCast: LazyInject<QuickMenuCast> = appFactory.quickMenuCast,
    quickMenuAutoscroll: LazyInject<QuickMenuAutoscroll> = appFactory.quickMenuAutoscroll,
    autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    songDetailsService: LazyInject<SongDetailsService> = appFactory.songDetailsService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    favouriteSongsService: LazyInject<FavouriteSongsService> = appFactory.favouriteSongsService,
    songContextMenuBuilder: LazyInject<SongContextMenuBuilder> = appFactory.songContextMenuBuilder,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    chordDiagramsService: LazyInject<ChordDiagramsService> = appFactory.chordDiagramsService,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
) : MainLayout {
    private val lyricsLoader by LazyExtractor(lyricsLoader)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val layoutController by LazyExtractor(layoutController)
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val activity by LazyExtractor(appCompatActivity)
    private val quickMenuTranspose by LazyExtractor(quickMenuTranspose)
    private val quickMenuCast by LazyExtractor(quickMenuCast)
    private val quickMenuAutoscroll by LazyExtractor(quickMenuAutoscroll)
    private val autoscrollService by LazyExtractor(autoscrollService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songDetailsService by LazyExtractor(songDetailsService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val favouriteSongsService by LazyExtractor(favouriteSongsService)
    private val songContextMenuBuilder by LazyExtractor(songContextMenuBuilder)
    private val songsRepository by LazyExtractor(songsRepository)
    private val chordsDiagramsService by LazyExtractor(chordDiagramsService)
    private val preferencesState by LazyExtractor(settingsState)
    private val songCastService by LazyExtractor(songCastService)
    private val activityController by LazyExtractor(activityController)

    var songPreview: SongPreview? = null
        private set
    var currentSong: Song? = null
    private var overlayAdapter: OverlayRecyclerAdapter? = null
    private var overlayScrollView: RecyclerView? = null
    private var songTitleLabel: TextView? = null
    private var fullscreen = false
    private var appBarLayout: AppBarLayout? = null
    private var exitFullscreenButton: FloatingActionButton? = null
    private var transposeButton: ImageButton? = null
    private var autoscrollButton: ImageButton? = null
    private var setFavouriteButton: ImageButton? = null
    private var songCastButton: ImageButton? = null
    private val originalButtonBackgrounds: MutableMap<Int, Pair<ColorFilter, Drawable>> = mutableMapOf()
    private val onInitListeners: MutableList<() -> Unit> = mutableListOf()
    private var graphicsInitialized: Boolean = false

    init {
        autoscrollService.get().scrollStateSubject
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                highlightPanelButtons()
            }, UiErrorHandler::handleError)
        favouriteSongsService.get().updateFavouriteSongSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                updateFavouriteButton()
            }, UiErrorHandler::handleError)
    }

    @SuppressLint("InflateParams")
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

        // songPreview canvas
        graphicsInitialized = false
        songPreview = SongPreview(
            activity,
            onInit = ::onGraphicsInitialized,
            onFontsizeChanged = ::onFontsizeChanged,
            onPreviewSizeChanged = ::onPreviewSizeChanged,
        ).apply {
            reset()
        }
        val songPreviewContainer = layout.findViewById<ViewGroup>(R.id.songPreviewContainer)
        songPreviewContainer.addView(songPreview?.canvas)

        // create quick menu panels
        val quickMenuContainer = layout.findViewById<FrameLayout>(R.id.quickMenuContainer)
        val inflater = activity.layoutInflater

        // transpose panel
        val quickMenuTransposeView = inflater.inflate(R.layout.component_quick_menu_transpose, null)
        quickMenuTransposeView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        quickMenuContainer.addView(quickMenuTransposeView)
        quickMenuTranspose.setQuickMenuView(quickMenuTransposeView)
        quickMenuTranspose.isVisible = false

        // autoscroll panel
        val quickMenuAutoscrollView =
            inflater.inflate(R.layout.component_quick_menu_autoscroll, null)
        quickMenuAutoscrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        quickMenuContainer.addView(quickMenuAutoscrollView)
        quickMenuAutoscroll.setQuickMenuView(quickMenuAutoscrollView)
        quickMenuAutoscroll.isVisible = false

        // cast panel
        val quickMenuCastView = inflater.inflate(R.layout.component_quick_menu_cast, null)
        quickMenuCastView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        quickMenuContainer.addView(quickMenuCastView)
        quickMenuCast.setQuickMenuView(quickMenuCastView)
        quickMenuCast.isVisible = false

        // overlaying RecyclerView
        overlayScrollView = activity.findViewById<RecyclerView>(R.id.overlayScrollView)?.apply {
            setHasFixedSize(true) // improve performance
            layoutManager = LinearLayoutManager(activity)
            songPreview?.let { songPreview ->
                overlayAdapter = OverlayRecyclerAdapter(songPreview)
                songPreview.overlayScrollResetter = { resetOverlayScroll() }
                songPreview.overlayScrollView = this
            }
            adapter = overlayAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    songPreview?.changedRecyclerScrollState(newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    songPreview?.run {
                        scrollByPxVertical(dy.toFloat())
                        onManuallyScrolled(dy.toFloat())
                    }
                }
            })
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_ALWAYS
            setOnClickListener { songPreview?.onClick() }
            setOnTouchListener(songPreview)
        }
        resetOverlayScroll()

        songTitleLabel = layout.findViewById<TextView>(R.id.songTitleLabel)?.apply {
            text = formatSongTitle()
        }

        transposeButton = layout.findViewById<ImageButton>(R.id.transposeButton)?.also {
            it.setOnClickListener { toggleTransposePanel() }
            originalButtonBackgrounds[it.id] = it.colorFilter to it.background
        }

        autoscrollButton = layout.findViewById<ImageButton>(R.id.autoscrollButton)?.also {
            it.setOnClickListener { toggleAutoscrollPanel() }
            originalButtonBackgrounds[it.id] = it.colorFilter to it.background
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

        layout.findViewById<ImageButton>(R.id.moreActionsButton)?.run {
            setOnClickListener { showMoreActions() }
        }

        songCastButton = layout.findViewById<ImageButton>(R.id.songCastButton)?.also {
            it.setOnClickListener {
                toggleCastPanel()
            }
            originalButtonBackgrounds[it.id] = it.colorFilter to it.background
        }
        updateSongCastButton()

        exitFullscreenButton = layout.findViewById<FloatingActionButton>(R.id.exitFullscreenButton)
            ?.apply {
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

    private fun onGraphicsInitialized() {
        val w = songPreview?.canvas?.w ?: return
        val currentSong = currentSong ?: return
        mainScope.launch {
            // load file and parse it
            val fileContent = currentSong.content.orEmpty()
            val srcNotation = currentSong.chordsNotation
            val transposed = songsRepository.transposeDao.getSongTransposition(currentSong.songIdentifier())
            // initialize - first file loading
            lyricsLoader.load(fileContent, w, transposed, srcNotation)

            songPreview?.setFontSizes(lyricsThemeService.fontsize)
            songPreview?.setLyricsModel(lyricsLoader.arrangedLyrics)
            resetOverlayScroll()

            autoscrollService.onLoad(currentSong.songIdentifier())

            graphicsInitialized = true
            onInitListeners.forEach { it() }
            onInitListeners.clear()
        }
    }

    fun addOnInitListener(onInitListener: () -> Unit) {
        if (graphicsInitialized) {
            mainScope.launch {
                onInitListener()
            }
            return
        }
        onInitListeners.add(onInitListener)
    }

    private fun resetOverlayScroll() {
        // recenter
        overlayScrollView?.layoutManager?.scrollToPosition(1)
        overlayScrollView?.scrollToPosition(1)
    }

    fun onLyricsModelUpdated() {
        songPreview?.setLyricsModel(lyricsLoader.arrangedLyrics)
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

    private fun onFontsizeChanged(fontsize: Float) {
        lyricsThemeService.fontsize = fontsize
        // parse without reading a whole file again
        lyricsLoader.onFontSizeChanged()
        onLyricsModelUpdated()
    }

    private fun toggleTransposePanel() {
        quickMenuTranspose.isVisible = !quickMenuTranspose.isVisible
        quickMenuAutoscroll.isVisible = false
        quickMenuCast.isVisible = false
        songPreview?.canvas?.repaint()
        if (quickMenuTranspose.isVisible) {
            activity.findViewById<View>(R.id.transpose0Button)?.requestFocus()
        }
    }

    private fun toggleAutoscrollPanel() {
        quickMenuTranspose.isVisible = false
        quickMenuAutoscroll.isVisible = !quickMenuAutoscroll.isVisible
        quickMenuCast.isVisible = false
        songPreview?.canvas?.repaint()
        if (quickMenuAutoscroll.isVisible) {
            activity.findViewById<View>(R.id.autoscrollToggleButton)?.requestFocus()
        }
    }

    private fun toggleCastPanel() {
        quickMenuTranspose.isVisible = false
        quickMenuAutoscroll.isVisible = false
        quickMenuCast.isVisible = !quickMenuCast.isVisible
        songPreview?.canvas?.repaint()
    }

    fun isTransposePanelVisible(): Boolean = quickMenuTranspose.isVisible

    fun isAutoscrollPanelVisible(): Boolean = quickMenuAutoscroll.isVisible

    private fun goToBeginning() {
        resetOverlayScroll()
        if ((songPreview?.scroll ?: 0f) == 0f && !autoscrollService.isRunning) {
            uiInfoService.showInfo(R.string.scroll_at_the_beginning_already)
        }
        songPreview?.goToBeginning()
        if (autoscrollService.isRunning) {
            autoscrollService.start() // restart autoscrolling
        }
    }

    fun toggleFullscreen() {
        setFullscreen(!this.fullscreen)
    }

    private fun setFullscreen(fullscreen: Boolean) {
        this.fullscreen = fullscreen
        windowManagerService.setFullscreen(fullscreen)

        if (fullscreen) {
            appBarLayout?.visibility = View.GONE
            if (!activityController.isAndroidTv()) {
                exitFullscreenButton?.show()
            }
        } else {
            appBarLayout?.visibility = View.VISIBLE
            exitFullscreenButton?.hide()
        }
    }

    override fun onBackClicked() {
        when {
            quickMenuTranspose.isVisible -> {
                quickMenuTranspose.isVisible = false
                songPreview?.canvas?.repaint()
            }
            quickMenuAutoscroll.isVisible -> quickMenuAutoscroll.isVisible = false
            quickMenuCast.isVisible -> quickMenuCast.isVisible = false
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

    private fun onPreviewSizeChanged() {
        songPreview?.canvas?.let { canvas ->
            lyricsLoader.onPreviewSizeChange(canvas.w)
            onLyricsModelUpdated()
        }
    }

    private fun highlightButton(button: ImageButton) {
        val color = ContextCompat.getColor(activity, R.color.activePanelButton)
        button.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        button.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark))
    }

    private fun unhighlightButton(button: ImageButton) {
        originalButtonBackgrounds[button.id]?.let { original ->
            button.colorFilter = original.first
            button.background = original.second
        } ?: run {
            button.clearColorFilter()
            button.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary))
        }
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

    private fun updateSongCastButton() {
        songCastButton?.let { songCastButton ->
            if (songCastService.isInRoom()) {
                songCastButton.visibility = View.VISIBLE
                highlightButton(songCastButton)
            } else {
                songCastButton.visibility = View.GONE
            }
        }
    }

    private fun showChordsGraphs() {
        chordsDiagramsService.showLyricsChordsMenu(lyricsLoader.transposedLyrics)
    }

    fun scrollByStep(stepsDown: Int): Boolean {
        if (navigationMenuController.isDrawerShown())
            return false
        val lines = stepsDown * 1f
        val dy: Float = lines * (songPreview?.lineheightPx ?: 0f)
        overlayScrollView?.smoothScrollBy(0, dy.toInt())
        return when {
            stepsDown < 0 && !canScrollUp() -> false
            stepsDown > 0 && !canScrollDown() -> false
            else -> true
        }
    }

    fun canScrollUp(): Boolean {
        return (songPreview?.scroll ?: 0f) > 1f
    }

    fun canScrollDown(): Boolean {
        return songPreview?.canScrollDown() ?: false
    }

    private fun formatSongTitle(): Spanned {
        val song = currentSong ?: return SpannableString("")
        val categories = song.displayCategories()
        return when {
            categories.isNotBlank() -> {
                val span: Spannable = SpannableString("${song.title} - $categories")
                span.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, song.title.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                span
            }
            else -> {
                SpannableString(song.title)
            }
        }
    }

}
