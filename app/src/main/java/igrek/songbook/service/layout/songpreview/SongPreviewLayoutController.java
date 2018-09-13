package igrek.songbook.service.layout.songpreview;

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
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.LyricsManager;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.LayoutState;
import igrek.songbook.service.layout.MainLayout;
import igrek.songbook.service.navmenu.NavigationMenuController;
import igrek.songbook.service.system.SoftKeyboardService;
import igrek.songbook.service.system.WindowManagerService;
import igrek.songbook.view.songpreview.OverlayRecyclerAdapter;
import igrek.songbook.view.songpreview.SongPreview;
import igrek.songbook.view.songpreview.quickmenu.QuickMenu;

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
		overlayRecyclerView.getLayoutManager().scrollToPosition(1);
		
		songTitleLabel = layout.findViewById(R.id.songTitleLabel);
		String title = currentSong.displayName();
		songTitleLabel.setText(title);
		
		ImageButton goBackButton = layout.findViewById(R.id.goBackButton);
		ImageButton transposeButton = layout.findViewById(R.id.transposeButton);
		ImageButton autoscrollButton = layout.findViewById(R.id.autoscrollButton);
		ImageButton goBeginningButton = layout.findViewById(R.id.goBeginningButton);
		ImageButton songInfoButton = layout.findViewById(R.id.songInfoButton);
		songInfoButton.setOnClickListener((v) -> songDetailsService.showSongDetails(currentSong));
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
		overlayRecyclerView.getLayoutManager().scrollToPosition(1); // refresh
	}
	
	public void onCrdModelUpdated() {
		songPreview.setCRDModel(lyricsManager.getCRDModel());
		overlayRecyclerView.getLayoutManager().scrollToPosition(1); // refresh
	}
	
	public void onFontsizeChangedEvent(float fontsize) {
		lyricsManager.setFontsize(fontsize);
		// parse without reading a whole file again
		lyricsManager.reparse();
		songPreview.setCRDModel(lyricsManager.getCRDModel());
		overlayRecyclerView.getLayoutManager().scrollToPosition(1); // refresh
	}
	
	public SongPreview getSongPreview() {
		return songPreview;
	}
	
	public void setCurrentSong(Song currentSong) {
		this.currentSong = currentSong;
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
