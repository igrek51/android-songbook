package igrek.songbook.settings.preferences;

import android.app.Activity;

import igrek.songbook.system.output.Output;

public class Preferences extends BasePreferences {

    public String startPath = "/storage/extSdCard/Gitara";

    public Preferences(Activity activity){
        super(activity);
    }

    public void preferencesSave() {
        //TODO utworzenie folderu jeśli nie istnieje
        setString("startPath", startPath);
    }

    public void preferencesLoad() {
        if (exists("startPath")) {
            startPath = getString("startPath");
            Output.log("Wczytano początkową ścieżkę: " + startPath);
        } else {
            Output.log("Wczytano domyślną początkową ścieżkę: " + startPath);
        }
    }
}
