package igrek.songbook.graphics.gui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

import igrek.songbook.R;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.gui.filelist.FileListView;
import igrek.songbook.logic.crdfile.CRDModel;
import igrek.songbook.logic.filetree.FileItem;

public class GUI extends GUIBase {

    private ActionBar actionBar;
    private FileListView itemsListView;
    private View mainView;
    private CanvasGraphics canvas = null;

    public GUI(AppCompatActivity activity, GUIListener guiListener) {
        super(activity, guiListener);
    }

    @Override
    protected void init() {

    }

    public void showFileList(String currentDir, List<FileItem> items) {

        setFullscreen(false);

        activity.setContentView(R.layout.files_list);

        //toolbar
        Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
        activity.setSupportActionBar(toolbar1);
        actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar1.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guiListener.onToolbarBackClicked();
            }
        });

        mainView = activity.findViewById(R.id.mainLayout);

        itemsListView = (FileListView) activity.findViewById(R.id.filesList);

        itemsListView.init(activity, guiListener);

        updateFileList(currentDir, items);
    }

    public void showFileContent(String filename) {

        setFullscreen(true);

        canvas = new CanvasGraphics(activity, guiListener);

        canvas.setFilename(filename);

        activity.setContentView(canvas);
    }

    public void updateFileList(String currentDir, List<FileItem> items) {
        setTitle(currentDir);
        //lista elementów
        itemsListView.setItems(items);
    }

    public void scrollToItem(int position) {
        itemsListView.scrollTo(position);
    }

    public void setTitle(String title) {
        actionBar.setTitle(title);
    }

    public View getMainView() {
        return mainView;
    }

    public void setCRDModel(CRDModel model){
        canvas.setCRDModel(model);
    }
}
