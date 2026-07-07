package pl.yalgrin.playnite.simplesync.client.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.yalgrin.playnite.simplesync.client.domain.RegisteredClient
import pl.yalgrin.playnite.simplesync.client.dto.RegisteredClientDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.repository.RegisteredClientRepository
import pl.yalgrin.playnite.simplesync.security.getSessionInfo
import pl.yalgrin.playnite.simplesync.util.thenAny
import pl.yalgrin.playnite.simplesync.util.toSha1
import reactor.core.publisher.Mono
import java.util.*
import kotlin.time.Clock
import kotlin.time.toJavaInstant

@Service
class RegisteredClientService(
    val registeredClientRepository: RegisteredClientRepository
) {
    @Transactional(rollbackFor = [Throwable::class])
    fun register(info: RegistrationRequestDTO): Mono<RegisteredClientDTO> {
        return Mono.fromSupplier { createToken() }
            .flatMap { token ->
                Mono.fromCallable { token.toSha1() }
                    .map { createEntity(info, it) }
                    .flatMap { registeredClientRepository.save(it) }
                    .map { RegisteredClientDTO(it.clientId, it.displayName, token) }
            }
    }

    private fun createToken(): String {
        return UUID.randomUUID().toString()
    }

    private fun createEntity(info: RegistrationRequestDTO, token: String): RegisteredClient {
        return RegisteredClient(
            clientId = UUID.randomUUID().toString(),
            displayName = info.displayName,
            clientToken = token,
            newEntity = true
        )
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun changeName(newName: String): Mono<*> {
        return getSessionInfo()
            .flatMap { sessionInfo ->
                registeredClientRepository.findById(sessionInfo.clientId)
                    .map { registeredClient ->
                        registeredClient.displayName = newName
                        registeredClient
                    }
                    .flatMap { registeredClient ->
                        registeredClientRepository.save(registeredClient)
                    }
            }
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun updateConnectedTime(clientId: String): Mono<Unit> {
        return registeredClientRepository.findById(clientId)
            .map { registeredClient ->
                registeredClient.lastConnected = Clock.System.now().toJavaInstant()
                registeredClient
            }
            .flatMap { registeredClient ->
                registeredClientRepository.save(registeredClient)
            }.thenAny()
    }
}