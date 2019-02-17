package igrek.songbook.songselection.tree;

public class NoParentItemException extends Exception {
	
	public NoParentItemException() {
		super();
	}
	
	public NoParentItemException(String detailMessage) {
		super(detailMessage);
	}
}
