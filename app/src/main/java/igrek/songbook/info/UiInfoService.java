package igrek.songbook.info;

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
import igrek.songbook.info.errorcheck.SafeClickListener;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class UiInfoService {
	
	@Inject
	Activity activity;
	
	@Inject
	UiResourceService uiResourceService;
	
	private HashMap<View, Snackbar> infobars = new HashMap<>();
	
	private Logger logger = LoggerFactory.getLogger();
	
	public UiInfoService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	/**
	 * @param info       text to show or replace
	 * @param view       view, on which the text should be displayed
	 * @param actionName action button text value (if null - no action button)
	 * @param action     action perforfmed on button click (if null - dismiss displayed snackbar)
	 */
	private void showActionInfo(String info, View view, String actionName, InfoBarClickAction action, Integer color, int snackbarLength) {
		
		if (view == null) {
			view = activity.findViewById(android.R.id.content);
		}
		
		// dont create new snackbars if one is already shown
		Snackbar snackbar = infobars.get(view);
		if (snackbar == null || !snackbar.isShown()) { // a new one
			snackbar = Snackbar.make(view, info, snackbarLength);
			snackbar.setActionTextColor(Color.WHITE);
		} else { // visible - use it one more time
			snackbar.setDuration(snackbarLength);
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
	}
	
	public void showInfo(String info, String dismissName) {
		showActionInfo(info, null, dismissName, null, null, Snackbar.LENGTH_LONG);
	}
	
	public void showInfo(String info) {
		String dismissName = uiResourceService.resString(R.string.action_info_ok);
		showInfo(info, dismissName);
	}
	
	public void showInfo(int infoRes) {
		String info = uiResourceService.resString(infoRes);
		showInfo(info);
	}
	
	public void showInfoIndefinite(String info) {
		String dismissName = uiResourceService.resString(R.string.action_info_ok);
		showActionInfo(info, null, dismissName, null, null, Snackbar.LENGTH_INDEFINITE);
	}
	
	public void showInfoIndefinite(int infoRes) {
		String info = uiResourceService.resString(infoRes);
		showInfoIndefinite(info);
	}
	
	public void showInfoWithAction(String info, String actionName, InfoBarClickAction actionCallback) {
		int color = ContextCompat.getColor(activity, R.color.colorAccent);
		showActionInfo(info, null, actionName, actionCallback, color, Snackbar.LENGTH_LONG);
	}
	
	public void showInfoWithAction(String info, int actionNameRes, InfoBarClickAction actionCallback) {
		String actionName = uiResourceService.resString(actionNameRes);
		showInfoWithAction(info, actionName, actionCallback);
	}
	
	public void showInfoWithAction(int infoRes, int actionNameRes, InfoBarClickAction actionCallback) {
		String info = uiResourceService.resString(infoRes);
		String actionName = uiResourceService.resString(actionNameRes);
		showInfoWithAction(info, actionName, actionCallback);
	}
	
	public void showToast(String message) {
		Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
	
	public void showToast(int messageRes) {
		String message = uiResourceService.resString(messageRes);
		showToast(message);
	}
	
	public void showDialog(String title, String message) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
		alertBuilder.setMessage(message);
		alertBuilder.setTitle(title);
		alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok), (dialog, which) -> {
		});
		alertBuilder.setCancelable(true);
		alertBuilder.create().show();
	}
	
}
