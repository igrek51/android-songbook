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
import igrek.songbook.layout.songtree.SongTreeItem;

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
		
		final View itemView = inflater.inflate(R.layout.song_list_item, parent, false);
		final SongTreeItem item = dataSource.get(position);
		
		//zawartość tekstowa elementu
		TextView textView = itemView.findViewById(R.id.tvItemContent);
		if (item.isCategory()) {
			textView.setTypeface(null, Typeface.BOLD);
		} else {
			textView.setTypeface(null, Typeface.NORMAL);
		}
		textView.setText(item.getSimpleName());
		
		return itemView;
	}
	
}