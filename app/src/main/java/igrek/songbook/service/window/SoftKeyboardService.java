package igrek.songbook.service.window;

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
	
	public void hideSoftKeyboard(View window) {
		if (imm != null) {
			imm.hideSoftInputFromWindow(window.getWindowToken(), 0);
		}
	}
	
	public void showSoftKeyboard(View window) {
		if (imm != null) {
			imm.showSoftInput(window, 0);
		}
	}
}
