package igrek.songbook.about;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;

public class HelpLayoutController {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	
	public HelpLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showUIHelp() {
		String message = uiResourceService.resString(R.string.ui_help_content);
		String title = uiResourceService.resString(R.string.nav_help);
		uiInfoService.showDialog(title, message);
	}
}