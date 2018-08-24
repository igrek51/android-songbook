package igrek.songbook.service.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.screen.ScreenService;

public class ActivityController {
	
	private Logger logger = LoggerFactory.getLogger();
	
	@Inject
	ScreenService screenService;
	@Inject
	Activity activity;
	
//	public static final boolean KEEP_SCREEN_ON = true;
//	private FileTreeManager fileTreeManager;
//	private ChordsManager chordsManager;
//	private UIResourceService userInfo;
//	private GUI gui;
//
//	private AppState state;
	
	public ActivityController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		// resize event
		int screenWidthDp = newConfig.screenWidthDp;
		int screenHeightDp = newConfig.screenHeightDp;
		int orientation = newConfig.orientation;
		int densityDpi = newConfig.densityDpi;
		logger.debug("Screen resized: " + screenWidthDp + "dp x " + screenHeightDp + "dp (DPI = " + densityDpi + ")");
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			logger.debug("Screen orientation: landscape");
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			logger.debug("Screen orientation: portrait");
		}
	}
	
	public void onDestroy() {
		logger.debug("Activity has been destroyed.");
	}
	
	public void quit() {
		//TODO
// 		PreferencesOld preferences;
//		preferences.saveAll();
		
		logger.debug("Closing app...");
		screenService.keepScreenOff();
		activity.finish();
	}
	
	public void minimize() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(startMain);
	}
	
	// TODO move elsewhere
	
//	public void goUp() {
//		try {
//			fileTreeManager.goUp();
//			updateFileList();
//			//scrollowanie do ostatnio otwartego folderu
//			restoreScrollPosition(fileTreeManager.getCurrentPath());
//		} catch (NoParentDirException e) {
//			quit();
//		}
//	}
//
//	private void updateFileList() {
//		gui.updateFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
//		state = AppState.FILE_LIST;
//	}
//
//	private void showFileContent(String filename) {
//		state = AppState.FILE_CONTENT;
//		fileTreeManager.setCurrentFileName(filename);
//		gui.showFileContent();
//		if (KEEP_SCREEN_ON) {
//			screenService.keepScreenOn();
//		}
//	}
//
//	private String getHomePath() {
//		PreferencesOld preferences = AppController.getService(PreferencesOld.class);
//		return preferences.startPath;
//	}
//
//	private boolean isInHomeDir() {
//		return fileTreeManager.getCurrentPath().equals(FileTreeManager.trimEndSlash(getHomePath()));
//	}
//
//	private void homeClicked() {
//		if (isInHomeDir()) {
//			quit();
//		} else {
//			fileTreeManager.goTo(getHomePath());
//			updateFileList();
//		}
//	}
//
//	private void setHomePath() {
//		PreferencesOld preferences = AppController.getService(PreferencesOld.class);
//		String homeDir = fileTreeManager.getCurrentPath();
//		preferences.startPath = homeDir;
//		preferences.saveAll();
//		userInfo.showActionInfo(R.string.starting_directory_saved, null, userInfo.resString(R.string.action_info_ok), null);
//	}
//
//
//	private void restoreScrollPosition(String path) {
//		ScrollPosBuffer scrollPosBuffer = AppController.getService(ScrollPosBuffer.class);
//		Integer savedScrollPos = scrollPosBuffer.restoreScrollPosition(path);
//		if (savedScrollPos != null) {
//			gui.scrollToPosition(savedScrollPos);
//		}
//	}
//
//	private void showUIHelp() {
//		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
//		dlgAlert.setMessage(userInfo.resString(R.string.ui_help_content));
//		dlgAlert.setTitle(userInfo.resString(R.string.ui_help));
//		dlgAlert.setPositiveButton(userInfo.resString(R.string.action_info_ok), new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//			}
//		});
//		dlgAlert.setCancelable(true);
//		dlgAlert.create().show();
//	}
//
//	private void setLocale(String langCode) {
//		Resources res = activity.getResources();
//		// Change locale settings in the app.
//		DisplayMetrics dm = res.getDisplayMetrics();
//		android.content.res.Configuration conf = res.getConfiguration();
//		conf.locale = new Locale(langCode.toLowerCase());
//		res.updateConfiguration(conf, dm);
//	}
//
//	@Override
//	public void onEvent(IEvent event) {
//		//TODO uproszczenie odbierania eventów
//		//TODO problem polimorfizmu - wczesnego wyłapania pochodnych typów eventów
//		//TODO przenieść do klas odpowiedzialnych za działanie
//		if (event instanceof ToolbarBackClickedEvent) {
//
//			backClicked();
//
//		} else if (event instanceof ItemClickedEvent) {
//
//			ScrollPosBuffer scrollPosBuffer = AppController.getService(ScrollPosBuffer.class);
//			FileItem item = ((ItemClickedEvent) event).getItem();
//
//			scrollPosBuffer.storeScrollPosition(fileTreeManager.getCurrentPath(), gui.getCurrentScrollPos());
//			if (item.isDirectory()) {
//				fileTreeManager.goInto(item.getName());
//				updateFileList();
//				gui.scrollToItem(0);
//			} else {
//				showFileContent(item.getName());
//			}
//		} else if (event instanceof ResizedEvent) {
//
//			Logger.debug("Rozmiar grafiki 2D zmieniony: " + ((ResizedEvent) event).getW() + " x " + ((ResizedEvent) event).getH());
//
//		} else if (event instanceof GraphicsInitializedEvent) {
//
//			int w = ((GraphicsInitializedEvent) event).getW();
//			int h = ((GraphicsInitializedEvent) event).getH();
//			Paint paint = ((GraphicsInitializedEvent) event).getPaint();
//
//			//wczytanie pliku i sparsowanie
//			String filePath = fileTreeManager.getCurrentFilePath(fileTreeManager.getCurrentFileName());
//			String fileContent = fileTreeManager.getFileContent(filePath);
//			//inicjalizacja - pierwsze wczytanie pliku
//			chordsManager.load(fileContent, w, h, paint);
//
//			gui.setFontSize(chordsManager.getFontsize());
//			gui.setCRDModel(chordsManager.getCRDModel());
//
//		} else if (event instanceof FontsizeChangedEvent) {
//
//			float fontsize = ((FontsizeChangedEvent) event).getFontsize();
//
//			chordsManager.setFontsize(fontsize);
//			//parsowanie bez ponownego wczytywania pliku i wykrywania kodowania
//			chordsManager.reparse();
//			gui.setCRDModel(chordsManager.getCRDModel());
//
//		}
//	}
	
}
