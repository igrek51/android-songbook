package igrek.songbook.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class BasePreferences {

    protected SharedPreferences sharedPreferences;

    private static final String SHARED_PREFERENCES_NAME = "SongBookUserPreferences";

    public BasePreferences(Activity activity) {
        sharedPreferences = activity.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void setBoolean(String name, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public void setInt(String name, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    public void setString(String name, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public boolean getBoolean(String name, boolean _default) {
        return sharedPreferences.getBoolean(name, _default);
    }

    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    public int getInt(String name, int _default) {
        return sharedPreferences.getInt(name, _default);
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public String getString(String name, String _default) {
        return sharedPreferences.getString(name, _default);
    }

    public String getString(String name) {
        return getString(name, "");
    }

    public boolean exists(String name) {
        return sharedPreferences.contains(name);
    }
}
