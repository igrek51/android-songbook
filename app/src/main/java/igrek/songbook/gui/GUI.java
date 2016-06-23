package igrek.songbook.gui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import igrek.songbook.R;
import igrek.songbook.gui.fileslist.FilesListView;
import igrek.songbook.logic.filetree.FileItem;

public class GUI extends GUIBase {

    private ActionBar actionBar;
    private FilesListView itemsListView;

    public GUI(AppCompatActivity activity, GUIListener guiListener) {
        super(activity, guiListener);
    }

    @Override
    protected void init() {
        activity.setContentView(R.layout.activity_main);

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

        //  główna zawartość
        mainContent = (RelativeLayout) activity.findViewById(R.id.mainContent);
    }

    public void showFilesList(final FileItem currentItem) {
        View itemsListLayout = setMainContentLayout(R.layout.files_list);

        itemsListView = (FilesListView) itemsListLayout.findViewById(R.id.filesList);

        itemsListView.init(activity, guiListener);

        updateItemsList(currentItem);
    }

    public void updateItemsList(FileItem currentItem) {
        setTitle(currentItem.getFilename());
        //lista elementów
        //TODO wylistowanie aktualnego folderu i wyświetlenie plików i folderów
        //itemsListView.setItems(currentItem.getChildren());
    }

    public void scrollToItem(int position){
        itemsListView.scrollTo(position);
    }

    public void setTitle(String title){
        actionBar.setTitle(title);
    }
}
