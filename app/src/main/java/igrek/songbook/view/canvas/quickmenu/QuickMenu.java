package igrek.songbook.view.canvas.quickmenu;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.info.UIResourceService;
import igrek.songbook.view.canvas.CanvasGraphics;

public class QuickMenu {
	
	@Inject
	Lazy<ChordsManager> chordsManager;
	@Inject
	UIResourceService infoService;
	@Inject
	AutoscrollService autoscrollService;
	private CanvasGraphics canvas;
	private boolean visible = false;
	
	private View quickMenuView;
	
	private TextView tvTransposition;
	private Button btnTransposeM5;
	private Button btnTransposeM1;
	private Button btnTranspose0;
	private Button btnTransposeP1;
	private Button btnTransposeP5;
	private Button btnAutoscrollToggle;
	
	public QuickMenu(CanvasGraphics canvas) {
		this.canvas = canvas;
		DaggerIoc.getFactoryComponent().inject(this);
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
		String tvTranspositionText = infoService.resString(R.string.transposition) + ": " + chordsManager.get()
				.getTransposedString();
		tvTransposition.setText(tvTranspositionText);
	}
	
	public void draw() {
		if (visible) {
			//dimmed background
			float w = canvas.getW();
			float h = canvas.getH();
			
			canvas.setColor(0x000000, 130);
			canvas.fillRect(0, 0, w, h);
			
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
		
		canvas.repaint();
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
