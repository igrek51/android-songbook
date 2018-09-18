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
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.LayoutState;
import igrek.songbook.layout.MainLayout;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.view.OverlayRecyclerAdapter;
import igrek.songbook.layout.songpreview.view.SongPreview;
import igrek.songbook.layout.songpreview.view.quickmenu.QuickMenu;
import igrek.songbook.layout.view.ButtonClickEffect;
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
	QuickMenu quickMenu;
	@Inject
	AutoscrollService autoscrollService;
	@Inject
	SoftKeyboardService softKeyboardService;
	@Inject
	SongDetailsService songDetailsService;
	
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
		ButtonClickEffect.addClickEffect(navMenuButton);
		
		// create songPreview
		songPreview = new SongPreview(activity);
		songPreview.reset();
		ViewGroup songPreviewContainer = layout.findViewById(R.id.songPreviewContainer);
		songPreviewContainer.addView(songPreview);
		
		// create quick menu
		FrameLayout quickMenuContainer = layout.findViewById(R.id.quickMenuContainer);
		LayoutInflater inflater = activity.getLayoutInflater();
		View quickMenuView = inflater.inflate(R.layout.quick_menu, null);
		quickMenuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		quickMenuContainer.addView(quickMenuView);
		quickMenu.setQuickMenuView(quickMenuView);
		quickMenu.setVisible(false);
		
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
		ButtonClickEffect.addClickEffect(goBackButton);
		
		ImageButton transposeButton = layout.findViewById(R.id.transposeButton);
		ButtonClickEffect.addClickEffect(transposeButton);
		
		ImageButton autoscrollButton = layout.findViewById(R.id.autoscrollButton);
		ButtonClickEffect.addClickEffect(autoscrollButton);
		
		ImageButton goBeginningButton = layout.findViewById(R.id.goBeginningButton);
		goBeginningButton.setOnClickListener((v) -> goToBeginning());
		ButtonClickEffect.addClickEffect(goBeginningButton);
		
		ImageButton songInfoButton = layout.findViewById(R.id.songInfoButton);
		songInfoButton.setOnClickListener((v) -> songDetailsService.showSongDetails(currentSong));
		ButtonClickEffect.addClickEffect(songInfoButton);
		
		ImageButton fullscreenButton = layout.findViewById(R.id.fullscreenButton);
		ButtonClickEffect.addClickEffect(fullscreenButton);
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
		overlayRecyclerView.getLayoutManager().scrollToPosition(1); // refresh
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
	
	private void goToBeginning() {
		resetOverlayScroll();
		songPreview.goToBeginning();
		if (autoscrollService.isRunning()) {
			// restart autoscrolling
			autoscrollService.start();
		}
	}
	
	@Override
	public void onBackClicked() {
		if (quickMenu.isVisible()) {
			quickMenu.onShowQuickMenuEvent(false);
		} else {
			autoscrollService.stop();
			layoutController.get().showPreviousLayout();
			windowManagerService.keepScreenOn(false);
		}
	}
}
