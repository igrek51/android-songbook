package igrek.songbook.service.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;

public class PreferencesService {
	
	private static final String SHARED_PREFERENCES_NAME = "SongBook-UserPreferences";
	@Inject
	Activity activity;
	private Logger logger = LoggerFactory.getLogger();
	private Map<String, Object> propertyValues = new HashMap<>();
	private SharedPreferences sharedPreferences;
	
	public PreferencesService() {
		DaggerIoc.getFactoryComponent().inject(this);
		sharedPreferences = createSharedPreferences();
		loadAll();
	}
	
	private SharedPreferences createSharedPreferences() {
		return activity.getApplicationContext()
				.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}
	
	private void loadAll() {
		for (PreferencesDefinition propertyDefinition : PreferencesDefinition.values()) {
			loadProperty(propertyDefinition);
		}
	}
	
	private void loadProperty(PreferencesDefinition propertyDefinition) {
		String propertyName = propertyDefinition.name();
		Object value = null;
		if (exists(propertyName)) {
			try {
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
					case LONG:
						value = sharedPreferences.getLong(propertyName, 0);
						break;
					case FLOAT:
						value = sharedPreferences.getFloat(propertyName, 0.0f);
						break;
				}
				logger.debug("preferences property loaded: " + propertyName + " = " + value);
			} catch (ClassCastException e) {
				value = propertyDefinition.getDefaultValue();
				logger.debug("Invalid property type, loading default value: " + propertyName + " = " + value);
			}
		} else {
			value = propertyDefinition.getDefaultValue();
			logger.debug("Missing preferences property, loading default value: " + propertyName + " = " + value);
		}
		propertyValues.put(propertyName, value);
	}
	
	public void saveAll() {
		for (PreferencesDefinition propertyDefinition : PreferencesDefinition.values()) {
			saveProperty(propertyDefinition);
		}
	}
	
	private void saveProperty(PreferencesDefinition propertyDefinition) {
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
				case LONG:
					setLong(propertyName, castIfNotNull(propertyValue, Long.class));
					break;
				case FLOAT:
					setFloat(propertyName, castIfNotNull(propertyValue, Float.class));
					break;
			}
			logger.debug("Shared preferences property saved: " + propertyName + " = " + propertyValue);
		} else {
			logger.warn("No shared preferences property found in map");
		}
	}
	
	public <T> T getValue(PreferencesDefinition propertyDefinition, Class<T> clazz) {
		String propertyName = propertyDefinition.name();
		if (!propertyValues.containsKey(propertyName))
			return null;
		
		Object propertyValue = propertyValues.get(propertyName);
		
		return castIfNotNull(propertyValue, clazz);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T castIfNotNull(Object o, Class<T> clazz) {
		if (o == null)
			return null;
		return (T) o;
	}
	
	public void setValue(PreferencesDefinition propertyDefinition, Object value) {
		String propertyName = propertyDefinition.name();
		// class type validation
		if (value != null) {
			String validClazz = propertyDefinition.getType().getClazz().getName();
			String givenClazz = value.getClass().getName();
			if (!givenClazz.equals(validClazz))
				throw new IllegalArgumentException("invalid value type, expected: " + validClazz + ", but given: " + givenClazz);
		}
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
	
	private void setLong(String name, Long value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (value == null) {
			editor.remove(name);
		} else {
			editor.putLong(name, value);
		}
		editor.apply();
	}
	
	private void setFloat(String name, Float value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (value == null) {
			editor.remove(name);
		} else {
			editor.putFloat(name, value);
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
