package igrek.songbook.domain.exception;

public class DataNotFoundException extends Exception {

    public DataNotFoundException() {
        super();
    }

    public DataNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
