package pl.yalgrin.playnite.simplesync.client.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ServerWebExchange
import pl.yalgrin.playnite.simplesync.client.domain.RegisteredClient
import pl.yalgrin.playnite.simplesync.client.dto.CheckRequestDTO
import pl.yalgrin.playnite.simplesync.client.dto.CheckResultDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegisteredClientDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.enums.CheckResult
import pl.yalgrin.playnite.simplesync.client.helper.RegistrationHandler
import pl.yalgrin.playnite.simplesync.client.repository.RegisteredClientRepository
import pl.yalgrin.playnite.simplesync.client.validator.RegisteredClientValidator
import pl.yalgrin.playnite.simplesync.common.config.CURRENT_API_VERSION
import pl.yalgrin.playnite.simplesync.common.util.pairWith
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.common.util.toSha1
import pl.yalgrin.playnite.simplesync.exception.ApiVersionException
import pl.yalgrin.playnite.simplesync.exception.ApiVersionExceptionType
import pl.yalgrin.playnite.simplesync.security.getSessionInfo
import reactor.core.publisher.Mono
import java.util.*
import kotlin.time.Clock
import kotlin.time.toJavaInstant

@Service
class RegisteredClientService(
    val registeredClientRepository: RegisteredClientRepository,
    val validator: RegisteredClientValidator,
    val registrationHandler: RegistrationHandler
) {
    @Transactional(rollbackFor = [Throwable::class])
    fun register(info: RegistrationRequestDTO): Mono<RegisteredClientDTO> {
        return doCheckApiVersion(info.supportedApiVersion)
            .flatMap { result ->
                when (result) {
                    CheckResult.OK -> {
                        doRegister(info)
                    }

                    CheckResult.OUTDATED_CLIENT -> {
                        Mono.error(ApiVersionException(ApiVersionExceptionType.OUTDATED_CLIENT))
                    }

                    else -> {
                        Mono.error(ApiVersionException(ApiVersionExceptionType.OUTDATED_SERVER))
                    }
                }
            }
    }

    private fun doRegister(info: RegistrationRequestDTO): Mono<RegisteredClientDTO> =
        validator.validateRegisterRequestMono(info)
            .then(Mono.fromSupplier { createToken() })
            .flatMap { token ->
                Mono.fromCallable { token.toSha1() }
                    .map { createEntity(info, it) }
                    .flatMap { registeredClientRepository.save(it) }
                    .map { RegisteredClientDTO(it.clientId, it.displayName, token) }
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

    fun check(checkRequest: CheckRequestDTO, exchange: ServerWebExchange): Mono<CheckResultDTO> {
        return doCheckConnection(checkRequest.supportedApiVersion, exchange)
    }

    private fun doCheckApiVersion(clientApiVersion: Int): Mono<CheckResult> = Mono.just(clientApiVersion)
        .map {
            if (clientApiVersion == CURRENT_API_VERSION)
                CheckResult.OK
            else if (clientApiVersion < CURRENT_API_VERSION)
                CheckResult.OUTDATED_CLIENT
            else
                CheckResult.OUTDATED_SERVER
        }

    private fun doCheckConnection(clientApiVersion: Int, exchange: ServerWebExchange): Mono<CheckResultDTO> =
        doCheckApiVersion(clientApiVersion).pairWith(registrationHandler.getRegistrationInfo(exchange))
            .map { (versionResult, registrationInfo) ->
                CheckResultDTO(
                    result = versionResult,
                    registrationSpecified = registrationInfo.registrationSpecified,
                    registrationValid = registrationInfo.registrationValid,
                    displayClientName = registrationInfo.displayClientName,
                    sessionActive = registrationInfo.sessionActive
                )
            }

    @Transactional(rollbackFor = [Throwable::class])
    fun changeName(newName: String): Mono<Unit> {
        return validator.validateChangeNameRequestMono(newName)
            .then(getSessionInfo())
            .flatMap { sessionInfo ->
                registeredClientRepository.findById(sessionInfo.clientId)
                    .map { registeredClient ->
                        registeredClient.displayName = newName
                        registeredClient
                    }
                    .flatMap { registeredClient ->
                        registeredClientRepository.save(registeredClient)
                    }
                    .thenAny()
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