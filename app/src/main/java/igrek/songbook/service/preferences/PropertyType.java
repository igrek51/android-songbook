package igrek.songbook.service.preferences;

enum PropertyType {
	
	STRING(String.class),
	
	BOOLEAN(Boolean.class),
	
	INTEGER(Integer.class),
	
	LONG(Long.class),
	
	FLOAT(Float.class);
	
	private Class<?> clazz;
	
	PropertyType(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
}
