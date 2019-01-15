package igrek.songbook.songselection.songtree;

public class NoParentItemException extends Exception {
	
	public NoParentItemException() {
		super();
	}
	
	public NoParentItemException(String detailMessage) {
		super(detailMessage);
	}
}
