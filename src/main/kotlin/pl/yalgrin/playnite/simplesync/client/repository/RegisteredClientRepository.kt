package pl.yalgrin.playnite.simplesync.client.repository

import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.client.domain.RegisteredClient
import reactor.core.publisher.Mono

@Repository
interface RegisteredClientRepository : R2dbcRepository<RegisteredClient, String> {
    fun findByClientId(clientId: String): Mono<RegisteredClient>
}