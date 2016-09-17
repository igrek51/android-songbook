package igrek.songbook.logic.app;

import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

import igrek.songbook.R;
import igrek.songbook.events.CanvasScrollEvent;
import igrek.songbook.events.FontsizeChangedEvent;
import igrek.songbook.events.GraphicsInitializedEvent;
import igrek.songbook.events.ItemClickedEvent;
import igrek.songbook.events.ResizedEvent;
import igrek.songbook.events.ShowQuickMenuEvent;
import igrek.songbook.events.ToolbarBackClickedEvent;
import igrek.songbook.events.autoscroll.AutoscrollEndedEvent;
import igrek.songbook.events.autoscroll.AutoscrollRemainingWaitTimeEvent;
import igrek.songbook.events.autoscroll.AutoscrollStartEvent;
import igrek.songbook.events.autoscroll.AutoscrollStartUIEvent;
import igrek.songbook.events.autoscroll.AutoscrollStartedEvent;
import igrek.songbook.events.autoscroll.AutoscrollStopEvent;
import igrek.songbook.events.autoscroll.AutoscrollStopUIEvent;
import igrek.songbook.events.transpose.TransposeEvent;
import igrek.songbook.events.transpose.TransposeResetEvent;
import igrek.songbook.filesystem.Filesystem;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.canvas.quickmenu.QuickMenu;
import igrek.songbook.graphics.gui.GUI;
import igrek.songbook.graphics.gui.ScrollPosBuffer;
import igrek.songbook.graphics.infobar.InfoBarClickAction;
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

//TODO menu z przyciskami: otwarcie kliknięciem (w odpowiednim miejscu), transpozycja 0, 1, 5

//TODO context server service - systemowe operacje, zamiast przekazywania contextu

//TODO teksty wyświetlane użytkownikowi przenieść do strings.xml

//TODO UI help

public class App extends BaseApp implements IEventObserver {
    
    private FileTreeManager fileTreeManager;
    private ChordsManager chordsManager;
    private GUI gui;
    
    private AppState state;
    
    public App(AppCompatActivity activity) {
        super(activity);

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
        AppController.registerEventObserver(TransposeEvent.class, this);
        AppController.registerEventObserver(TransposeResetEvent.class, this);
        AppController.registerEventObserver(FontsizeChangedEvent.class, this);
        AppController.registerEventObserver(AutoscrollRemainingWaitTimeEvent.class, this);
        AppController.registerEventObserver(AutoscrollStartUIEvent.class, this);
        AppController.registerEventObserver(AutoscrollStartedEvent.class, this);
        AppController.registerEventObserver(AutoscrollEndedEvent.class, this);
        AppController.registerEventObserver(AutoscrollStopUIEvent.class, this);
        AppController.registerEventObserver(CanvasScrollEvent.class, this);
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
        showActionInfo("Current directory saved as starting directory.", null, "OK", null);
    }


    private void restoreScrollPosition(String path) {
        ScrollPosBuffer scrollPosBuffer = AppController.getService(ScrollPosBuffer.class);
        Integer savedScrollPos = scrollPosBuffer.restoreScrollPosition(path);
        if (savedScrollPos != null) {
            gui.scrollToPosition(savedScrollPos);
        }
    }


    @Override
    protected View getActiveView() {
        return gui.getMainView();
    }

    @Override
    public void onEvent(IEvent event) {
        //TODO zrobić z tym porządek - uprościć
        //TODO problem polimorfizmu - wyłapania pochodnych typów eventów
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

        } else if (event instanceof TransposeEvent) {

            //TODO przenieść łapanie eventu do chordsmanager
            int t = ((TransposeEvent) event).getT();

            chordsManager.transpose(t);
            gui.setCRDModel(chordsManager.getCRDModel());
            showActionInfo("Transposition: " + chordsManager.getTransposed(), null, "Reset", new InfoBarClickAction() {
                @Override
                public void onClick() {
                    AppController.sendEvent(new TransposeResetEvent());
                }
            });

        } else if (event instanceof TransposeResetEvent) {

            //TODO przenieść łapanie eventu do chordsmanager
            AppController.sendEvent(new TransposeEvent(-chordsManager.getTransposed()));

        } else if (event instanceof FontsizeChangedEvent) {

            float fontsize = ((FontsizeChangedEvent) event).getFontsize();

            chordsManager.setFontsize(fontsize);
            //parsowanie bez ponownego wczytywania pliku i wykrywania kodowania
            chordsManager.reparse();
            gui.setCRDModel(chordsManager.getCRDModel());

        } else if (event instanceof AutoscrollRemainingWaitTimeEvent) {

            long ms = ((AutoscrollRemainingWaitTimeEvent) event).getMs();

            int seconds = (int) ((ms + 500) / 1000);
            showActionInfo("Autoscroll starts in " + seconds + " s.", null, "Stop", new InfoBarClickAction() {
                @Override
                public void onClick() {
                    AppController.sendEvent(new AutoscrollStopEvent());
                }
            });

        } else if (event instanceof AutoscrollStartUIEvent) {

            //TODO event odbierany przez autoscrolla
            //TODO zgeneralizowane wiadomości o rozpoczęciu / zakończeniu autoscrolla
            Autoscroll autoscroll = AppController.getService(Autoscroll.class);

            if (!autoscroll.isRunning()) {
                CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
                if (canvas.canAutoScroll()) {
                    AppController.sendEvent(new AutoscrollStartEvent());
                    showActionInfo("Autoscroll started.", null, "Stop", new InfoBarClickAction() {
                        @Override
                        public void onClick() {
                            AppController.sendEvent(new AutoscrollStopEvent());
                        }
                    });
                } else {
                    showActionInfo("End of file\nAutoscroll stopped.", null, "OK", null);
                }
            } else {
                AppController.sendEvent(new AutoscrollStopUIEvent());
            }

        } else if (event instanceof AutoscrollStartedEvent) {

            showActionInfo("Autoscroll started.", null, "Stop", new InfoBarClickAction() {
                @Override
                public void onClick() {
                    AppController.sendEvent(new AutoscrollStopEvent());
                }
            });

        } else if (event instanceof AutoscrollEndedEvent) {

            showActionInfo("End of file\n" + "Autoscroll stopped.", null, "OK", null);

        } else if (event instanceof AutoscrollStopUIEvent) {

            Autoscroll autoscroll = AppController.getService(Autoscroll.class);
            if (autoscroll.isRunning()) {
                AppController.sendEvent(new AutoscrollStopEvent());
                showActionInfo("Autoscroll stopped.", null, "OK", null);
            }

        } else if (event instanceof CanvasScrollEvent) {

            float dScroll = ((CanvasScrollEvent) event).getdScroll();
            float scroll = ((CanvasScrollEvent) event).getScroll();

            Autoscroll autoscroll = AppController.getService(Autoscroll.class);
            autoscroll.handleCanvasScroll(dScroll, scroll);

        }
    }
}
