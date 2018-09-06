package igrek.songbook.service.preferences;

public enum PreferencesDefinition {
	
	fontsize(24.0f), // dp
	
	autoscrollInitialPause(35000L), // ms
	
	autoscrollSpeed(0.1f); // em / s
	
	private Object defaultValue;
	private PropertyType type;
	
	PreferencesDefinition(String defaultValue) {
		this(PropertyType.STRING, defaultValue);
	}
	
	PreferencesDefinition(Boolean defaultValue) {
		this(PropertyType.BOOLEAN, defaultValue);
	}
	
	PreferencesDefinition(Long defaultValue) {
		this(PropertyType.LONG, defaultValue);
	}
	
	PreferencesDefinition(Float defaultValue) {
		this(PropertyType.FLOAT, defaultValue);
	}
	
	PreferencesDefinition(PropertyType type, Object defaultValue) {
		this.type = type;
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public PropertyType getType() {
		return type;
	}
	
	
}
