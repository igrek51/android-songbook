package igrek.songbook.layout.songselection;

import android.content.Context;
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
		SongTreeItem item = dataSource.get(position);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (item instanceof SongSearchItem) {
			return createSearchSongView((SongSearchItem) item, inflater, parent);
		}
		if (item.isCategory()) {
			return createTreeCategoryView(item, inflater, parent);
		} else {
			return createTreeSongView(item, inflater, parent);
		}
	}
	
	private View createTreeCategoryView(SongTreeItem item, LayoutInflater inflater, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.song_tree_category, parent, false);
		TextView itemCategoryNameLabel = itemView.findViewById(R.id.itemCategoryNameLabel);
		// set item title
		itemCategoryNameLabel.setText(item.getSimpleName());
		
		return itemView;
	}
	
	private View createTreeSongView(SongTreeItem item, LayoutInflater inflater, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.song_tree_song, parent, false);
		TextView itemSongTitleLabel = itemView.findViewById(R.id.itemSongTitleLabel);
		// set item title
		itemSongTitleLabel.setText(item.getSimpleName());
		
		return itemView;
	}
	
	private View createSearchSongView(SongSearchItem item, LayoutInflater inflater, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.song_search_song, parent, false);
		TextView itemSongLabel = itemView.findViewById(R.id.itemSongLabel);
		// search item: title - category
		String songTitle = item.getSong().getTitle();
		String categoryName = item.getSong().getCategory().getName();
		String displayName;
		if (categoryName != null) {
			displayName = songTitle + " - " + categoryName;
		} else {
			displayName = songTitle;
		}
		itemSongLabel.setText(displayName);
		
		return itemView;
	}
	
}