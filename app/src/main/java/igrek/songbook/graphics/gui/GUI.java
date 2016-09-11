package igrek.songbook.graphics.gui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

import igrek.songbook.R;
import igrek.songbook.graphics.canvas.CanvasGraphics;
import igrek.songbook.graphics.gui.filelist.FileListView;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.crdfile.CRDModel;
import igrek.songbook.logic.events.ToolbarBackClickedEvent;
import igrek.songbook.logic.filetree.FileItem;

public class GUI extends GUIBase {

    private ActionBar actionBar;
    private FileListView itemsListView;
    private View mainView;
    private CanvasGraphics canvas = null;

    public GUI(AppCompatActivity activity) {
        super(activity);
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
                AppController.sendEvent(new ToolbarBackClickedEvent());
            }
        });

        mainView = activity.findViewById(R.id.mainLayout);

        itemsListView = (FileListView) activity.findViewById(R.id.filesList);

        itemsListView.init(activity);

        updateFileList(currentDir, items);
    }

    public void showFileContent() {

        setFullscreen(true);

        canvas = new CanvasGraphics(activity);

        mainView = canvas;

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

    public void setCRDModel(CRDModel model) {
        canvas.setCRDModel(model);
    }

    public void setFontSize(float fontsize) {
        canvas.setFontSizes(fontsize);
    }

    public Integer getCurrentScrollPos() {
        return itemsListView.getCurrentScrollPosition();
    }

    public void scrollToPosition(int y) {
        itemsListView.scrollToPosition(y);
    }
}
