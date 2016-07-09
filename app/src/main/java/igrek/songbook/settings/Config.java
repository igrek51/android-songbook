package igrek.songbook.settings;

import android.view.WindowManager;

public class Config {
    //STAŁE
    //TODO przenieść ustawienia do odpowiedzialnych klas
    //  OUTPUT
    public static class Output {
        public static final String logTag = "ylog";
        public static final boolean show_exceptions_trace = true;
    }
    //  SCREEN
    public static class Screen {
        public static final int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        public static final boolean fullscreen = false;
        public static final boolean hide_taskbar = true;
        public static final boolean keep_screen_on = true;
    }
    //  CZCIONKI
    public static class Fonts {
        public static final int fontsize = 20;
        public static final int lineheight = 21;
    }
    //  KOLORY
    public static class Colors {
        public static final int background = 0x000000;
    }
    //  USTAWIENIA UŻYTKOWNIKA
    public static final String shared_preferences_name = "SongBookUserPreferences";
}
