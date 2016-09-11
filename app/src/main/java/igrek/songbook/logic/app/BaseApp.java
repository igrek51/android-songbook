package igrek.songbook.logic.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.HashMap;

import igrek.songbook.graphics.infobar.InfoBarClickAction;
import igrek.songbook.logger.Logs;

public abstract class BaseApp {

    public static final int FULLSCREEN_FLAG = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
    public static final boolean FULLSCREEN = false;
    public static final boolean HIDE_TASKBAR = true;
    public static final boolean KEEP_SCREEN_ON = true;

    public AppCompatActivity activity;
    private Thread.UncaughtExceptionHandler defaultUEH;
    protected Menu menu;

    protected HashMap<View, Snackbar> infobars = new HashMap<>();

    boolean running = true;

    public BaseApp(AppCompatActivity aActivity) {
        this.activity = aActivity;

        //łapanie niezłapanych wyjątków
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable th) {
                Logs.errorUncaught(th);
                //przekazanie dalej do systemu operacyjnego
                defaultUEH.uncaughtException(thread, th);
            }
        });

        //schowanie paska tytułu
        if (HIDE_TASKBAR) {
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
        }
        //fullscreen
        if (FULLSCREEN) {
            activity.getWindow().setFlags(FULLSCREEN_FLAG, FULLSCREEN_FLAG);
        }

        new Logs();

        // activity.setContentView(graphics);

        Logs.debug("Inicjalizacja aplikacji...");
    }

    public void pause() {

    }

    public void resume() {

    }

    public void quit() {
        if (!running) { //próba ponownego zamknięcia
            Logs.warn("Zamykanie - próba ponownego zamknięcia");
            return;
        }
        Logs.info("Zamykanie aplikacji...");
        running = false;
        keepScreenOff(activity);
        activity.finish();
    }


    public void onResizeEvent(Configuration newConfig) {
        int screenWidthDp = newConfig.screenWidthDp;
        int screenHeightDp = newConfig.screenHeightDp;
        int orientation = newConfig.orientation;
        int densityDpi = newConfig.densityDpi;
        Logs.debug("Rozmiar ekranu zmieniony na: " + screenWidthDp + "dp x " + screenHeightDp + "dp (DPI = " + densityDpi + ")");
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Logs.debug("Zmiana orientacji ekranu: landscape");
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Logs.debug("Zmiana orientacji ekranu: portrait");
        }
    }

    public void menuInit(Menu menu){
        this.menu = menu;
    }

    public void setMenuItemVisible(int id, boolean visibility){
        MenuItem item = menu.findItem(id);
        if(item != null) {
            item.setVisible(visibility);
        }
    }

    public boolean onKeyBack() {
        quit();
        return true;
    }

    public boolean onKeyMenu() {
        return false;
    }

    public boolean optionsSelect(int id) {
        return false;
    }

    public void minimize() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(startMain);
    }

    /**
     * @param info tekst do wyświetlenia lub zmiany
     * @param view widok, na którym ma zostać wyświetlony tekst
     * @param actionName tekst przycisku akcji (jeśli null - brak przycisku akcji)
     * @param action akcja kliknięcia przycisku (jeśli null - schowanie wyświetlanego tekstu)
     */
    public void showActionInfo(String info, View view, String actionName, InfoBarClickAction action) {

        if (view == null) {
            view = getActiveView();
        }

        Snackbar snackbar = infobars.get(view);
        if (snackbar == null) { //nowy
            snackbar = Snackbar.make(view, info, Snackbar.LENGTH_SHORT);
            snackbar.setActionTextColor(Color.WHITE);
        } else { //użyty kolejny raz
            snackbar.setText(info);
        }

        if(actionName != null){
            if(action == null){
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

    public void hideInfo(View view){
        final Snackbar snackbar = infobars.get(view);
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    protected void keepScreenOn(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void keepScreenOff(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected abstract View getActiveView();
}
