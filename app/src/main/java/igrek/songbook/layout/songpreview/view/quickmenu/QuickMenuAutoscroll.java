package igrek.songbook.layout.songpreview.view.quickmenu;

import android.view.View;
import android.widget.Button;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.view.SongPreview;

public class QuickMenuAutoscroll {
	
	@Inject
	Lazy<LyricsManager> lyricsManager;
	@Inject
	UiResourceService infoService;
	@Inject
	AutoscrollService autoscrollService;
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	
	private boolean visible = false;
	private View quickMenuView;
	
	public QuickMenuAutoscroll() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	private SongPreview getCanvas() {
		return songPreviewController.get().getSongPreview();
	}
	
	public void setQuickMenuView(View quickMenuView) {
		this.quickMenuView = quickMenuView;
		
		Button btnAutoscrollToggle = quickMenuView.findViewById(R.id.autoscrollToggleButton);
		btnAutoscrollToggle.setOnClickListener(v -> {
			if (autoscrollService.isRunning()) {
				autoscrollService.onAutoscrollStopUIEvent();
			} else {
				autoscrollService.onAutoscrollStartUIEvent();
			}
			
			setVisible(false);
		});
		
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		
		if (visible) {
			quickMenuView.setVisibility(View.VISIBLE);
		} else {
			quickMenuView.setVisibility(View.GONE);
		}
		
		getCanvas().repaint();
	}
	
}
