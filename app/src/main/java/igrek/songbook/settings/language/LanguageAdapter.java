package igrek.songbook.settings.language;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import igrek.songbook.info.UiResourceService;

public class LanguageAdapter extends ArrayAdapter<AppLanguage> {
	
	private LayoutInflater inflater;
	private UiResourceService uiResourceService;
	
	public LanguageAdapter(Context context, AppLanguage[] dataSource, UiResourceService uiResourceService) {
		super(context, 0, dataSource);
		this.uiResourceService = uiResourceService;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createItemTextView(position, parent, android.R.layout.simple_spinner_item, android.R.id.text1);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createItemTextView(position, parent, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
	}
	
	private View createItemTextView(int position, ViewGroup parent, @LayoutRes int layoutResource, @IdRes int textViewId) {
		AppLanguage item = getItem(position);
		
		View itemView = inflater.inflate(layoutResource, parent, false);
		TextView text1 = itemView.findViewById(textViewId);
		
		// set item title
		String displayName = uiResourceService.resString(item.getDisplayNameResId());
		text1.setText(displayName);
		
		return itemView;
	}
	
}