package igrek.songbook.logic.app;

import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import igrek.songbook.R;
import igrek.songbook.filesystem.Filesystem;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.gui.GUI;
import igrek.songbook.graphics.gui.ScrollPosBuffer;
import igrek.songbook.graphics.infobar.InfoBarClickAction;
import igrek.songbook.logger.Logs;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.controller.dispatcher.IEventObserver;
import igrek.songbook.logic.crdfile.ChordsManager;
import igrek.songbook.logic.events.AutoscrollEndedEvent;
import igrek.songbook.logic.events.AutoscrollRemainingWaitTimeEvent;
import igrek.songbook.logic.events.AutoscrollStartRequestEvent;
import igrek.songbook.logic.events.AutoscrollStartedEvent;
import igrek.songbook.logic.events.CanvasClickedEvent;
import igrek.songbook.logic.events.CanvasScrollEvent;
import igrek.songbook.logic.events.FontsizeChangedEvent;
import igrek.songbook.logic.events.GraphicsInitializedEvent;
import igrek.songbook.logic.events.ItemClickedEvent;
import igrek.songbook.logic.events.ResizedEvent;
import igrek.songbook.logic.events.ToolbarBackClickedEvent;
import igrek.songbook.logic.events.TransposedEvent;
import igrek.songbook.logic.exceptions.NoParentDirException;
import igrek.songbook.logic.filetree.FileItem;
import igrek.songbook.logic.filetree.FileTreeManager;
import igrek.songbook.preferences.Preferences;

//TODO menu z przyciskami: otwarcie kliknięciem (w odpowiednim miejscu), transpozycja 0, 1, 5

//TODO context server service - systemowe operacje, zamiast przekazywania contextu

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
        chordsManager = new ChordsManager();

        gui = new GUI(activity);
        gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
        state = AppState.FILE_LIST;

        Logs.info("Aplikacja uruchomiona.");
    }

    private void registerServices() {
        AppController.registerService(new Filesystem(activity));
        AppController.registerService(new Preferences(activity));

        AppController.registerService(new ScrollPosBuffer());
    }

    private void registerEventObservers() {
        AppController.registerEventObserver(ToolbarBackClickedEvent.class, this);
        AppController.registerEventObserver(ItemClickedEvent.class, this);
        AppController.registerEventObserver(ResizedEvent.class, this);
        AppController.registerEventObserver(GraphicsInitializedEvent.class, this);
        AppController.registerEventObserver(TransposedEvent.class, this);
        AppController.registerEventObserver(FontsizeChangedEvent.class, this);
        AppController.registerEventObserver(AutoscrollRemainingWaitTimeEvent.class, this);
        AppController.registerEventObserver(AutoscrollStartRequestEvent.class, this);
        AppController.registerEventObserver(AutoscrollStartedEvent.class, this);
        AppController.registerEventObserver(AutoscrollEndedEvent.class, this);
        AppController.registerEventObserver(CanvasClickedEvent.class, this);
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
            chordsManager.autoscrollStop();
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
        showReusableActionInfo("Zapisano obecny folder jako startowy.", gui.getMainView(), "OK", null);
    }


    private void restoreScrollPosition(String path) {
        ScrollPosBuffer scrollPosBuffer = AppController.getService(ScrollPosBuffer.class);
        Integer savedScrollPos = scrollPosBuffer.restoreScrollPosition(path);
        if (savedScrollPos != null) {
            gui.scrollToPosition(savedScrollPos);
        }
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

        } else if (event instanceof TransposedEvent) {

            int t = ((TransposedEvent) event).getT();

            chordsManager.transpose(t);
            gui.setCRDModel(chordsManager.getCRDModel());
            showReusableActionInfo("Transpozycja: " + chordsManager.getTransposed(), gui.getCanvas(), "Zeruj", new InfoBarClickAction() {
                @Override
                public void onClick() {
                    AppController.sendEvent(new TransposedEvent(-chordsManager.getTransposed()));
                }
            });

        } else if (event instanceof FontsizeChangedEvent) {

            float fontsize = ((FontsizeChangedEvent) event).getFontsize();

            chordsManager.setFontsize(fontsize);
            //parsowanie bez ponownego wczytywania pliku i wykrywania kodowania
            chordsManager.reparse();
            gui.setCRDModel(chordsManager.getCRDModel());

        } else if (event instanceof AutoscrollRemainingWaitTimeEvent) {

            long ms = ((AutoscrollRemainingWaitTimeEvent) event).getMs();

            int seconds = (int) ((ms + 500) / 1000);
            showReusableActionInfo("Autoprzewijanie za " + seconds + " s.", gui.getCanvas(), "Zatrzymaj", new InfoBarClickAction() {
                @Override
                public void onClick() {
                    chordsManager.autoscrollStop();
                }
            });

        } else if (event instanceof AutoscrollStartRequestEvent) {

            if (!chordsManager.getAutoscroll().isRunning()) {
                CanvasGraphics canvas = AppController.getService(CanvasGraphics.class);
                if (canvas.canAutoScroll()) {
                    chordsManager.autoscrollStart(gui.getCanvasScroll());
                    showReusableActionInfo("Rozpoczęto autoprzewijanie.", gui.getCanvas(), "Zatrzymaj", new InfoBarClickAction() {
                        @Override
                        public void onClick() {
                            chordsManager.autoscrollStop();
                        }
                    });
                } else {
                    showReusableActionInfo("EOF - Zatrzymano autoprzewijanie.", gui.getCanvas(), "OK", null);
                }
            } else {
                AppController.sendEvent(new CanvasClickedEvent());
            }

        } else if (event instanceof AutoscrollStartedEvent) {

            showReusableActionInfo("Rozpoczęto autoprzewijanie.", gui.getCanvas(), "Zatrzymaj", new InfoBarClickAction() {
                @Override
                public void onClick() {
                    chordsManager.autoscrollStop();
                }
            });

        } else if (event instanceof AutoscrollEndedEvent) {

            showReusableActionInfo("EOF - Zatrzymano autoprzewijanie.", gui.getCanvas(), "OK", null);

        } else if (event instanceof CanvasClickedEvent) {

            if (chordsManager.getAutoscroll().isRunning()) {
                chordsManager.autoscrollStop();
                showReusableActionInfo("Zatrzymano autoprzewijanie.", gui.getCanvas(), "OK", null);
            }

        } else if (event instanceof CanvasScrollEvent) {

            float dScroll = ((CanvasScrollEvent) event).getdScroll();
            float scroll = ((CanvasScrollEvent) event).getScroll();

            chordsManager.getAutoscroll().handleCanvasScroll(dScroll, scroll);

        }
    }
}
