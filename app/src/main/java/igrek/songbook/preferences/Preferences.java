package igrek.songbook.preferences;

import android.app.Activity;

import igrek.songbook.logger.Logs;
import igrek.songbook.logic.controller.services.IService;

public class Preferences extends BasePreferences implements IService {

    //TODO zapis szybkości scrolla, rozmiaru czcionki, w preferences

    //TODO oznaczenie pól anotacjami, zapis i odczyt przez mechanizm refleksji

    public String startPath = "/storage/extSdCard/Gitara";
    private final String START_PATH = "startPath";

    public float fontsize = 23.0f;
    private final String FONTSIZE = "fontsize";

    public float autoscrollInterval = 300.0f;
    private final String AUTOSCROLL_INTERVAL = "autoscrollInterval";

    public Preferences(Activity activity){
        super(activity);
        loadAll();
    }

    public void saveAll() {
        if (startPath != null) {
            setString(START_PATH, startPath);
        }
    }

    public void loadAll() {
        if (exists(START_PATH)) {
            startPath = getString(START_PATH);
            Logs.debug("Wczytano początkową ścieżkę: " + startPath);
        } else {
            Logs.debug("Wczytano domyślną początkową ścieżkę: " + startPath);
        }
    }
}
