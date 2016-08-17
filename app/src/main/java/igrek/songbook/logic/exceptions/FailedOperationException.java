package igrek.songbook.logic.exceptions;

public class FailedOperationException extends Exception {

    public FailedOperationException() {
        super();
    }

    public FailedOperationException(String detailMessage) {
        super(detailMessage);
    }
}
