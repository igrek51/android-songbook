package igrek.songbook.domain.crdfile;

public enum CRDTextType {
	
	REGULAR_TEXT(true),
	
	CHORDS(true),
	
	BRACKET(false),
	
	LINEWRAPPER(true);
	
	private boolean displayable;
	
	CRDTextType(boolean displayable) {
		this.displayable = displayable;
	}
	
	public boolean isDisplayable() {
		return displayable;
	}
}
