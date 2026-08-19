package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.NoRepositoryBean
import reactor.core.publisher.Flux

@NoRepositoryBean
interface ModelVersionObjectRepository<T : Any> : R2dbcRepository<T, Long> {
    fun findByModelVersion(modelVersion: Long, pageable: Pageable): Flux<T>
}