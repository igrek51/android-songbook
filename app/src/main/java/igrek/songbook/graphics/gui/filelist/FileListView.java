package igrek.songbook.graphics.gui.filelist;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;

import igrek.songbook.graphics.gui.GUIListener;
import igrek.songbook.logger.Logs;
import igrek.songbook.logic.filetree.FileItem;

public class FileListView extends ListView implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private List<FileItem> items;
    private FileItemAdapter adapter;
    private GUIListener guiListener;

    private HashMap<Integer, Integer> itemHeights = new HashMap<>();

    public FileListView(Context context) {
        super(context);
    }

    public FileListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FileListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Context context, GUIListener aGuiListener) {
        this.guiListener = aGuiListener;
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new FileItemAdapter(context, null, guiListener, this);
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
        calculateViewHeights();
    }

    private void calculateViewHeights() {
        int measureSpecW = MeasureSpec.makeMeasureSpec(this.getWidth(), MeasureSpec.EXACTLY);
        int measureSpecH = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        itemHeights = new HashMap<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            View mView = adapter.getView(i, null, this);
            mView.measure(measureSpecW, measureSpecH);
            itemHeights.put(i, mView.getMeasuredHeight());
        }
    }

    private int getRealScrollPosition() {
        if (getChildAt(0) == null) {
            return 0;
        }
        int sumh = 0;
        for (int i = 0; i < getFirstVisiblePosition(); i++) {
            sumh += getItemHeight(i);
        }
        //separatory
        //sumh += getFirstVisiblePosition() * getDividerHeight();
        return sumh - getChildAt(0).getTop();
    }

    private int getItemHeight(int position) {
        Integer h = itemHeights.get(position);
        if (h == null) {
            Logs.warn("Item View = null");
        }
        return h != null ? h : 0;
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

    public void scrollToPosition(int y) {
        //wyznaczenie najbliższego elementu i przesunięcie względem niego
        int position = 0;
        while (y > itemHeights.get(position)) {
            y -= itemHeights.get(position);
            position++;
        }

        setSelection(position);
        smoothScrollBy(y, 50);

        invalidate();
    }

    public Integer getCurrentScrollPosition() {
        return getRealScrollPosition();
    }
}
