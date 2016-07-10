package igrek.songbook.settings;

import android.view.WindowManager;

public class Config {
    //STAŁE
    //TODO przenieść ustawienia do odpowiedzialnych klas

    //  SCREEN
    public static class Screen {
        public static final int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        public static final boolean fullscreen = false;
        public static final boolean hide_taskbar = true;
        public static final boolean keep_screen_on = true;
    }
}
