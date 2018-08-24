package igrek.songbook.service.preferences;

public class PreferencesDefinition {
	
	dbFilePath("Android/data/igrek.todotree/todo.json"),
	
	lockDB(false);
	
	
	private Object defaultValue;
	private PropertyType type;
	
	PropertyDefinition(String defaultValue) {
		this(PropertyType.STRING, defaultValue);
	}
	
	PropertyDefinition(Boolean defaultValue) {
		this(PropertyType.BOOLEAN, defaultValue);
	}
	
	PropertyDefinition(Integer defaultValue) {
		this(PropertyType.INTEGER, defaultValue);
	}
	
	PropertyDefinition(PropertyType type, Object defaultValue) {
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
