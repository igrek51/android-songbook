package igrek.songbook.preferences;

import android.app.Activity;

import igrek.songbook.output.Output;

public class Preferences extends BasePreferences {

    //TODO zapis szybkości scrolla, rozmiaru czcionki, w preferences

    //TODO oznaczenie pól anotacjami, zapis i odczyt przez mechanizm reflekcji

    public String startPath = "/storage/extSdCard/Gitara";
    private final String START_PATH = "startPath";

    public float fontsize = 23.0f;
    private final String FONTSIZE = "fontsize";

    public float autoscrollInterval = 300.0f;
    private final String AUTOSCROLL_INTERVAL = "autoscrollInterval";

    public Preferences(Activity activity){
        super(activity);
    }

    public void preferencesSave() {
        setString(START_PATH, startPath);
    }

    public void preferencesLoad() {
        if (exists(START_PATH)) {
            startPath = getString(START_PATH);
            Output.debug("Wczytano początkową ścieżkę: " + startPath);
        } else {
            Output.debug("Wczytano domyślną początkową ścieżkę: " + startPath);
        }
    }
}
