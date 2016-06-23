package igrek.songbook.settings.preferences;

import android.app.Activity;

import igrek.songbook.system.output.Output;

public class Preferences extends BasePreferences {

    //TODO: wczytywanie początkowego folderu
    public String dbFilePath = "Android/data/igrek.songbook/todo.dat";

    public Preferences(Activity activity){
        super(activity);
    }

    public void preferencesSave() {

        //TODO utworzenie folderu jeśli nie istnieje

        setString("dbFilePath", dbFilePath);
    }

    public void preferencesLoad() {
        if (exists("dbFilePath")) {
            dbFilePath = getString("dbFilePath");
            Output.log("Wczytano ścieżkę do pliku bazy: " + dbFilePath);
        } else {
            Output.log("Wczytano domyślną ścieżkę do pliku bazy: " + dbFilePath);
        }
    }
}
