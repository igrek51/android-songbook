package igrek.songbook.songpreview.renderer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class OverlayRecyclerAdapter extends RecyclerView.Adapter<OverlayRecyclerAdapter.OverlayViewHolder> {
	
	private SongPreview songPreview;
	private View overlayView;
	
	public OverlayRecyclerAdapter(SongPreview songPreview) {
		this.songPreview = songPreview;
	}
	
	@Override
	public OverlayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Scrolling in Android is so fukced up!!
		// The only workaround seems to be make the almost indefinite views and put the scroll somewhere in the middle
		int height = Integer.MAX_VALUE / getItemCount();
		
		overlayView = new View(parent.getContext());
		overlayView.setLayoutParams(new ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height));
		overlayView.setMinimumHeight(height);
		overlayView.setOnClickListener((v) -> songPreview.onClick());
		overlayView.setOnTouchListener(songPreview);
		
		return new OverlayViewHolder(overlayView);
	}
	
	@Override
	public void onBindViewHolder(OverlayViewHolder holder, int position) {
	}
	
	@Override
	public int getItemCount() {
		return 3;
	}
	
	static class OverlayViewHolder extends RecyclerView.ViewHolder {
		OverlayViewHolder(View v) {
			super(v);
		}
	}
}