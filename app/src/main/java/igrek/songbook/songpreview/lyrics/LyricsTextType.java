package igrek.songbook.songpreview.lyrics;

public enum LyricsTextType {
	
	REGULAR_TEXT(true),
	
	CHORDS(true),
	
	BRACKET(false),
	
	LINEWRAPPER(true);
	
	private boolean displayable;
	
	LyricsTextType(boolean displayable) {
		this.displayable = displayable;
	}
	
	public boolean isDisplayable() {
		return displayable;
	}
}
