package pl.yalgrin.playnite.simplesync.exception;

public class ManualSynchronizationRequiredException extends RuntimeException {

    public ManualSynchronizationRequiredException() {
    }

    public ManualSynchronizationRequiredException(String message) {
        super(message);
    }

    public ManualSynchronizationRequiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManualSynchronizationRequiredException(Throwable cause) {
        super(cause);
    }

    public ManualSynchronizationRequiredException(String message, Throwable cause, boolean enableSuppression,
                                                  boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
