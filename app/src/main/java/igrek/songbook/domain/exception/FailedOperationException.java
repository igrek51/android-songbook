package igrek.songbook.domain.exception;

public class FailedOperationException extends Exception {

	public FailedOperationException() {
		super();
	}

	public FailedOperationException(String detailMessage) {
		super(detailMessage);
	}
}
