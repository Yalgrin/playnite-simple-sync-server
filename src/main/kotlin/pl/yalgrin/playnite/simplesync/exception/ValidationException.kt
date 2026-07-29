package pl.yalgrin.playnite.simplesync.exception

import pl.yalgrin.playnite.simplesync.dto.FieldErrorDTO
import java.io.Serial

class ValidationException(val errors: List<FieldErrorDTO> = emptyList()) : RuntimeException() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -6415214718172049734L
    }
}