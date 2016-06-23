package igrek.songbook.settings;

import android.view.WindowManager;

public class Config {
    //STAŁE
    //  OUTPUT
    public static class Output {
        public static final String logTag = "ylog";
        public static final int echo_showtime = 1800; //[ms]
        public static final boolean show_exceptions_trace = true;
    }
    //  SCREEN
    public static class Screen {
        public static final int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        public static final boolean fullscreen = false;
        public static final boolean hide_taskbar = true;
        public static final boolean keep_screen_on = true;
    }
    //  USTAWIENIA UŻYTKOWNIKA
    public static final String shared_preferences_name = "ToDoTreeUserPreferences";
    //  BACKUP
    public static final String backup_file_prefix = "backup_";
    public static final int backup_num = 10;
    //Zestaw ustawień: 0 - samsung galaxy grand prime (DEFAULT), 1 - emulator Android
    public static final int config_set = 0;
    //nadpisanie parametrów dla innego zestawu
    public Config() {
        if (config_set == 1) {

        }
    }
}
