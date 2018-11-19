package igrek.songbook.songselection;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class SongListView extends ListView implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	
	private List<SongTreeItem> items;
	private SongListItemAdapter adapter;
	private OnSongClickListener onClickListener;
	
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
		adapter = new SongListItemAdapter(context, null);
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
	}
	
	public ListScrollPosition getCurrentScrollPosition() {
		int yOffset = 0;
		if (getChildCount() > 0) {
			yOffset = -getChildAt(0).getTop();
		}
		return new ListScrollPosition(getFirstVisiblePosition(), yOffset);
	}
	
	/**
	 * @param position of element to scroll
	 */
	private void scrollTo(int position) {
		setSelection(position);
		invalidate();
	}
	
	public void scrollToBeginning() {
		scrollTo(0);
	}
	
	public void restoreScrollPosition(ListScrollPosition scrollPosition) {
		if (scrollPosition != null) {
			// scroll to first position
			setSelection(scrollPosition.getFirstVisiblePosition());
			// and move a little by y offset
			smoothScrollBy(scrollPosition.getYOffsetPx(), 50);
			invalidate();
		}
	}
}
