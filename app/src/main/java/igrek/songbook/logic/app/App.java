package igrek.songbook.logic.app;

import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import igrek.songbook.R;
import igrek.songbook.graphics.gui.GUI;
import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.logic.crdfile.ChordsManager;
import igrek.songbook.logic.exceptions.NoParentDirException;
import igrek.songbook.logic.filetree.FileItem;
import igrek.songbook.logic.filetree.FileTreeManager;
import igrek.songbook.output.Output;

//TODO autoscroll, obsługa gestem ?
//TODO gesty
//TODO przywracanie scrolla po powrocie z podglądu i przechodzeniu w górę
//TODO import z todo tree - powrtót scrolla, przewijanie, logger

public class App extends BaseApp implements GUIListener {
    
    private FileTreeManager fileTreeManager;
    private ChordsManager chordsManager;
    private GUI gui;
    
    private AppState state;
    
    public App(AppCompatActivity activity) {
        super(activity);
        
        preferences.preferencesLoad();

        fileTreeManager = new FileTreeManager(files, getHomePath());
        chordsManager = new ChordsManager();
        
        gui = new GUI(activity, this);
        gui.setTouchController(this);
        gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
        state = AppState.FILE_LIST;
        
        Output.debug("Aplikacja uruchomiona.");
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
        } else if (id == R.id.action_home) {
            homeClicked();
            return true;
        } else if (id == R.id.action_font_increase) {
            changeFontSize(+1);
            return true;
        } else if (id == R.id.action_font_decrease) {
            changeFontSize(-1);
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
            //TODO: scrollowanie do ostatnio otwartego folderu
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
        gui.showFileContent();
    }

    private void changeFontSize(float change){
        chordsManager.setFontsize(chordsManager.getFontsize() + change);
        chordsManager.setLineheight(chordsManager.getFontsize() + 1);
        Output.debug("rozmiar czcionki: " + chordsManager.getFontsize() + ", rozmiar wiersza: " + chordsManager.getLineheight());
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
        Output.debug("Rozmiar grafiki 2D zmieniony: " + w + " x " + h);
    }

    @Override
    public void onGraphicsInitialized(int w, int h, Paint paint) {
        //wczytanie pliku i sparsowanie
        String filePath = fileTreeManager.getCurrentFilePath(fileTreeManager.getCurrentFileName());
        String fileContent = fileTreeManager.getFileContent(filePath);
        chordsManager.load(fileContent, w, h, paint);

        gui.setFontSize(chordsManager.getFontsize(), chordsManager.getLineheight());
        gui.setCRDModel(chordsManager.getCRDModel());
    }

    @Override
    public void onTransposed(int t) {
        chordsManager.transpose(t);
        gui.setCRDModel(chordsManager.getCRDModel());
    }

    private String getHomePath() {
        return preferences.getString("startPath", "/");
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
}
