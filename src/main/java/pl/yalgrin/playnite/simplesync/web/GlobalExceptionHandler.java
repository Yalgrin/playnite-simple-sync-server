package pl.yalgrin.playnite.simplesync.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.yalgrin.playnite.simplesync.dto.ErrorDTO;
import pl.yalgrin.playnite.simplesync.exception.ForceFetchRequiredException;
import pl.yalgrin.playnite.simplesync.exception.ManualSynchronizationRequiredException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ManualSynchronizationRequiredException.class)
    public ResponseEntity<ErrorDTO> handleManualSynchronizationRequiredException(
            ManualSynchronizationRequiredException e) {
        log.error("Manual synchronization required!", e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDTO("manualSyncRequired"));
    }

    @ExceptionHandler(ForceFetchRequiredException.class)
    public ResponseEntity<ErrorDTO> handleForceFetchRequiredException(ForceFetchRequiredException e) {
        log.error("Force fetch required!", e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDTO("forceFetchRequired"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e) {
        log.error("Unknown exception!", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDTO("exception"));
    }
}
