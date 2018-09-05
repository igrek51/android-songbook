package igrek.songbook.domain.exception;

public class NoParentItemException extends Exception {
	
	public NoParentItemException() {
		super();
	}
	
	public NoParentItemException(String detailMessage) {
		super(detailMessage);
	}
}
