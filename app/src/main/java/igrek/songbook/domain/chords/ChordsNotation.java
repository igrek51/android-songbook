package igrek.songbook.domain.chords;

import igrek.songbook.R;

public enum ChordsNotation {
	
	GERMAN(1, R.string.notation_german),
	
	ENGLISH(2, R.string.notation_english);
	
	private final int id;
	private final int displayNameResId;
	
	ChordsNotation(int id, int displayNameResId) {
		this.id = id;
		this.displayNameResId = displayNameResId;
	}
	
	public int getId() {
		return id;
	}
	
	public int getDisplayNameResId() {
		return displayNameResId;
	}
}
