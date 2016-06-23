package igrek.songbook.logic.app;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import igrek.songbook.R;
import igrek.songbook.gui.GUI;
import igrek.songbook.gui.GUIListener;
import igrek.songbook.logic.exceptions.NoSuperItemException;
import igrek.songbook.logic.filetree.FileItem;
import igrek.songbook.logic.filetree.FileTreeManager;
import igrek.songbook.system.output.Output;

//TODO: wypisać TODO

public class App extends BaseApp implements GUIListener {
    
    FileTreeManager fileTreeManager;
    GUI gui;
    
    AppState state;
    
    public App(AppCompatActivity activity) {
        super(activity);
        
        preferences.preferencesLoad();
        
        fileTreeManager = new FileTreeManager();
        //TODO załadowanie zawartości folderu
        
        gui = new GUI(activity, this);
        gui.setTouchController(this);
        gui.showFilesList(fileTreeManager.getCurrentItem());
        state = AppState.ITEMS_LIST;
        
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
        showInfo(info, gui.getMainContent());
    }
    
    
    @Override
    public void menuInit(Menu menu) {
        super.menuInit(menu);
        //setMenuItemVisible(R.id.action_copy, false);
        //item.setTitle(title);
        //item.setIcon(iconRes); //int iconRes
    }
    
    public void goUp() {
        //TODO folder w góre
        //        try {
        //            FileItem current = fileTreeManager.getCurrentItem();
        //            FileItem parent = current.getParent();
        //            fileTreeManager.goUp();
        //            updateItemsList();
        //            if (parent != null) {
        //                int childIndex = parent.getChildIndex(current);
        //                if (childIndex != -1) {
        //                    gui.scrollToItem(childIndex);
        //                }
        //            }
        //        } catch (NoSuperItemException e) {
        //            quit();
        //        }
    }
    
    private void backClicked() {
        goUp();
    }
    
    private void updateItemsList() {
        //TODO: 
//        gui.updateItemsList(fileTreeManager.getCurrentItem(), fileTreeManager.getSelectedItems());
        state = AppState.ITEMS_LIST;
    }
    
    
    @Override
    public void onToolbarBackClicked() {
        backClicked();
    }
    
    @Override
    public void onItemClicked(int position, FileItem item) {
        //TODO: wejście do folderu lub otworzenie pliku
//        if (item.isEmpty()) {
//            onItemEditClicked(position, item);
//        } else {
//            onItemGoIntoClicked(position, item);
//        }
    }
}
