package pl.yalgrin.playnite.simplesync.exception;

public class ForceFetchRequiredException extends RuntimeException {

    public ForceFetchRequiredException() {
    }

    public ForceFetchRequiredException(String message) {
        super(message);
    }

    public ForceFetchRequiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForceFetchRequiredException(Throwable cause) {
        super(cause);
    }

    public ForceFetchRequiredException(String message, Throwable cause, boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
