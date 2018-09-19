package igrek.songbook.layout.songpreview.transpose;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.view.quickmenu.QuickMenuTranspose;

public class ChordsTransposerManager {
	
	private int transposed = 0;
	private ChordsTransposer chordsTransposer = new ChordsTransposer();
	
	@Inject
	Lazy<LyricsManager> lyricsManager;
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	UiInfoService userInfo;
	@Inject
	Lazy<QuickMenuTranspose> quickMenuTranspose;
	
	public ChordsTransposerManager() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void reset() {
		transposed = 0;
	}
	
	public String transposeContent(String fileContent) {
		return chordsTransposer.transposeContent(fileContent, transposed);
	}
	
	public void onTransposeEvent(int semitones) {
		transposeBy(semitones);
		
		songPreviewController.get().onCrdModelUpdated();
		
		String info = uiResourceService.resString(R.string.transposed_by_semitones, getTransposedByDisplayName());
		
		if (getTransposedBy() != 0) {
			userInfo.showInfoWithAction(info, R.string.action_transposition_reset, this::onTransposeResetEvent);
		} else {
			userInfo.showInfo(info);
		}
		
		quickMenuTranspose.get().onTransposedEvent();
	}
	
	public void onTransposeResetEvent() {
		onTransposeEvent(-getTransposedBy());
	}
	
	private void transposeBy(int semitones) {
		transposed += semitones;
		if (transposed >= 12)
			transposed -= 12;
		if (transposed <= -12)
			transposed += 12;
		lyricsManager.get().reparse();
	}
	
	private int getTransposedBy() {
		return transposed;
	}
	
	public String getTransposedByDisplayName() {
		return (transposed > 0 ? "+" : "") + transposed + " " + getSemitonesDisplayName(transposed);
	}
	
	private String getSemitonesDisplayName(int transposed) {
		int absTransposed = transposed >= 0 ? transposed : -transposed;
		int stringResId;
		if (absTransposed == 0) {
			stringResId = R.string.transpose_0_semitones;
		} else if (absTransposed == 1) {
			stringResId = R.string.transpose_1_semitones;
		} else if (absTransposed <= 4) { // 2,3,4
			stringResId = R.string.transpose_234_semitones;
		} else {
			stringResId = R.string.transpose_5_semitones;
		}
		return uiResourceService.resString(stringResId);
	}
	
}
