package igrek.songbook.service.info;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.errorcheck.SafeClickListener;

public class UserInfoService {
	
	@Inject
	Activity activity;
	
	@Inject
	UIResourceService uiResourceService;
	
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
	
	
	public void showInfo(String info, String dismissName) {
		showActionInfo(info, null, dismissName, null, null);
	}
	
	public void showInfo(String info) {
		String dismissName = uiResourceService.resString(R.string.action_info_ok);
		showInfo(info, dismissName);
	}
	
	public void showInfo(int infoRes, int dismissNameRes) {
		String info = uiResourceService.resString(infoRes);
		String dismissName = uiResourceService.resString(dismissNameRes);
		showInfo(info, dismissName);
	}
	
	public void showInfo(int infoRes) {
		String info = uiResourceService.resString(infoRes);
		showInfo(info);
	}
	
	public void showInfoCancellable(String info, InfoBarClickAction cancelCallback) {
		showActionInfo(info, null, "Undo", cancelCallback, ContextCompat.getColor(activity, R.color.colorPrimary));
	}
	
	public void showToast(String message) {
		Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
		logger.info("TOAST: " + message);
	}
	
	public void showDialog(String title, String message) {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
		dlgAlert.setMessage(message);
		dlgAlert.setTitle(title);
		dlgAlert.setPositiveButton(uiResourceService.resString(R.string.action_info_ok), (dialog, which) -> {
		});
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}
}
