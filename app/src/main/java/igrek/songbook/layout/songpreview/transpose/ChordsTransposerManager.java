package igrek.songbook.layout.songpreview.transpose;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.view.quickmenu.QuickMenu;

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
	Lazy<QuickMenu> quickMenu;
	
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
		
		String info = uiResourceService.resString(R.string.transposition) + ": " + getTransposedByDisplayName();
		
		if (getTransposedBy() != 0) {
			userInfo.showInfoWithAction(info, R.string.transposition_reset, this::onTransposeResetEvent);
		} else {
			userInfo.showInfo(info);
		}
		
		quickMenu.get().onTransposedEvent();
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
		return (transposed > 0 ? "+" : "") + transposed;
	}
	
}
