package igrek.songbook.model.exception;

public class WrongDbVersionException extends RuntimeException {
	
	public WrongDbVersionException() {
		super();
	}
	
	public WrongDbVersionException(String detailMessage) {
		super(detailMessage);
	}
}
