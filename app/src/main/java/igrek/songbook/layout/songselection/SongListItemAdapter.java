package igrek.songbook.layout.songselection;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import igrek.songbook.R;

public class SongListItemAdapter extends ArrayAdapter<SongTreeItem> {
	
	private Context context;
	private List<SongTreeItem> dataSource;
	
	public SongListItemAdapter(Context context, List<SongTreeItem> dataSource, SongListView listView) {
		super(context, 0, new ArrayList<>());
		this.context = context;
		if (dataSource == null)
			dataSource = new ArrayList<>();
		this.dataSource = dataSource;
	}
	
	public void setDataSource(List<SongTreeItem> dataSource) {
		this.dataSource = dataSource;
		notifyDataSetChanged();
	}
	
	@Override
	public SongTreeItem getItem(int position) {
		return dataSource.get(position);
	}
	
	@Override
	public int getCount() {
		return dataSource.size();
	}
	
	@Override
	public long getItemId(int position) {
		if (position < 0)
			return -1;
		if (position >= dataSource.size())
			return -1;
		return (long) position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View itemView = inflater.inflate(R.layout.song_list_item, parent, false);
		SongTreeItem item = dataSource.get(position);
		
		// set item title
		TextView itemContentLabel = itemView.findViewById(R.id.itemContentLabel);
		if (item.isCategory()) {
			itemContentLabel.setTypeface(null, Typeface.BOLD);
		} else {
			itemContentLabel.setTypeface(null, Typeface.NORMAL);
		}
		if (item instanceof SongSearchItem) {
			// search item: title - category
			String songTitle = item.getSong().getTitle();
			String categoryName = item.getSong().getCategory().getName();
			String displayName;
			if (categoryName != null) {
				displayName = songTitle + " - " + categoryName;
			} else {
				displayName = songTitle;
			}
			itemContentLabel.setText(displayName);
		} else {
			itemContentLabel.setText(item.getSimpleName());
		}
		
		return itemView;
	}
	
}