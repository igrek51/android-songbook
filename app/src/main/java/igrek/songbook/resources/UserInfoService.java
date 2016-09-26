package igrek.songbook.resources;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.HashMap;

import igrek.songbook.graphics.infobar.InfoBarClickAction;
import igrek.songbook.logger.Logs;
import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.services.IService;

public class UserInfoService implements IService {

    private Activity activity;
    private View mainView = null;

    private HashMap<View, Snackbar> infobars = new HashMap<>();

    public UserInfoService(Activity activity) {
        this.activity = activity;
        AppController.registerService(this);
    }

    public String resString(int resourceId) {
        return activity.getResources().getString(resourceId);
    }

    public void setMainView(View mainView) {
        this.mainView = mainView;
    }

    /**
     * @param info       tekst do wyświetlenia lub zmiany
     * @param view       widok, na którym ma zostać wyświetlony tekst
     * @param actionName tekst przycisku akcji (jeśli null - brak przycisku akcji)
     * @param action     akcja kliknięcia przycisku (jeśli null - schowanie wyświetlanego tekstu)
     */
    public void showActionInfo(String info, View view, String actionName, InfoBarClickAction action) {

        if (view == null) {
            view = mainView;
        }

        Snackbar snackbar = infobars.get(view);
        if (snackbar == null) { //nowy
            snackbar = Snackbar.make(view, info, Snackbar.LENGTH_SHORT);
            snackbar.setActionTextColor(Color.WHITE);
        } else { //użyty kolejny raz
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
            snackbar.setAction(actionName, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalAction.onClick();
                }
            });
        }

        snackbar.show();
        infobars.put(view, snackbar);
        Logs.info(info);
    }

    public void showActionInfo(int resourceId, View view, String actionName, InfoBarClickAction action) {
        showActionInfo(resString(resourceId), view, actionName, action);
    }

    public void hideInfo(View view) {
        final Snackbar snackbar = infobars.get(view);
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }
}
