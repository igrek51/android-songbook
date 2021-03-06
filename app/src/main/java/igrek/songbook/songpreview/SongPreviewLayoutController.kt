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
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.chords.diagram.ChordsDiagramsService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.lyrics.LyricsManager
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
import javax.inject.Inject

class SongPreviewLayoutController : MainLayout {

    @Inject
    lateinit var lyricsManager: Lazy<LyricsManager>
    @Inject
    lateinit var lyricsThemeService: Lazy<LyricsThemeService>
    @Inject
    lateinit var layoutController: Lazy<LayoutController>
    @Inject
    lateinit var windowManagerService: Lazy<WindowManagerService>
    @Inject
    lateinit var navigationMenuController: Lazy<NavigationMenuController>
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var quickMenuTranspose: Lazy<QuickMenuTranspose>
    @Inject
    lateinit var quickMenuAutoscroll: Lazy<QuickMenuAutoscroll>
    @Inject
    lateinit var autoscrollService: Lazy<AutoscrollService>
    @Inject
    lateinit var softKeyboardService: Lazy<SoftKeyboardService>
    @Inject
    lateinit var songDetailsService: Lazy<SongDetailsService>
    @Inject
    lateinit var uiInfoService: Lazy<UiInfoService>
    @Inject
    lateinit var favouriteSongsService: Lazy<FavouriteSongsService>
    @Inject
    lateinit var songContextMenuBuilder: Lazy<SongContextMenuBuilder>
    @Inject
    lateinit var songsRepository: Lazy<SongsRepository>
    @Inject
    lateinit var chordsDiagramsService: Lazy<ChordsDiagramsService>

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
        get() = quickMenuTranspose.get().isVisible || quickMenuAutoscroll.get().isVisible

    init {
        DaggerIoc.factoryComponent.inject(this)

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
        windowManagerService.get().keepScreenOn(true)
        softKeyboardService.get().hideSoftKeyboard()

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
        navMenuButton.setOnClickListener { navigationMenuController.get().navDrawerShow() }

        appBarLayout = layout.findViewById(R.id.appBarLayout)

        // create songPreview
        songPreview = SongPreview(activity)
        songPreview!!.reset()
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
        quickMenuTranspose.get().setQuickMenuView(quickMenuTransposeView)
        quickMenuTranspose.get().isVisible = false
        // autoscroll panel
        val quickMenuAutoscrollView = inflater.inflate(R.layout.component_quick_menu_autoscroll, null)
        quickMenuAutoscrollView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        quickMenuContainer.addView(quickMenuAutoscrollView)
        quickMenuAutoscroll.get().setQuickMenuView(quickMenuAutoscrollView)
        quickMenuAutoscroll.get().isVisible = false

        // overlaying RecyclerView
        overlayRecyclerView = activity.findViewById(R.id.overlayRecyclerView)
        overlayRecyclerView!!.setHasFixedSize(true) // improve performance
        overlayRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        overlayAdapter = OverlayRecyclerAdapter(songPreview!!)
        overlayRecyclerView!!.adapter = overlayAdapter
        overlayRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                songPreview!!.scrollByPx(dy.toFloat())
                songPreview!!.onManuallyScrolled(dy)
            }
        })
        overlayRecyclerView!!.isVerticalScrollBarEnabled = false
        overlayRecyclerView!!.overScrollMode = OVER_SCROLL_ALWAYS
        overlayRecyclerView!!.setOnClickListener { songPreview!!.onClick() }
        overlayRecyclerView!!.setOnTouchListener(songPreview)
        resetOverlayScroll()

        songTitleLabel = layout.findViewById(R.id.songTitleLabel)
        val title = currentSong!!.displayName()
        songTitleLabel!!.text = title

        val goBackButton = layout.findViewById<ImageButton>(R.id.goBackButton)
        goBackButton.setOnClickListener { onBackClicked() }

        transposeButton = layout.findViewById(R.id.transposeButton)
        transposeButton!!.setOnClickListener { toggleTransposePanel() }

        autoscrollButton = layout.findViewById(R.id.autoscrollButton)
        autoscrollButton!!.setOnClickListener { toggleAutoscrollPanel() }

        setFavouriteButton = layout.findViewById(R.id.setFavouriteButton)
        setFavouriteButton!!.setOnClickListener { toggleSongFavourite() }
        updateFavouriteButton()

        val chordsHelpButton = layout.findViewById<ImageButton>(R.id.chordsHelpButton)
        chordsHelpButton.setOnClickListener { showChordsGraphs() }

        val songInfoButton = layout.findViewById<ImageButton>(R.id.songInfoButton)
        songInfoButton.setOnClickListener { songDetailsService.get().showSongDetails(currentSong!!) }

        val fullscreenButton = layout.findViewById<ImageButton>(R.id.fullscreenButton)
        fullscreenButton.setOnClickListener { setFullscreen(true) }

        val moreActionsButton = layout.findViewById<ImageButton>(R.id.moreActionsButton)
        moreActionsButton.setOnClickListener { showMoreActions() }

        disableFullscreenButton = layout.findViewById(R.id.disableFullscreenButton)
        disableFullscreenButton!!.setOnClickListener { setFullscreen(false) }
        setFullscreen(false)
    }

    private fun showMoreActions() {
        songContextMenuBuilder.get().showSongActions(currentSong!!)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.screen_song_preview
    }

    fun onGraphicsInitializedEvent(w: Int, paint: Paint?) {
        currentSong?.let {
            // load file and parse it
            val fileContent = it.content.orEmpty()
            val srcNotation = it.chordsNotation ?: ChordsNotation.default
            val transposed = songsRepository.get().transposeDao.getSongTransposition(it.songIdentifier())
            // initialize - first file loading
            lyricsManager.get().load(fileContent, w, paint, transposed, srcNotation)

            songPreview?.setFontSizes(lyricsThemeService.get().fontsize)
            songPreview?.setCRDModel(lyricsManager.get().crdModel)
            resetOverlayScroll()
        }
    }

    private fun resetOverlayScroll() {
        // refresh
        overlayRecyclerView?.layoutManager?.scrollToPosition(1)
        overlayRecyclerView?.scrollToPosition(1)
    }

    fun onLyricsModelUpdated() {
        songPreview!!.setCRDModel(lyricsManager.get().crdModel)
        resetOverlayScroll()
        highlightPanelButtons()
    }

    private fun highlightPanelButtons() {
        if (quickMenuTranspose.get().isFeatureActive) {
            highlightButton(transposeButton!!)
        } else {
            unhighlightButton(transposeButton!!)
        }
        if (quickMenuAutoscroll.get().isFeatureActive) {
            highlightButton(autoscrollButton!!)
        } else {
            unhighlightButton(autoscrollButton!!)
        }
    }

    fun onFontsizeChangedEvent(fontsize: Float) {
        lyricsThemeService.get().fontsize = fontsize
        // parse without reading a whole file again
        lyricsManager.get().reparse()
        onLyricsModelUpdated()
    }

    private fun toggleTransposePanel() {
        quickMenuAutoscroll.get().isVisible = false
        quickMenuTranspose.get().isVisible = !quickMenuTranspose.get().isVisible
        songPreview!!.repaint()
    }

    private fun toggleAutoscrollPanel() {
        quickMenuTranspose.get().isVisible = false
        quickMenuAutoscroll.get().isVisible = !quickMenuAutoscroll.get().isVisible
        songPreview!!.repaint()
    }

    private fun goToBeginning() {
        resetOverlayScroll()
        if (songPreview!!.scroll == 0f && !autoscrollService.get().isRunning) {
            uiInfoService.get().showInfo(R.string.scroll_at_the_beginning_already)
        }
        songPreview!!.goToBeginning()
        if (autoscrollService.get().isRunning) {
            // restart autoscrolling
            autoscrollService.get().start()
        }
    }

    private fun setFullscreen(fullscreen: Boolean) {
        this.fullscreen = fullscreen
        windowManagerService.get().setFullscreen(fullscreen)

        if (fullscreen) {
            appBarLayout!!.visibility = View.GONE
            disableFullscreenButton!!.show()
        } else {
            appBarLayout!!.visibility = View.VISIBLE
            disableFullscreenButton!!.hide()
        }
    }

    override fun onBackClicked() {
        when {
            quickMenuTranspose.get().isVisible -> {
                quickMenuTranspose.get().isVisible = false
                songPreview!!.repaint()
            }
            quickMenuAutoscroll.get().isVisible -> quickMenuAutoscroll.get().isVisible = false
            fullscreen -> setFullscreen(false)
            else -> layoutController.get().showPreviousLayoutOrQuit()
        }
    }

    override fun onLayoutExit() {
        autoscrollService.get().stop()
        windowManagerService.get().keepScreenOn(false)
        if (fullscreen)
            setFullscreen(false)
    }

    fun onPreviewSizeChange(w: Int) {
        lyricsManager.get().onPreviewSizeChange(w, songPreview!!.paint)
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
        if (!favouriteSongsService.get().isSongFavourite(currentSong!!)) {
            favouriteSongsService.get().setSongFavourite(currentSong!!)
        } else {
            favouriteSongsService.get().unsetSongFavourite(currentSong!!)
        }
    }

    private fun updateFavouriteButton() {
        if (favouriteSongsService.get().isSongFavourite(currentSong!!)) {
            setFavouriteButton!!.setImageResource(R.drawable.star_filled)
        } else {
            setFavouriteButton!!.setImageResource(R.drawable.star_border)
        }
    }

    fun showChordsGraphs() {
        val crdModel = lyricsManager.get().crdModel ?: return
        chordsDiagramsService.get().showLyricsChordsMenu(crdModel)
    }

}
