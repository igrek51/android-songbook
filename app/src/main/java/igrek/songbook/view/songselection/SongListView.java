package igrek.songbook.view.songselection;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.songtree.SongTreeItem;

public class SongListView extends ListView implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	
	private Logger logger = LoggerFactory.getLogger();
	private List<SongTreeItem> items;
	private SongListItemAdapter adapter;
	private HashMap<Integer, Integer> itemHeights = new HashMap<>();
	private OnSongClickListener onClickListener;
	
	{
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public SongListView(Context context) {
		super(context);
	}
	
	public SongListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public SongListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void init(Context context, OnSongClickListener onClickListener) {
		this.onClickListener = onClickListener;
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
		setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		adapter = new SongListItemAdapter(context, null, this);
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
		SongTreeItem item = adapter.getItem(position);
		if (onClickListener != null)
			onClickListener.onSongItemClick(item);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		SongTreeItem item = adapter.getItem(position);
		if (onClickListener != null)
			onClickListener.onSongItemClick(item);
		return true;
	}
	
	public void setItems(List<SongTreeItem> items) {
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
		if (getChildAt(0) == null)
			return 0;
		int sumh = 0;
		for (int i = 0; i < getFirstVisiblePosition(); i++) {
			sumh += getItemHeight(i);
		}
		return sumh - getChildAt(0).getTop();
	}
	
	private int getItemHeight(int position) {
		Integer h = itemHeights.get(position);
		if (h == null)
			logger.warn("Item View = null");
		return h != null ? h : 0;
	}
	
	/**
	 * @param position position of element to scroll (-1 - last element)
	 */
	public void scrollTo(int position) {
		if (position == -1)
			position = items.size() - 1;
		if (position < 0)
			position = 0;
		setSelection(position);
		invalidate();
	}
	
	public void scrollToPosition(int y) {
		// determine nearest element and scroll relative to it
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
