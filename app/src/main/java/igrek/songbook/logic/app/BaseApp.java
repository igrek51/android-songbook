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

import igrek.songbook.filesystem.Files;
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

        Output.debug("Inicjalizacja aplikacji...");
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
        Output.debug("Zamykanie aplikacji...");
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
        Output.debug("Rozmiar ekranu zmieniony na: " + screenWidthDp + "dp x " + screenHeightDp + "dp (DPI = " + densityDpi + ")");
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Output.debug("Zmiana orientacji ekranu: landscape");
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Output.debug("Zmiana orientacji ekranu: portrait");
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

    public void showInfo(String info, View view) {
        final Snackbar snackbar = Snackbar.make(view, info, Snackbar.LENGTH_SHORT);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.show();
        Output.info(info);
    }
}
