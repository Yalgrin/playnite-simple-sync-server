package pl.yalgrin.playnite.simplesync.security

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionSettingsDTO
import pl.yalgrin.playnite.simplesync.exception.AuthException
import pl.yalgrin.playnite.simplesync.exception.AuthExceptionType
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class SessionManager {
    companion object {
        private val log = LoggerFactory.getLogger(SessionManager::class.java)
    }

    private val sessionMap = mutableMapOf<String, SessionInfoDTO>()
    private val sessionSettingsMap = mutableMapOf<String, SessionSettingsDTO>()

    fun getSessionInfo(sessionId: String): SessionInfoDTO? {
        synchronized(sessionMap) {
            return sessionMap[sessionId]
        }
    }

    fun getSessionSettings(sessionId: String): SessionSettingsDTO? {
        synchronized(sessionMap) {
            return sessionSettingsMap[sessionId]
        }
    }

    fun getSessionInfoMono(sessionId: String): Mono<SessionInfoDTO> {
        return Mono.fromCallable { getSessionInfo(sessionId) }
            .subscribeOn(Schedulers.boundedElastic())
    }

    fun getSessionSettingsMono(sessionId: String): Mono<SessionSettingsDTO> {
        return Mono.fromCallable { getSessionSettings(sessionId) }
            .subscribeOn(Schedulers.boundedElastic())
    }

    fun saveSessionInfo(sessionInfo: SessionInfoDTO) {
        synchronized(sessionMap) {
            if (sessionMap.values.any { it.clientId == sessionInfo.clientId }) {
                log.warn("Client with id {} is already registered", sessionInfo.clientId)
                throw AuthException(AuthExceptionType.CLIENT_ALREADY_REGISTERED)
            }
            log.info("Saving session info for client with id {} ({})", sessionInfo.clientId, sessionInfo.displayName)
            sessionMap[sessionInfo.sessionId] = sessionInfo
            sessionSettingsMap[sessionInfo.sessionId] = SessionSettingsDTO()
        }
    }

    fun updateSettings(sessionInfo: SessionInfoDTO, sessionSettingsDTO: SessionSettingsDTO) {
        synchronized(sessionSettingsMap) {
            log.info(
                "Updating session settings for client with id {} ({})",
                sessionInfo.clientId,
                sessionInfo.displayName
            )
            sessionSettingsMap[sessionInfo.sessionId] = sessionSettingsDTO
        }
    }

    fun removeSessionInfo(sessionInfo: SessionInfoDTO) {
        synchronized(sessionMap) {
            log.info("Removing session info for client with id {} ({})", sessionInfo.clientId, sessionInfo.displayName)
            sessionMap.remove(sessionInfo.sessionId)
            sessionSettingsMap.remove(sessionInfo.sessionId)
        }
    }
}