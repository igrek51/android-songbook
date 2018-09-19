package igrek.songbook.layout.view;

import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageButton;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiResourceService;

public class ButtonClickEffect implements View.OnTouchListener {
	
	@Inject
	UiResourceService uiResourceService;
	private int downColor;
	
	private ButtonClickEffect() {
		DaggerIoc.getFactoryComponent().inject(this);
		downColor = uiResourceService.getColor(R.color.buttonDownColor);
	}
	
	
	public static void addClickEffect(ImageButton imageButton) {
		imageButton.setOnTouchListener(new ButtonClickEffect());
		expandTouchArea(imageButton, 0.3f);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				v.getBackground().setColorFilter(downColor, PorterDuff.Mode.SRC_ATOP);
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
	
	private static void expandTouchArea(View component, float increasePart) {
		final View parent = (View) component.getParent();
		parent.post(() -> {
			final Rect rect = new Rect();
			component.getHitRect(rect);
			int sidePxW = (int) (rect.width() * increasePart);
			int sidePxH = (int) (rect.height() * increasePart);
			rect.top -= sidePxH;    // increase top hit area
			rect.left -= sidePxW;   // increase left hit area
			rect.bottom += sidePxH; // increase bottom hit area
			rect.right += sidePxW;  // increase right hit area
			parent.setTouchDelegate(new TouchDelegate(rect, component));
		});
	}
}
