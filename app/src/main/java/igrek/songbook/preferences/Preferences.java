package igrek.songbook.preferences;

import android.app.Activity;

import igrek.songbook.output.Output;

public class Preferences extends BasePreferences {

    //TODO zapis szybkości scrolla, rozmiaru czcionki, w preferences

    public String startPath = "/storage/extSdCard/Gitara";

    public Preferences(Activity activity){
        super(activity);
    }

    public void preferencesSave() {
        setString("startPath", startPath);
    }

    public void preferencesLoad() {
        if (exists("startPath")) {
            startPath = getString("startPath");
            Output.info("Wczytano początkową ścieżkę: " + startPath);
        } else {
            Output.info("Wczytano domyślną początkową ścieżkę: " + startPath);
        }
    }
}
