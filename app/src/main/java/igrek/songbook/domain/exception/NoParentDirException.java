package igrek.songbook.domain.exception;

public class NoParentDirException extends Exception {
	
	public NoParentDirException() {
		super();
	}
	
	public NoParentDirException(String detailMessage) {
		super(detailMessage);
	}
}