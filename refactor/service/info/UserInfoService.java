package igrek.songbook.service.info;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.HashMap;

import igrek.todotree.R;
import igrek.todotree.logger.Logger;
import igrek.todotree.ui.GUI;
import igrek.todotree.ui.errorcheck.SafeClickListener;

public class UserInfoService {
	
	private Activity activity;
	private GUI gui;
	
	private View mainView = null;
	
	private HashMap<View, Snackbar> infobars = new HashMap<>();
	
	protected Logger logger;
	
	public UserInfoService(Activity activity, GUI gui, Logger logger) {
		this.activity = activity;
		this.gui = gui;
		this.logger = logger;
	}
	
	private String resString(int resourceId) {
		return activity.getResources().getString(resourceId);
	}
	
	/**
	 * @param info       tekst do wyświetlenia lub zmiany
	 * @param view       widok, na którym ma zostać wyświetlony tekst
	 * @param actionName tekst przycisku akcji (jeśli null - brak przycisku akcji)
	 * @param action     akcja kliknięcia przycisku (jeśli null - schowanie wyświetlanego tekstu)
	 */
	private void showActionInfo(String info, View view, String actionName, InfoBarClickAction action, Integer color) {
		
		if (view == null) {
			view = mainView;
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
				action = new InfoBarClickAction() {
					@Override
					public void onClick() {
						finalSnackbar.dismiss();
					}
				};
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
		showActionInfo(info, gui.getMainContent(), "OK", null, null);
	}
	
	public void showInfoCancellable(String info, InfoBarClickAction cancelCallback) {
		showActionInfo(info, gui.getMainContent(), "Undo", cancelCallback, ContextCompat.getColor(activity, R.color.colorPrimary));
	}
}
