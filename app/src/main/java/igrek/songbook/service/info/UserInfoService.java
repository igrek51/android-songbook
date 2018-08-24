package igrek.songbook.service.info;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.ui.errorcheck.SafeClickListener;

public class UserInfoService {
	
	@Inject
	Activity activity;
	
	private HashMap<View, Snackbar> infobars = new HashMap<>();
	
	private Logger logger = LoggerFactory.getLogger();
	
	public UserInfoService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	/**
	 * @param info       tekst do wyświetlenia lub zmiany
	 * @param view       widok, na którym ma zostać wyświetlony tekst
	 * @param actionName tekst przycisku akcji (jeśli null - brak przycisku akcji)
	 * @param action     akcja kliknięcia przycisku (jeśli null - schowanie wyświetlanego tekstu)
	 */
	private void showActionInfo(String info, View view, String actionName, InfoBarClickAction action, Integer color) {
		
		if (view == null) {
			view = activity.findViewById(android.R.id.content);
		}
		
		Snackbar snackbar = infobars.get(view);
		if (snackbar == null || !snackbar.isShown()) { //nowy
			snackbar = Snackbar.make(view, info, Snackbar.LENGTH_SHORT);
			snackbar.setActionTextColor(Color.WHITE);
		} else { //widoczny - użyty kolejny raz
			snackbar.setText(info);
		}
		
		if (actionName != null) {
			if (action == null) {
				final Snackbar finalSnackbar = snackbar;
				action = finalSnackbar::dismiss;
			}
			
			final InfoBarClickAction finalAction = action;
			snackbar.setAction(actionName, new SafeClickListener() {
				@Override
				public void onClick() {
					finalAction.onClick();
				}
			});
			if (color != null) {
				snackbar.setActionTextColor(color);
			}
		}
		
		snackbar.show();
		infobars.put(view, snackbar);
		logger.info(info);
	}
	
	
	public void showInfo(String info) {
		showActionInfo(info, null, "OK", null, null);
	}
	
	public void showInfoCancellable(String info, InfoBarClickAction cancelCallback) {
		showActionInfo(info, null, "Undo", cancelCallback, ContextCompat.getColor(activity, R.color.colorPrimary));
	}
	
	public void showToast(String message) {
		Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
		logger.info("TOAST: " + message);
	}
}
