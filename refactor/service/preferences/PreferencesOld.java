package igrek.songbook.service.preferences;

import android.app.Activity;

import igrek.songbook.logger.Logger;
import igrek.songbook.service.controller.services.IService;

public class PreferencesOld extends BasePreferences implements IService {

    //TODO zapis szybkości scrolla, rozmiaru czcionki, w preferences

    //TODO oznaczenie pól anotacjami, zapis i odczyt przez mechanizm refleksji

    public String startPath = "/storage/extSdCard/guitarDB";
    private final String START_PATH = "startPath";

    public float fontsize = 23.0f;
    private final String FONTSIZE = "fontsize";

    public float autoscrollInterval = 300.0f;
    private final String AUTOSCROLL_INTERVAL = "autoscrollInterval";

    public PreferencesOld(Activity activity){
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
            Logger.debug("Wczytano początkową ścieżkę: " + startPath);
        } else {
            Logger.debug("Wczytano domyślną początkową ścieżkę: " + startPath);
        }
    }
}
