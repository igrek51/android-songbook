package igrek.songbook.layout.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import igrek.songbook.domain.chords.ChordsNotation;
import igrek.songbook.info.UiResourceService;

public class ChordsNotationAdapter extends ArrayAdapter<ChordsNotation> {
	
	private Context context;
	private ChordsNotation[] dataSource;
	
	UiResourceService uiResourceService;
	
	public ChordsNotationAdapter(Context context, ChordsNotation[] dataSource, UiResourceService uiResourceService) {
		super(context, 0, dataSource);
		this.context = context;
		this.dataSource = dataSource;
		this.uiResourceService = uiResourceService;
	}
	
	public void setDataSource(ChordsNotation[] dataSource) {
		this.dataSource = dataSource;
		notifyDataSetChanged();
	}
	
	@Override
	public ChordsNotation getItem(int position) {
		return dataSource[position];
	}
	
	@Override
	public int getCount() {
		return dataSource.length;
	}
	
	@Override
	public long getItemId(int position) {
		if (position < 0 || position >= dataSource.length)
			return -1;
		return (long) position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ChordsNotation item = dataSource[position];
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View itemView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
		TextView text1 = itemView.findViewById(android.R.id.text1);
		// set item title
		String displayName = uiResourceService.resString(item.getDisplayNameResId());
		text1.setText(displayName);
		
		return itemView;
	}
	
}