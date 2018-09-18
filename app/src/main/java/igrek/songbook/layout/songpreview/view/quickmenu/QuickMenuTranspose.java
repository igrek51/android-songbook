package igrek.songbook.layout.songpreview.view.quickmenu;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.transpose.ChordsTransposerManager;
import igrek.songbook.layout.songpreview.view.SongPreview;

public class QuickMenuTranspose {
	
	@Inject
	Lazy<LyricsManager> lyricsManager;
	@Inject
	Lazy<ChordsTransposerManager> chordsTransposerManager;
	@Inject
	UiResourceService infoService;
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	
	private boolean visible = false;
	private View quickMenuView;
	private TextView transposedByLabel;
	
	public QuickMenuTranspose() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	private SongPreview getCanvas() {
		return songPreviewController.get().getSongPreview();
	}
	
	public void setQuickMenuView(View quickMenuView) {
		this.quickMenuView = quickMenuView;
		
		transposedByLabel = quickMenuView.findViewById(R.id.transposedByLabel);
		
		Button btnTransposeM5 = quickMenuView.findViewById(R.id.btnTransposeM5);
		btnTransposeM5.setOnClickListener(v -> chordsTransposerManager.get().onTransposeEvent(-5));
		
		Button btnTransposeM1 = quickMenuView.findViewById(R.id.btnTransposeM1);
		btnTransposeM1.setOnClickListener(v -> chordsTransposerManager.get().onTransposeEvent(-1));
		
		Button btnTranspose0 = quickMenuView.findViewById(R.id.btnTranspose0);
		btnTranspose0.setOnClickListener(v -> chordsTransposerManager.get()
				.onTransposeResetEvent());
		
		Button btnTransposeP1 = quickMenuView.findViewById(R.id.btnTransposeP1);
		btnTransposeP1.setOnClickListener(v -> chordsTransposerManager.get().onTransposeEvent(+1));
		
		Button btnTransposeP5 = quickMenuView.findViewById(R.id.btnTransposeP5);
		btnTransposeP5.setOnClickListener(v -> chordsTransposerManager.get().onTransposeEvent(+5));
		
		updateTranspositionText();
	}
	
	private void updateTranspositionText() {
		String semitonesDisplayName = chordsTransposerManager
				.get()
				.getTransposedByDisplayName();
		String transposedByText = infoService.resString(R.string.transposed_by, semitonesDisplayName);
		transposedByLabel.setText(transposedByText);
	}
	
	public void draw() {
		if (visible) {
			//dimmed background
			float w = getCanvas().getW();
			float h = getCanvas().getH();
			getCanvas().setColor(0x000000, 110);
			getCanvas().fillRect(0, 0, w, h);
		}
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
