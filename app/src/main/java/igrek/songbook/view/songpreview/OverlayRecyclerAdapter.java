package igrek.songbook.view.songpreview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class OverlayRecyclerAdapter extends RecyclerView.Adapter<OverlayRecyclerAdapter.OverlayViewHolder> {
	
	private SongPreview songPreview;
	private View overlayView;
	
	public OverlayRecyclerAdapter(SongPreview songPreview) {
		this.songPreview = songPreview;
	}
	
	@Override
	public OverlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		int height = getContentHeight();
		if (height < parent.getMeasuredHeight())
			height = parent.getMeasuredHeight();
		
		overlayView = new View(parent.getContext());
		overlayView.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height));
		overlayView.setMinimumHeight(height);
		overlayView.setOnClickListener((v) -> songPreview.onClick());
		overlayView.setOnTouchListener(songPreview);
		
		return new OverlayViewHolder(overlayView);
	}
	
	private int getContentHeight() {
		int maxContentHeight = songPreview.getMaxContentHeight();
		// add some reserve (2 %)
		return maxContentHeight + maxContentHeight / 50;
	}
	
	@Override
	public void onBindViewHolder(OverlayViewHolder holder, int position) {
	}
	
	@Override
	public int getItemCount() {
		return 1;
	}
	
	static class OverlayViewHolder extends RecyclerView.ViewHolder {
		OverlayViewHolder(View v) {
			super(v);
		}
	}
}