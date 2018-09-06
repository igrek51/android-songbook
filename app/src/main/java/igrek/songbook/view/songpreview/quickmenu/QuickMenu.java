package igrek.songbook.view.songpreview.quickmenu;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.LyricsManager;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.view.songpreview.CanvasGraphics;

public class QuickMenu {
	
	@Inject
	Lazy<LyricsManager> chordsManager;
	@Inject
	UiResourceService infoService;
	@Inject
	AutoscrollService autoscrollService;
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	
	private boolean visible = false;
	
	private View quickMenuView;
	
	private TextView tvTransposition;
	private Button btnTransposeM5;
	private Button btnTransposeM1;
	private Button btnTranspose0;
	private Button btnTransposeP1;
	private Button btnTransposeP5;
	private Button btnAutoscrollToggle;
	
	public QuickMenu() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	private CanvasGraphics getCanvas() {
		return songPreviewController.get().getCanvas();
	}
	
	public void setQuickMenuView(View quickMenuView) {
		this.quickMenuView = quickMenuView;
		
		tvTransposition = quickMenuView.findViewById(R.id.tvTransposition);
		
		btnTransposeM5 = quickMenuView.findViewById(R.id.btnTransposeM5);
		btnTransposeM5.setOnClickListener(v -> chordsManager.get().onTransposeEvent(-5));
		
		btnTransposeM1 = quickMenuView.findViewById(R.id.btnTransposeM1);
		btnTransposeM1.setOnClickListener(v -> chordsManager.get().onTransposeEvent(-1));
		
		btnTranspose0 = quickMenuView.findViewById(R.id.btnTranspose0);
		btnTranspose0.setOnClickListener(v -> chordsManager.get().onTransposeResetEvent());
		
		btnTransposeP1 = quickMenuView.findViewById(R.id.btnTransposeP1);
		btnTransposeP1.setOnClickListener(v -> chordsManager.get().onTransposeEvent(+1));
		
		btnTransposeP5 = quickMenuView.findViewById(R.id.btnTransposeP5);
		btnTransposeP5.setOnClickListener(v -> chordsManager.get().onTransposeEvent(+5));
		
		btnAutoscrollToggle = quickMenuView.findViewById(R.id.btnAutoscrollToggle);
		btnAutoscrollToggle.setOnClickListener(v -> {
			if (autoscrollService.isRunning()) {
				autoscrollService.onAutoscrollStopUIEvent();
			} else {
				autoscrollService.onAutoscrollStartUIEvent();
			}
			
			setVisible(false);
		});
		
		updateTranspositionText();
	}
	
	private void updateTranspositionText() {
		String tvTranspositionText = infoService.resString(R.string.transposition) + ": " + chordsManager
				.get()
				.getTransposedString();
		tvTransposition.setText(tvTranspositionText);
	}
	
	public void draw() {
		if (visible) {
			//dimmed background
			float w = getCanvas().getW();
			float h = getCanvas().getH();
			
			getCanvas().setColor(0x000000, 130);
			getCanvas().fillRect(0, 0, w, h);
			
		}
	}
	
	public boolean onScreenClicked(float x, float y) {
		setVisible(false);
		return true;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		
		if (visible) {
			quickMenuView.setVisibility(View.VISIBLE);
			updateTranspositionText();
		} else {
			quickMenuView.setVisibility(View.GONE);
		}
		
		getCanvas().repaint();
	}
	
	public void onShowQuickMenuEvent(boolean visible) {
		setVisible(visible);
	}
	
	public void onTransposedEvent() {
		if (visible) {
			updateTranspositionText();
		}
	}
	
}
