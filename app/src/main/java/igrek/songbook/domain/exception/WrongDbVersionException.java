package igrek.songbook.domain.exception;

public class WrongDbVersionException extends RuntimeException {
	
	public WrongDbVersionException() {
		super();
	}
	
	public WrongDbVersionException(String detailMessage) {
		super(detailMessage);
	}
}
