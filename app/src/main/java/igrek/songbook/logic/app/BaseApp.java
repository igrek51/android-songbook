package igrek.songbook.logic.app;

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

import igrek.songbook.filesystem.Files;
import igrek.songbook.graphics.infobar.InfoBarClickAction;
import igrek.songbook.logic.touchcontroller.ITouchController;
import igrek.songbook.output.Output;
import igrek.songbook.preferences.Preferences;

public abstract class BaseApp implements ITouchController {

    public static final int FULLSCREEN_FLAG = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
    public static final boolean FULLSCREEN = false;
    public static final boolean HIDE_TASKBAR = true;
    public static final boolean KEEP_SCREEN_ON = true;

    public AppCompatActivity activity;
    private Thread.UncaughtExceptionHandler defaultUEH;
    protected Menu menu;

    protected HashMap<View, Snackbar> infobars = new HashMap<>();

    boolean running = true;

    public Files files;
    public Preferences preferences;

    public BaseApp(AppCompatActivity aActivity) {
        this.activity = aActivity;

        //łapanie niezłapanych wyjątków
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable th) {
                Output.errorUncaught(th);
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
        if (KEEP_SCREEN_ON) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        new Output();
        files = new Files(activity);
        preferences = new Preferences(activity);

        //        activity.setContentView(graphics);

        Output.info("Inicjalizacja aplikacji...");
    }

    public void pause() {

    }

    public void resume() {

    }

    public void quit() {
        if (!running) { //próba ponownego zamknięcia
            Output.warn("Zamykanie - próba ponownego zamknięcia");
            return;
        }
        Output.info("Zamykanie aplikacji...");
        running = false;
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity.finish();
    }

    @Override
    public boolean onTouchDown(float x, float y) {
        return false;
    }

    @Override
    public boolean onTouchMove(float x, float y) {
        return false;
    }

    @Override
    public boolean onTouchUp(float x, float y) {
        return false;
    }

    public void onResizeEvent(Configuration newConfig) {
        int screenWidthDp = newConfig.screenWidthDp;
        int screenHeightDp = newConfig.screenHeightDp;
        int orientation = newConfig.orientation;
        int densityDpi = newConfig.densityDpi;
        Output.info("Rozmiar ekranu zmieniony na: " + screenWidthDp + "dp x " + screenHeightDp + "dp (DPI = " + densityDpi + ")");
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Output.info("Zmiana orientacji ekranu: landscape");
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Output.info("Zmiana orientacji ekranu: portrait");
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
    public void showReusableActionInfo(String info, View view, String actionName, InfoBarClickAction action){
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
        Output.info(info);
    }

    public void hideInfo(View view){
        final Snackbar snackbar = infobars.get(view);
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }
}
