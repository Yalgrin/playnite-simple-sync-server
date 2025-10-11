package pl.yalgrin.playnite.simplesync.exception;

import java.io.Serial;

public class ForceFetchRequiredException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8262781282909716606L;

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
