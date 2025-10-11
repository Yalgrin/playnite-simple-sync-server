package pl.yalgrin.playnite.simplesync.exception;

import java.io.Serial;

public class ManualSynchronizationRequiredException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6189953396806395343L;

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
