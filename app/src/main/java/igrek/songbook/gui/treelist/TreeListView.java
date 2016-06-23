package igrek.todotree.gui.treelist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import igrek.todotree.gui.GUIListener;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.system.output.Output;

public class TreeListView extends ListView implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private List<TreeItem> items;
    private TreeItemAdapter adapter;
    private GUIListener guiListener;

    /** początkowa pozycja kursora przy rozpoczęciu przeciągania (ulega zmianie przy zamianie elementów) */
    private float startTouchY;
    /** aktualna pozycja kursora względem ekranu i listview (bez scrollowania) */
    private float lastTouchY;

    /** pozycja aktualnie przeciąganego elementu */
    private Integer draggedItemPos = null;
    /** widok aktualnie przeciąganego elementu */
    private View draggedItemView = null;
    /** oryginalna górna pozycja niewidocznego elementu na liście, którego bitmapa jest przeciągana */
    private Integer draggedItemViewTop = null;

    /** aktualne położenie scrolla */
    private ListScrollOffset scrollOffset = new ListScrollOffset();
    /** położenie scrolla przy rozpoczęciu przeciągania */
    private ListScrollOffset scrollStart = new ListScrollOffset();

    private int scrollState = SCROLL_STATE_IDLE;

    private BitmapDrawable hoverBitmap;
    private Rect hoverBitmapBounds;

    private final int SMOOTH_SCROLL_EDGE_DP = 180;
    private int SMOOTH_SCROLL_EDGE_PX;
    private final float SMOOTH_SCROLL_FACTOR = 0.34f;
    private final int SMOOTH_SCROLL_DURATION = 10;

    private final float ITEMS_REPLACE_COVER = 0.65f;

    private final int HOVER_BORDER_THICKNESS = 5;
    private final int HOVER_BORDER_COLOR = 0xccb0b0b0;

    public TreeListView(Context context) {
        super(context);
    }

    public TreeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TreeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Context context, GUIListener aGuiListener) {
        this.guiListener = aGuiListener;
        setOnScrollListener(this);
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        SMOOTH_SCROLL_EDGE_PX = (int) (SMOOTH_SCROLL_EDGE_DP / metrics.density);
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapter = new TreeItemAdapter(context, null, guiListener, this);
        setAdapter(adapter);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (draggedItemPos != null) {
                    lastTouchY = event.getY();
                    handleItemDragging();
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                itemDraggingStopped();
                break;
            case MotionEvent.ACTION_CANCEL:
                itemDraggingStopped();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (draggedItemPos != null) {
            draggedItemView = getItemView(draggedItemPos);
            if (draggedItemView != null) {
                draggedItemView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverBitmap != null) {
            updateHoverBitmap();
            hoverBitmap.draw(canvas);
        }
    }

    public void setItemsAndSelected(List<TreeItem> items, List<Integer> selectedPositions) {
        adapter.setSelections(selectedPositions);
        setItems(items);
    }

    public void setItems(List<TreeItem> items) {
        this.items = items;
        adapter.setDataSource(items);
        invalidate();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        if (position == items.size()) {
            //nowy element
            guiListener.onAddItemClicked();
        } else {
            //istniejący element
            TreeItem item = adapter.getItem(position);
            guiListener.onItemClicked(position, item);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        itemDraggingStopped();
        if (position == items.size()) {
            //nowy element na końcu
            guiListener.onAddItemClicked();
        } else {
            //tryb zaznaczania elementów
            guiListener.onItemLongClick(position);
        }
        return true;
    }


    private void updateHoverBitmap() {
        if (draggedItemViewTop != null && draggedItemPos != null) {
            float dy = lastTouchY - startTouchY;
            hoverBitmapBounds.offsetTo(0, draggedItemViewTop + (int) dy);
            hoverBitmap.setBounds(hoverBitmapBounds);
        }
    }

    private BitmapDrawable getAndAddHoverView(View v) {
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmapWithBorder(v);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        hoverBitmapBounds = new Rect(left, top, left + v.getWidth(), top + v.getHeight());
        drawable.setBounds(hoverBitmapBounds);

        return drawable;
    }

    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(HOVER_BORDER_THICKNESS);
        paint.setColor(HOVER_BORDER_COLOR);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }

    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }


    private View getViewByPosition(int itemPos) {
        if (itemPos < 0) return null;
        if (itemPos >= items.size()) return null;
        int itemNum = itemPos - getFirstVisiblePosition();
        return getChildAt(itemNum);
    }

    private int getItemHeight(int position) {
        Integer h = adapter.getViewHeight(position, this);
        if(h == null){
            Output.log("Item View = null");
        }
        return h != null ? h.intValue() : 0;
    }

    private View getItemView(int position) {
        return adapter.getStoredView(position);
    }


    private final static TypeEvaluator<Rect> rectBoundsEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };


    private void itemDraggingStarted(int position, View itemView) {
        draggedItemPos = position;
        draggedItemView = itemView;
        draggedItemViewTop = itemView.getTop();
        scrollStart.copyFrom(scrollOffset);

        hoverBitmap = getAndAddHoverView(draggedItemView);
        draggedItemView.setVisibility(INVISIBLE);

        invalidate();
    }

    private void handleItemDragging() {
        float dyTotal = lastTouchY - startTouchY + (scrollOffset.diff(scrollStart, this));

        if (draggedItemViewTop == null) {
            Output.error("draggedItemViewTop = null");
            return;
        }
        if (draggedItemPos == null) {
            Output.error("draggedItemPos = null");
            return;
        }

        int step = 0;
        int deltaH = 0;

        if (dyTotal > 0) { //przesuwanie w dół
            while(true) {
                if (draggedItemPos + step + 1 >= items.size()) break;
                int downHeight = getItemHeight(draggedItemPos + step + 1);
                if(downHeight == 0) break;
                if (dyTotal - deltaH > downHeight * ITEMS_REPLACE_COVER) {
                    step++;
                    deltaH += downHeight;
                }else{
                    break;
                }
            }
        } else if (dyTotal < 0) { //przesuwanie w górę
            while(true) {
                if (draggedItemPos + step - 1 < 0) break;
                int upHeight = getItemHeight(draggedItemPos + step - 1);
                if(upHeight == 0) break;
                if (-dyTotal + deltaH > upHeight * ITEMS_REPLACE_COVER) {
                    step--;
                    deltaH -= upHeight;
                } else {
                    break;
                }
            }
        }

        if (step != 0) {
            int targetPosition = draggedItemPos + step;
            items = guiListener.onItemMoved(draggedItemPos, step);
            adapter.setDataSource(items);

            startTouchY += deltaH;
            draggedItemViewTop += deltaH;

            draggedItemPos = targetPosition;
            if (draggedItemView != null) {
                draggedItemView.setVisibility(View.VISIBLE);
            }
            draggedItemView = getItemView(draggedItemPos);
            if (draggedItemView != null) {
                draggedItemView.setVisibility(View.INVISIBLE);
            }
        }

        handleScrolling();

        invalidate();
    }

    private void itemDraggingStopped() {
        if (draggedItemPos != null && draggedItemViewTop != null) {
            //wyłączenie automatycznego ustawiania pozycji hover bitmapy
            draggedItemPos = null;
            //animacja powrotu do aktualnego połozenia elementu
            hoverBitmapBounds.offsetTo(0, draggedItemViewTop - (scrollOffset.diff(scrollStart, this)));
            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverBitmap, "bounds", rectBoundsEvaluator, hoverBitmapBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (draggedItemView != null) {
                        draggedItemView.setVisibility(VISIBLE);
                    }
                    draggedItemView = null;
                    hoverBitmap = null;
                    draggedItemViewTop = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
            draggedItemPos = null;
            draggedItemView = null;
            draggedItemViewTop = null;
        }
    }


    public boolean handleScrolling() {
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            int offset = computeVerticalScrollOffset();
            int height = getHeight();
            int extent = computeVerticalScrollExtent();
            int range = computeVerticalScrollRange();
            int hoverViewTop = hoverBitmapBounds.top;
            int hoverHeight = hoverBitmapBounds.height();

            if (draggedItemPos != null) {
                if (hoverViewTop <= SMOOTH_SCROLL_EDGE_PX && offset > 0) {
                    int scrollDistance = (int) ((hoverViewTop - SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR);
                    smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
                    return true;
                }
                if (hoverViewTop + hoverHeight >= height - SMOOTH_SCROLL_EDGE_PX && (offset + extent) < range) {
                    int scrollDistance = (int) ((hoverViewTop + hoverHeight - height + SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR);
                    smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //scrollOffset = getREALScrollPosition();
        if(totalItemCount > 0) {
            scrollOffset.setFirstVisiblePosition(getFirstVisiblePosition());
            scrollOffset.setItemHeights(adapter.getItemHeights());
            scrollOffset.setTopview(getChildAt(0) == null ? 0 : getChildAt(0).getTop());
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
        if (scrollState == SCROLL_STATE_IDLE) {
            if (draggedItemPos != null) {
                handleScrolling();
            }
        }
    }

    private int getREALScrollPosition() {
        if (getChildAt(0) == null) {
            return 0;
        }
        int sumh = 0;
        for(int i=0; i<getFirstVisiblePosition(); i++){
            sumh += getItemHeight(i);
        }
        return sumh - getChildAt(0).getTop();
    }

    /**
     * @param position pozycja elementu do przescrollowania (-1 - ostatni element)
     */
    public void scrollTo(int position){
        if(position == -1) position = items.size() - 1;
        if(position < 0) position = 0;
        setSelection(position);
        invalidate();
    }

    public void onItemMoveButtonPressed(int position, TreeItem item, View itemView, float touchX, float touchY) {
        startTouchY = itemView.getTop() + touchY;
        lastTouchY = startTouchY;
        itemDraggingStarted(position, itemView);
    }

    public void onItemMoveButtonReleased(int position, TreeItem item, View itemView, float touchX, float touchY) {
        itemDraggingStopped();
    }

    public boolean onItemMoveLongPressed(int position, TreeItem item){
        if(position == 0){
            //przeniesienie na koniec
            itemDraggingStopped();
            items = guiListener.onItemMoved(position, items.size() - 1);
            adapter.setDataSource(items);
            scrollTo(items.size() - 1);
            return true;
        }
        if(position == items.size() - 1){
            //przeniesienie na początek
            itemDraggingStopped();
            items = guiListener.onItemMoved(position, -(items.size() - 1));
            adapter.setDataSource(items);
            scrollTo(0);
            return true;
        }
        return false;
    }
}
