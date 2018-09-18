package igrek.songbook.layout.view;

import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class ButtonClickEffect implements View.OnTouchListener {
	
	private ButtonClickEffect() {
	}
	
	public static void addClickEffect(ImageButton imageButton) {
		imageButton.setOnTouchListener(new ButtonClickEffect());
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
				v.invalidate();
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				v.getBackground().clearColorFilter();
				v.invalidate();
				break;
			}
		}
		return false;
	}
}
