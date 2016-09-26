package igrek.songbook.resources;

import android.app.Activity;

import igrek.songbook.logic.controller.AppController;
import igrek.songbook.logic.controller.services.IService;

public class LangStringService implements IService {

    Activity activity;

    public LangStringService(Activity activity) {
        this.activity = activity;
        AppController.registerService(this);
    }

    public String resString(int resourceId) {
        return activity.getResources().getString(resourceId);
    }
}
