package igrek.todotree.logic.exceptions;

public class NoSuperItemException extends Exception {
    public NoSuperItemException() {
        super();
    }

    public NoSuperItemException(String detailMessage) {
        super(detailMessage);
    }
}
