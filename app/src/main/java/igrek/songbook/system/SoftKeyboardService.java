package igrek.songbook.system;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;

public class SoftKeyboardService {
	
	@Inject
	AppCompatActivity activity;
	
	private InputMethodManager imm;
	
	public SoftKeyboardService() {
		DaggerIoc.getFactoryComponent().inject(this);
		imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	public void hideSoftKeyboard(View view) {
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
	
	public void hideSoftKeyboard() {
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) {
			view = new View(activity);
		}
		hideSoftKeyboard(view);
	}
	
	public void showSoftKeyboard(View view) {
		if (imm != null) {
			imm.showSoftInput(view, 0);
		}
	}
}
