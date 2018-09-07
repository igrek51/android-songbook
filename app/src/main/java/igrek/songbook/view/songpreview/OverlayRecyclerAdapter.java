package igrek.songbook.view.songpreview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class OverlayRecyclerAdapter extends RecyclerView.Adapter<OverlayRecyclerAdapter.OverlayViewHolder> {
	
	private SongPreview canvas;
	private View emptyView;
	
	public OverlayRecyclerAdapter(SongPreview canvas) {
		this.canvas = canvas;
	}
	
	@Override
	public OverlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		emptyView = new View(parent.getContext());
		
		int height = getContentHeight();
		if (height < parent.getMeasuredHeight())
			height = parent.getMeasuredHeight();
		
		emptyView.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height));
		emptyView.setMinimumHeight(height);
		emptyView.setOnClickListener((v) -> canvas.onClick());
		
		return new OverlayViewHolder(emptyView);
	}
	
	public int getContentHeight() {
		int maxContentHeight = canvas.getMaxContentHeight();
		// add some reserve
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