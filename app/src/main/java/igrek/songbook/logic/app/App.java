package igrek.songbook.logic.app;

import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import igrek.songbook.R;
import igrek.songbook.graphics.gui.GUI;
import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.logic.crd.CRDModel;
import igrek.songbook.logic.crd.CRDParser;
import igrek.songbook.logic.exceptions.NoParentDirException;
import igrek.songbook.logic.filetree.FileItem;
import igrek.songbook.logic.filetree.FileTreeManager;
import igrek.songbook.settings.Config;
import igrek.songbook.output.Output;

//TODO: wypisać TODO

//TODO: transpozycja akordów

public class App extends BaseApp implements GUIListener {
    
    FileTreeManager fileTreeManager;
    GUI gui;
    
    AppState state;
    
    public App(AppCompatActivity activity) {
        super(activity);
        
        preferences.preferencesLoad();

        fileTreeManager = new FileTreeManager(files, preferences.getString("startPath", "/"));
        
        gui = new GUI(activity, this);
        gui.setTouchController(this);
        gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
        state = AppState.FILE_LIST;
        
        Output.log("Aplikacja uruchomiona.");
    }
    
    @Override
    public void quit() {
        preferences.preferencesSave();
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
        }
        return false;
    }
    
    @Override
    public boolean onKeyBack() {
        backClicked();
        return true;
    }
    
    
    public void showInfo(String info) {
        showInfo(info, gui.getMainView());
    }
    
    
    @Override
    public void menuInit(Menu menu) {
        super.menuInit(menu);
        //setMenuItemVisible(R.id.action_copy, false);
        //item.setTitle(title);
        //item.setIcon(iconRes); //int iconRes
    }
    
    public void goUp() {
        try {
            fileTreeManager.goUp();
            updateFileList();
            //TODO: scrollowanie do ostatnio otwartej pozycji
            //            if (parent != null) {
            //                int childIndex = parent.getChildIndex(current);
            //                if (childIndex != -1) {
            //                    gui.scrollToItem(childIndex);
            //                }
            //            }
        } catch (NoParentDirException e) {
            quit();
        }
    }
    
    private void backClicked() {
        if (state == AppState.FILE_LIST) {
            goUp();
        } else if (state == AppState.FILE_CONTENT) {
            state = AppState.FILE_LIST;
            gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
        }
    }
    
    private void updateFileList() {
        gui.updateFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
        state = AppState.FILE_LIST;
    }

    private void showFileContent(String filename) {
        state = AppState.FILE_CONTENT;
        fileTreeManager.setCurrentFileName(filename);
        gui.showFileContent(filename);
    }
    
    
    @Override
    public void onToolbarBackClicked() {
        backClicked();
    }
    
    @Override
    public void onItemClicked(int position, FileItem item) {
        if (item.isDirectory()) {
            fileTreeManager.goInto(item.getName());
            updateFileList();
        } else {
            showFileContent(item.getName());
        }
    }

    @Override
    public void onResized(int w, int h) {
        Output.log("Rozmiar grafiki 2D zmieniony: " + w + " x " + h);
    }

    @Override
    public void onGraphicsInitialized(int w, int h, Paint paint) {
        CRDParser crdParser = new CRDParser();

        String filePath = fileTreeManager.getCurrentFilePath(fileTreeManager.getCurrentFileName());
        CRDModel crdModel = crdParser.parseFileContent(fileTreeManager.getFileContent(filePath), w, Config.Fonts.lineheight, paint);
        gui.setCRDModel(crdModel);
    }
}
