package igrek.songbook.domain.song;

public enum SongCategoryType {
	
	ARTIST(1, null),
	
	CUSTOM(2, "song_category_custom"),
	
	OTHERS(3, "song_category_others");
	
	private long id;
	private String localeStringId;
	
	SongCategoryType(long id, String localeStringId) {
		this.id = id;
		this.localeStringId = localeStringId;
	}
	
	public long getId() {
		return id;
	}
	
	public String getLocaleStringId() {
		return localeStringId;
	}
}
