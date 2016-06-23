package igrek.songbook.gui.fileslist;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import igrek.songbook.gui.GUIListener;
import igrek.songbook.logic.filetree.FileItem;

public class FilesListView extends ListView implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private List<FileItem> items;
    private FilesItemAdapter adapter;
    private GUIListener guiListener;

    public FilesListView(Context context) {
        super(context);
    }

    public FilesListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FilesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Context context, GUIListener aGuiListener) {
        this.guiListener = aGuiListener;
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new FilesItemAdapter(context, null, guiListener, this);
        setAdapter(adapter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        FileItem item = adapter.getItem(position);
        guiListener.onItemClicked(position, item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem item = adapter.getItem(position);
        guiListener.onItemClicked(position, item);
        return true;
    }

    public void setItems(List<FileItem> items) {
        this.items = items;
        adapter.setDataSource(items);
        invalidate();
    }

    /**
     * @param position pozycja elementu do przescrollowania (-1 - ostatni element)
     */
    public void scrollTo(int position) {
        if (position == -1) position = items.size() - 1;
        if (position < 0) position = 0;
        setSelection(position);
        invalidate();
    }
}
