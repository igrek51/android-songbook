package igrek.songbook.layout.songselection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.layout.edit.SongEditService;

public class SongListItemAdapter extends ArrayAdapter<SongTreeItem> {
	
	private List<SongTreeItem> dataSource;
	private LayoutInflater inflater;
	@Inject
	SongEditService songEditService;
	
	public SongListItemAdapter(Context context, List<SongTreeItem> dataSource, SongListView listView) {
		super(context, 0, new ArrayList<>());
		if (dataSource == null)
			dataSource = new ArrayList<>();
		this.dataSource = dataSource;
		DaggerIoc.getFactoryComponent().inject(this);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		
		if (item instanceof SongSearchItem) {
			return createSearchSongView((SongSearchItem) item, parent);
		}
		if (item.isCategory()) {
			return createTreeCategoryView(item, parent);
		} else {
			if (item.getSong().getCustom()) {
				return createTreeCustomSongView(item, parent);
			} else {
				return createTreeSongView(item, parent);
			}
		}
	}
	
	private View createTreeCategoryView(SongTreeItem item, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.song_tree_category, parent, false);
		TextView itemCategoryNameLabel = itemView.findViewById(R.id.itemCategoryNameLabel);
		// set item title
		itemCategoryNameLabel.setText(item.getSimpleName());
		
		return itemView;
	}
	
	private View createTreeSongView(SongTreeItem item, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.song_tree_song, parent, false);
		TextView itemSongTitleLabel = itemView.findViewById(R.id.itemSongTitleLabel);
		// set item title
		itemSongTitleLabel.setText(item.getSimpleName());
		
		return itemView;
	}
	
	private View createSearchSongView(SongSearchItem item, ViewGroup parent) {
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
	
	private View createTreeCustomSongView(SongTreeItem item, ViewGroup parent) {
		View itemView = inflater.inflate(R.layout.song_tree_custom, parent, false);
		TextView itemSongTitleLabel = itemView.findViewById(R.id.itemSongTitleLabel);
		// set item title
		itemSongTitleLabel.setText(item.getSimpleName());
		// edit button
		ImageButton itemSongEditButton = itemView.findViewById(R.id.itemSongEditButton);
		itemSongEditButton.setOnClickListener(v -> songEditService.showEditSongScreen(item.getSong()));
		
		return itemView;
	}
}