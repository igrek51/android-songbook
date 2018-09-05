package igrek.songbook.service.system;

import android.content.Context;
import android.os.IBinder;
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
	
	public void hideSoftKeyboard() {
		IBinder window = activity.getWindow().getDecorView().getRootView().getWindowToken();
		if (imm != null) {
			imm.hideSoftInputFromWindow(window, 0);
		}
	}
	
	public void showSoftKeyboard() {
		View window = activity.getWindow().getDecorView().getRootView();
		if (imm != null) {
			imm.showSoftInput(window, 0);
		}
	}
}
