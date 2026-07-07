package pl.yalgrin.playnite.simplesync.security

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO

@Component
class SessionManager {
    private val sessionMap = mutableMapOf<String, SessionInfoDTO>()

    fun getSessionInfo(sessionId: String): SessionInfoDTO? {
        synchronized(sessionMap) {
            return sessionMap[sessionId]
        }
    }

    fun saveSessionInfo(sessionInfo: SessionInfoDTO) {
        synchronized(sessionMap) {
            if (sessionMap.values.any { it.clientId == sessionInfo.clientId }) {
                //TODO
                throw IllegalArgumentException("Client with id ${sessionInfo.clientId} has already registered session!")
            }
            sessionMap[sessionInfo.sessionId] = sessionInfo
        }
    }

    fun removeSessionInfo(sessionInfo: SessionInfoDTO) {
        synchronized(sessionMap) {
            sessionMap[sessionInfo.sessionId] = sessionInfo
        }
    }
}