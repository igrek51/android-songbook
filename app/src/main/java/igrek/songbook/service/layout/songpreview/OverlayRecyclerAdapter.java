package igrek.songbook.service.layout.songpreview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import igrek.songbook.view.songpreview.CanvasGraphics;

public class OverlayRecyclerAdapter extends RecyclerView.Adapter<OverlayRecyclerAdapter.OverlayViewHolder> {
	
	private CanvasGraphics canvas;
	private View emptyView;
	
	public OverlayRecyclerAdapter(CanvasGraphics canvas) {
		this.canvas = canvas;
	}
	
	@Override
	public OverlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		emptyView = new View(parent.getContext());
		
		int height = canvas.getMaxContentHeight();
		if (height < parent.getMeasuredHeight())
			height = parent.getMeasuredHeight();
		
		emptyView.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height));
		emptyView.setMinimumHeight(height);
		emptyView.setOnClickListener((v) -> canvas.onClick());
		
		return new OverlayViewHolder(emptyView);
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