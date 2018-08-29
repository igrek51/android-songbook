package igrek.songbook.view.songselection;

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
import igrek.songbook.service.filetree.FileItem;

public class FileItemAdapter extends ArrayAdapter<FileItem> {
	
	Context context;
	List<FileItem> dataSource;
	
	View convertView = null;
	ViewGroup parent = null;
	
	public FileItemAdapter(Context context, List<FileItem> dataSource, FileListView listView) {
		super(context, 0, new ArrayList<FileItem>());
		this.context = context;
		if (dataSource == null)
			dataSource = new ArrayList<>();
		this.dataSource = dataSource;
	}
	
	public void setDataSource(List<FileItem> dataSource) {
		this.dataSource = dataSource;
		notifyDataSetChanged();
	}
	
	public FileItem getItem(int position) {
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
		
		this.convertView = convertView;
		this.parent = parent;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View itemView = inflater.inflate(R.layout.file_item, parent, false);
		final FileItem item = dataSource.get(position);
		
		//zawartość tekstowa elementu
		TextView textView = itemView.findViewById(R.id.tvItemContent);
		if (item.isDirectory()) {
			textView.setTypeface(null, Typeface.BOLD);
		} else {
			textView.setTypeface(null, Typeface.NORMAL);
		}
		textView.setText(item.getName());
		
		return itemView;
	}
	
}