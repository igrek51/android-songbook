package igrek.songbook.logic.exceptions;

public class NoParentDirException extends Exception {
    public NoParentDirException() {
        super();
    }

    public NoParentDirException(String detailMessage) {
        super(detailMessage);
    }
}
