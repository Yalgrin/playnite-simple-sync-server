package pl.yalgrin.playnite.simplesync.migration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.data.domain.Pageable
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.common.util.transactional
import pl.yalgrin.playnite.simplesync.common.util.transactionalReadOnly
import pl.yalgrin.playnite.simplesync.library.repository.ModelVersionObjectRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ObjectMigrator {
    fun getSourceVersion(): Long

    fun getTargetVersion(): Long

    fun getHandledType(): MigrationObjectType

    fun migrateAll(): Mono<Unit>
}

abstract class AbstractObjectMigration<T : Any>(
    protected val repository: ModelVersionObjectRepository<T>,
    protected val transactionManager: ReactiveTransactionManager
) : ObjectMigrator {
    protected val log: Logger by lazy { LoggerFactory.getLogger(AopUtils.getTargetClass(this)) }

    override fun migrateAll(): Mono<Unit> {
        return Flux.range(0, Int.MAX_VALUE)
            .concatMap { migrateSingleBatch() }
            .takeUntil { hasMore -> !hasMore }
            .thenAny()
    }

    private fun migrateSingleBatch(): Mono<Boolean> {
        return fetchBatch()
            .flatMap { batch ->
                processBatch(batch).thenReturn(true)
            }
            .defaultIfEmpty(false)
    }

    private fun processBatch(batch: List<T>): Mono<Unit> {
        return Flux.fromIterable(batch)
            .flatMap { migrate(it) }
            .then()
            .transactional(transactionManager)
            .doOnSubscribe { log.debug("Processing batch of {} objects", batch.size) }
            .doOnError { log.error("Error processing batch of {} objects", batch.size, it) }
            .doOnSuccess { log.debug("Batch of {} objects processed", batch.size) }
            .thenAny()
    }

    protected abstract fun migrate(entity: T): Mono<Unit>

    private fun fetchBatch(): Mono<List<T>> {
        return repository.findByModelVersion(getSourceVersion(), Pageable.ofSize(100)).collectList()
            .transactionalReadOnly(transactionManager)
            .doOnNext {
                log.debug(
                    "Fetched batch of {} objects of type {} for migration (version {} to {})",
                    it.size,
                    getHandledType(),
                    getSourceVersion(),
                    getTargetVersion()
                )
            }
            .filter { it.isNotEmpty() }
    }
}