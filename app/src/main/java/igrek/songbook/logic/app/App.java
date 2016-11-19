package igrek.songbook.logic.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;

import java.util.Locale;

import igrek.songbook.R;
import igrek.songbook.events.FontsizeChangedEvent;
import igrek.songbook.events.GraphicsInitializedEvent;
import igrek.songbook.events.ItemClickedEvent;
import igrek.songbook.events.ResizedEvent;
import igrek.songbook.events.ShowQuickMenuEvent;
import igrek.songbook.events.ToolbarBackClickedEvent;
import igrek.songbook.events.autoscroll.AutoscrollStopEvent;
import igrek.songbook.filesystem.Filesystem;
import igrek.songbook.graphics.canvas.quickmenu.QuickMenu;
import igrek.songbook.graphics.gui.GUI;
import igrek.songbook.graphics.gui.ScrollPosBuffer;
import igrek.songbook.logger.Logs;
import igrek.songbook.logic.autoscroll.Autoscroll;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.crdfile.ChordsManager;
import igrek.songbook.logic.exceptions.NoParentDirException;
import igrek.songbook.logic.filetree.FileItem;
import igrek.songbook.logic.filetree.FileTreeManager;
import igrek.songbook.logic.music.transposer.ChordsTransposer;
import igrek.songbook.preferences.Preferences;
import igrek.songbook.resources.UserInfoService;

public class App extends BaseApp implements IEventObserver {
    
    private FileTreeManager fileTreeManager;
    private ChordsManager chordsManager;
    private UserInfoService userInfo;
    private GUI gui;
    
    private AppState state;
    
    public App(AppCompatActivity activity) {
        super(activity);

        //setLocale("en"); // angielska wersja językowa

        registerServices();
        registerEventObservers();

        fileTreeManager = new FileTreeManager(getHomePath());

        chordsManager = AppController.getService(ChordsManager.class);

        gui = new GUI(activity);
        gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
        state = AppState.FILE_LIST;

        Logs.info("Aplikacja uruchomiona.");
    }

    private void registerServices() {
        AppController.registerService(new Filesystem(activity));
        AppController.registerService(new Preferences(activity));

        userInfo = new UserInfoService(activity);

        AppController.registerService(new ChordsManager());
        AppController.registerService(new ScrollPosBuffer());
        AppController.registerService(new ChordsTransposer());
        AppController.registerService(new Autoscroll());
    }

    private void registerEventObservers() {
        AppController.registerEventObserver(ToolbarBackClickedEvent.class, this);
        AppController.registerEventObserver(ItemClickedEvent.class, this);
        AppController.registerEventObserver(ResizedEvent.class, this);
        AppController.registerEventObserver(GraphicsInitializedEvent.class, this);
        AppController.registerEventObserver(FontsizeChangedEvent.class, this);
    }
    
    @Override
    public void quit() {
        Preferences preferences = AppController.getService(Preferences.class);
        preferences.saveAll();
        super.quit();
    }
    
    @Override
    public boolean optionsSelect(int id) {
        if (id == R.id.action_minimize) {
            minimize();
            return true;
        } else if (id == R.id.action_exit) {
            quit();
            return true;
        } else if (id == R.id.action_home) {
            homeClicked();
            return true;
        } else if (id == R.id.action_sethomedir) {
            setHomePath();
            return true;
        } else if (id == R.id.action_ui_help) {
            showUIHelp();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onKeyBack() {
        backClicked();
        return true;
    }


    @Override
    public void menuInit(Menu menu) {
        super.menuInit(menu);
    }
    
    public void goUp() {
        try {
            fileTreeManager.goUp();
            updateFileList();
            //scrollowanie do ostatnio otwartego folderu
            restoreScrollPosition(fileTreeManager.getCurrentPath());
        } catch (NoParentDirException e) {
            quit();
        }
    }
    
    private void backClicked() {
        if (state == AppState.FILE_LIST) {
            goUp();
        } else if (state == AppState.FILE_CONTENT) {

            QuickMenu quickMenu = AppController.getService(QuickMenu.class);
            if (quickMenu.isVisible()) {
                AppController.sendEvent(new ShowQuickMenuEvent(false));
            } else {

                AppController.sendEvent(new AutoscrollStopEvent());
                state = AppState.FILE_LIST;
                gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());

                keepScreenOff(activity);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        restoreScrollPosition(fileTreeManager.getCurrentPath());
                    }
                });

            }

        }
    }
    
    private void updateFileList() {
        gui.updateFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
        state = AppState.FILE_LIST;
    }

    private void showFileContent(String filename) {
        state = AppState.FILE_CONTENT;
        fileTreeManager.setCurrentFileName(filename);
        gui.showFileContent();
        if (KEEP_SCREEN_ON) {
            keepScreenOn(activity);
        }
    }


    private String getHomePath() {
        Preferences preferences = AppController.getService(Preferences.class);
        return preferences.startPath;
    }

    private boolean isInHomeDir() {
        return fileTreeManager.getCurrentPath().equals(FileTreeManager.trimEndSlash(getHomePath()));
    }

    private void homeClicked() {
        if (isInHomeDir()) {
            quit();
        } else {
            fileTreeManager.goTo(getHomePath());
            updateFileList();
        }
    }

    private void setHomePath() {
        Preferences preferences = AppController.getService(Preferences.class);
        String homeDir = fileTreeManager.getCurrentPath();
        preferences.startPath = homeDir;
        preferences.saveAll();
        userInfo.showActionInfo(R.string.starting_directory_saved, null, userInfo.resString(R.string.action_info_ok), null);
    }


    private void restoreScrollPosition(String path) {
        ScrollPosBuffer scrollPosBuffer = AppController.getService(ScrollPosBuffer.class);
        Integer savedScrollPos = scrollPosBuffer.restoreScrollPosition(path);
        if (savedScrollPos != null) {
            gui.scrollToPosition(savedScrollPos);
        }
    }

    private void showUIHelp() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
        dlgAlert.setMessage(userInfo.resString(R.string.ui_help_content));
        dlgAlert.setTitle(userInfo.resString(R.string.ui_help));
        dlgAlert.setPositiveButton(userInfo.resString(R.string.action_info_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void setLocale(String langCode) {
        Resources res = activity.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(langCode.toLowerCase());
        res.updateConfiguration(conf, dm);
    }

    @Override
    public void onEvent(IEvent event) {
        //TODO uproszczenie odbierania eventów
        //TODO problem polimorfizmu - wczesnego wyłapania pochodnych typów eventów
        //TODO przenieść do klas odpowiedzialnych za działanie
        if (event instanceof ToolbarBackClickedEvent) {

            backClicked();

        } else if (event instanceof ItemClickedEvent) {

            ScrollPosBuffer scrollPosBuffer = AppController.getService(ScrollPosBuffer.class);
            FileItem item = ((ItemClickedEvent) event).getItem();

            scrollPosBuffer.storeScrollPosition(fileTreeManager.getCurrentPath(), gui.getCurrentScrollPos());
            if (item.isDirectory()) {
                fileTreeManager.goInto(item.getName());
                updateFileList();
                gui.scrollToItem(0);
            } else {
                showFileContent(item.getName());
            }
        } else if (event instanceof ResizedEvent) {

            Logs.debug("Rozmiar grafiki 2D zmieniony: " + ((ResizedEvent) event).getW() + " x " + ((ResizedEvent) event).getH());

        } else if (event instanceof GraphicsInitializedEvent) {

            int w = ((GraphicsInitializedEvent) event).getW();
            int h = ((GraphicsInitializedEvent) event).getH();
            Paint paint = ((GraphicsInitializedEvent) event).getPaint();

            //wczytanie pliku i sparsowanie
            String filePath = fileTreeManager.getCurrentFilePath(fileTreeManager.getCurrentFileName());
            String fileContent = fileTreeManager.getFileContent(filePath);
            //inicjalizacja - pierwsze wczytanie pliku
            chordsManager.load(fileContent, w, h, paint);

            gui.setFontSize(chordsManager.getFontsize());
            gui.setCRDModel(chordsManager.getCRDModel());

        } else if (event instanceof FontsizeChangedEvent) {

            float fontsize = ((FontsizeChangedEvent) event).getFontsize();

            chordsManager.setFontsize(fontsize);
            //parsowanie bez ponownego wczytywania pliku i wykrywania kodowania
            chordsManager.reparse();
            gui.setCRDModel(chordsManager.getCRDModel());

        }
    }
}
