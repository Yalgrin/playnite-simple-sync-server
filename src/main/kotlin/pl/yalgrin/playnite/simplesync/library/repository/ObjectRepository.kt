package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.NoRepositoryBean
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import reactor.core.publisher.Flux

@NoRepositoryBean
interface ObjectRepository<E : LibraryObjectEntity> : R2dbcRepository<E, Long> {
    fun findByPlayniteId(playniteId: String): Flux<E>

    fun findByPlayniteIdIn(playniteId: Collection<String>): Flux<E>

    fun findIdsByPlayniteIdIn(playniteId: Collection<String>): Flux<Long>

    fun findAllIds(): Flux<Long>

    fun findByName(name: String): Flux<E>

    fun findByPlayniteIdAndNameAndRemovedIsFalse(playniteId: String, name: String): Flux<E>
}