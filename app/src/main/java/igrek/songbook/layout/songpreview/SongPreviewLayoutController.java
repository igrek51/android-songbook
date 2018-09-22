package igrek.songbook.layout.songpreview;

import android.graphics.Paint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.LayoutState;
import igrek.songbook.layout.MainLayout;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuAutoscroll;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuTranspose;
import igrek.songbook.layout.songpreview.render.SongPreview;
import igrek.songbook.system.SoftKeyboardService;
import igrek.songbook.system.WindowManagerService;

import static android.view.View.OVER_SCROLL_ALWAYS;

public class SongPreviewLayoutController implements MainLayout {
	
	@Inject
	LyricsManager lyricsManager;
	@Inject
	Lazy<LayoutController> layoutController;
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	NavigationMenuController navigationMenuController;
	@Inject
	AppCompatActivity activity;
	@Inject
	QuickMenuTranspose quickMenuTranspose;
	@Inject
	QuickMenuAutoscroll quickMenuAutoscroll;
	@Inject
	AutoscrollService autoscrollService;
	@Inject
	SoftKeyboardService softKeyboardService;
	@Inject
	SongDetailsService songDetailsService;
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	
	private Logger logger = LoggerFactory.getLogger();
	private SongPreview songPreview;
	private Song currentSong;
	private OverlayRecyclerAdapter overlayAdapter;
	private RecyclerView overlayRecyclerView;
	private TextView songTitleLabel;
	
	public SongPreviewLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	@Override
	public void showLayout(View layout) {
		windowManagerService.keepScreenOn(true);
		softKeyboardService.hideSoftKeyboard();
		
		// Toolbar
		Toolbar toolbar1 = layout.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		ActionBar actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowHomeEnabled(false);
		}
		// navigation menu button
		ImageButton navMenuButton = layout.findViewById(R.id.navMenuButton);
		navMenuButton.setOnClickListener((v) -> navigationMenuController.navDrawerShow());
		
		// create songPreview
		songPreview = new SongPreview(activity);
		songPreview.reset();
		ViewGroup songPreviewContainer = layout.findViewById(R.id.songPreviewContainer);
		songPreviewContainer.addView(songPreview);
		
		// TODO lazy load quick menu panels - load on use only
		// create quick menu panels
		FrameLayout quickMenuContainer = layout.findViewById(R.id.quickMenuContainer);
		LayoutInflater inflater = activity.getLayoutInflater();
		// transpose panel
		View quickMenuTransposeView = inflater.inflate(R.layout.quick_menu_transpose, null);
		quickMenuTransposeView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		quickMenuContainer.addView(quickMenuTransposeView);
		quickMenuTranspose.setQuickMenuView(quickMenuTransposeView);
		quickMenuTranspose.setVisible(false);
		// autoscroll panel
		View quickMenuAutoscrollView = inflater.inflate(R.layout.quick_menu_autoscroll, null);
		quickMenuAutoscrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		quickMenuContainer.addView(quickMenuAutoscrollView);
		quickMenuAutoscroll.setQuickMenuView(quickMenuAutoscrollView);
		quickMenuAutoscroll.setVisible(false);
		
		// overlaying RecyclerView
		overlayRecyclerView = activity.findViewById(R.id.overlayRecyclerView);
		overlayRecyclerView.setHasFixedSize(true); // improve performance
		overlayRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
		overlayAdapter = new OverlayRecyclerAdapter(songPreview);
		overlayRecyclerView.setAdapter(overlayAdapter);
		overlayRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			}
			
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				songPreview.scrollByPx(dy);
				songPreview.onManuallyScrolled(dy);
			}
		});
		overlayRecyclerView.setVerticalScrollBarEnabled(false);
		overlayRecyclerView.setOverScrollMode(OVER_SCROLL_ALWAYS);
		overlayRecyclerView.setOnClickListener((v) -> songPreview.onClick());
		overlayRecyclerView.setOnTouchListener(songPreview);
		resetOverlayScroll();
		
		songTitleLabel = layout.findViewById(R.id.songTitleLabel);
		String title = currentSong.displayName();
		songTitleLabel.setText(title);
		
		ImageButton goBackButton = layout.findViewById(R.id.goBackButton);
		goBackButton.setOnClickListener((v) -> onBackClicked());
		
		ImageButton transposeButton = layout.findViewById(R.id.transposeButton);
		transposeButton.setOnClickListener((v) -> toggleTransposePanel());
		
		ImageButton autoscrollButton = layout.findViewById(R.id.autoscrollButton);
		autoscrollButton.setOnClickListener((v) -> toggleAutoscrollPanel());
		
		ImageButton goBeginningButton = layout.findViewById(R.id.goBeginningButton);
		goBeginningButton.setOnClickListener((v) -> goToBeginning());
		
		ImageButton songInfoButton = layout.findViewById(R.id.songInfoButton);
		songInfoButton.setOnClickListener((v) -> songDetailsService.showSongDetails(currentSong));
		
		ImageButton fullscreenButton = layout.findViewById(R.id.fullscreenButton);
		fullscreenButton.setOnClickListener((v) -> uiInfoService.showToast(uiResourceService.resString(R.string.feature_not_implemented)));
		
	}
	
	@Override
	public LayoutState getLayoutState() {
		return LayoutState.SONG_PREVIEW;
	}
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.song_preview;
	}
	
	public void onGraphicsInitializedEvent(int w, int h, Paint paint) {
		// load file and parse it
		String fileContent = currentSong.getFileContent();
		// initialize - first file loading
		lyricsManager.load(fileContent, w, h, paint);
		
		songPreview.setFontSizes(lyricsManager.getFontsize());
		songPreview.setCRDModel(lyricsManager.getCRDModel());
		resetOverlayScroll();
	}
	
	private void resetOverlayScroll() {
		// refresh
		overlayRecyclerView.getLayoutManager().scrollToPosition(1);
		overlayRecyclerView.scrollToPosition(1);
	}
	
	public void onCrdModelUpdated() {
		songPreview.setCRDModel(lyricsManager.getCRDModel());
		resetOverlayScroll();
	}
	
	public void onFontsizeChangedEvent(float fontsize) {
		lyricsManager.setFontsize(fontsize);
		// parse without reading a whole file again
		lyricsManager.reparse();
		songPreview.setCRDModel(lyricsManager.getCRDModel());
		resetOverlayScroll();
	}
	
	public SongPreview getSongPreview() {
		return songPreview;
	}
	
	public void setCurrentSong(Song currentSong) {
		this.currentSong = currentSong;
	}
	
	private void toggleTransposePanel() {
		quickMenuAutoscroll.setVisible(false);
		quickMenuTranspose.setVisible(!quickMenuTranspose.isVisible());
		songPreview.repaint();
	}
	
	private void toggleAutoscrollPanel() {
		quickMenuTranspose.setVisible(false);
		quickMenuAutoscroll.setVisible(!quickMenuAutoscroll.isVisible());
		songPreview.repaint();
	}
	
	private void goToBeginning() {
		resetOverlayScroll();
		if (songPreview.getScroll() == 0f && !autoscrollService.isRunning()) {
			uiInfoService.showInfo(R.string.scroll_at_the_beginning_already);
		}
		songPreview.goToBeginning();
		if (autoscrollService.isRunning()) {
			// restart autoscrolling
			autoscrollService.start();
		}
	}
	
	public boolean isQuickMenuVisible() {
		return quickMenuTranspose.isVisible() || quickMenuAutoscroll.isVisible();
	}
	
	@Override
	public void onBackClicked() {
		if (isQuickMenuVisible()) {
			if (quickMenuTranspose.isVisible()) {
				quickMenuTranspose.setVisible(false);
				songPreview.repaint();
			} else if (quickMenuAutoscroll.isVisible()) {
				quickMenuAutoscroll.setVisible(false);
			}
		} else {
			autoscrollService.stop();
			layoutController.get().showPreviousLayout();
			windowManagerService.keepScreenOn(false);
		}
	}
	
	public void onPreviewSizeChanged() {
		songPreview.reset();
	}
}
