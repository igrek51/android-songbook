package igrek.songbook.service.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import igrek.todotree.logger.Logger;

public class PreferencesService {
	
	
	private static final String SHARED_PREFERENCES_NAME = "ToDoTreeUserPreferences";
	
	private Map<String, Object> propertyValues = new HashMap<>();
	
	private SharedPreferences sharedPreferences;
	private Logger logger;
	
	public Preferences(Activity activity, Logger logger) {
		this.logger = logger;
		sharedPreferences = createSharedPreferences(activity);
		loadAll();
	}
	
	protected SharedPreferences createSharedPreferences(Activity activity){
		return activity.getApplicationContext()
				.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}
	
	private void loadAll() {
		for (PropertyDefinition propertyDefinition : PropertyDefinition.values()) {
			loadProperty(propertyDefinition);
		}
	}
	
	private void loadProperty(PropertyDefinition propertyDefinition) {
		String propertyName = propertyDefinition.name();
		Object value = null;
		if (exists(propertyName)) {
			switch (propertyDefinition.getType()) {
				case STRING:
					value = sharedPreferences.getString(propertyName, null);
					break;
				case BOOLEAN:
					value = sharedPreferences.getBoolean(propertyName, false);
					break;
				case INTEGER:
					value = sharedPreferences.getInt(propertyName, 0);
					break;
			}
			logger.debug("preferences property loaded: " + propertyName + " = " + value);
		} else {
			value = propertyDefinition.getDefaultValue();
			logger.debug("Missing preferences property, loading default value: " + propertyName + " = " + value);
		}
		propertyValues.put(propertyName, value);
	}
	
	public void saveAll() {
		for (PropertyDefinition propertyDefinition : PropertyDefinition.values()) {
			saveProperty(propertyDefinition);
		}
	}
	
	private void saveProperty(PropertyDefinition propertyDefinition) {
		String propertyName = propertyDefinition.name();
		if (propertyValues.containsKey(propertyName)) {
			Object propertyValue = propertyValues.get(propertyName);
			switch (propertyDefinition.getType()) {
				case STRING:
					setString(propertyName, castIfNotNull(propertyValue, String.class));
					break;
				case BOOLEAN:
					setBoolean(propertyName, castIfNotNull(propertyValue, Boolean.class));
					break;
				case INTEGER:
					setInt(propertyName, castIfNotNull(propertyValue, Integer.class));
					break;
			}
			logger.debug("Shared preferences property saved: " + propertyName + " = " + propertyValue);
		} else {
			logger.warn("No shared preferences property found in map");
		}
	}
	
	public <T> T getValue(PropertyDefinition propertyDefinition, Class<T> clazz) {
		String propertyName = propertyDefinition.name();
		if (!propertyValues.containsKey(propertyName))
			return null;
		
		Object propertyValue = propertyValues.get(propertyName);
		
		return castIfNotNull(propertyValue, clazz);
	}
	
	private <T> T castIfNotNull(Object o, Class<T> clazz) {
		if (o == null)
			return null;
		return (T) o;
	}
	
	public void setValue(PropertyDefinition propertyDefinition, Object value) {
		String propertyName = propertyDefinition.name();
		propertyValues.put(propertyName, value);
	}
	
	public void clear() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
	}
	
	private void setBoolean(String name, Boolean value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (value == null) {
			editor.remove(name);
		} else {
			editor.putBoolean(name, value);
		}
		editor.apply();
	}
	
	private void setInt(String name, Integer value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (value == null) {
			editor.remove(name);
		} else {
			editor.putInt(name, value);
		}
		editor.apply();
	}
	
	private void setString(String name, String value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (value == null) {
			editor.remove(name);
		} else {
			editor.putString(name, value);
		}
		editor.apply();
	}
	
	public boolean exists(String name) {
		return sharedPreferences.contains(name);
	}
	
}
