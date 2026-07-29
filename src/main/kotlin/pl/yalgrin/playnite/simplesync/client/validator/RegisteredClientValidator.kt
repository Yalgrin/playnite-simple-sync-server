package pl.yalgrin.playnite.simplesync.client.validator

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.dto.FieldErrorDTO
import pl.yalgrin.playnite.simplesync.exception.ValidationException
import reactor.core.publisher.Mono

@Component
class RegisteredClientValidator {
    fun validateRegisterRequestMono(info: RegistrationRequestDTO): Mono<Void> {
        return Mono.fromRunnable {
            validateRegisterRequest(info)
        }
    }

    fun validateRegisterRequest(info: RegistrationRequestDTO) {
        if (info.displayName.isBlank()) {
            throw ValidationException(listOf(FieldErrorDTO("displayName", "validation.notNull")))
        }
        if (info.displayName.length > 200) {
            throw ValidationException(listOf(FieldErrorDTO("displayName", "validation.maxSize")))
        }
    }

    fun validateChangeNameRequestMono(newName: String): Mono<Void> {
        return Mono.fromRunnable {
            validateChangeNameRequest(newName)
        }
    }

    fun validateChangeNameRequest(newName: String) {
        if (newName.isBlank()) {
            throw ValidationException(listOf(FieldErrorDTO("newName", "validation.notNull")))
        }
        if (newName.length > 200) {
            throw ValidationException(listOf(FieldErrorDTO("newName", "validation.maxSize")))
        }
    }
}