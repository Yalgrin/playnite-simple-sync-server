package pl.yalgrin.playnite.simplesync.library.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.common.util.first
import pl.yalgrin.playnite.simplesync.common.util.transactional
import pl.yalgrin.playnite.simplesync.common.util.transactionalReadOnly
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.mapper.LibraryObjectMapperImpl
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.security.getSessionClientId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class BaseLibraryObjectService<D : LibraryObjectDTO, E : LibraryObjectEntity>(
    protected val repository: ObjectRepository<E>,
    protected val mapper: LibraryObjectMapperImpl<E, D>,
    protected val changeService: ChangeService,
    protected val changeListenerService: ChangeListenerService,
    protected val transactionManager: ReactiveTransactionManager
) : LibraryObjectSaveService<D> {

    protected val log: Logger by lazy { LoggerFactory.getLogger(AopUtils.getTargetClass(this)) }

    override fun saveObjectAndPublishChanges(dto: D): Mono<D> {
        return saveObject(dto)
            .transactional(transactionManager)
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    override fun saveObject(dto: D): Mono<LibrarySaveResult<D>> {
        return findOrCreateEntity(dto)
            .flatMap { entity -> mapper.fillEntity(dto, entity) }
            .doOnNext { entity ->
                log.debug(
                    "saveObject > entity has{} been changed!",
                    if (entity.isChanged) "" else " not"
                )
            }
            .filter { it.isChanged }
            .flatMap { entity -> repository.save(entity) }
            .flatMap { entity -> saveChange(entity) }
            .flatMap { result ->
                mapper.toDTO(result.savedObject)
                    .map { LibrarySaveResult(it, result.generatedChanges) }
            }
    }

    private fun findOrCreateEntity(dto: D): Mono<E> {
        return findBasedOnId(dto)
            .switchIfEmpty(findBasedOnName(dto))
            .switchIfEmpty(createNewEntity(dto))
    }

    private fun findBasedOnId(dto: D): Mono<E> {
        return dto.id.toMono().flatMapMany { repository.findByPlayniteId(it) }.first()
            .doOnNext { e ->
                log.debug("findOrCreateEntity > found entity with id = {} by playnite id: {}", e.id, dto.id)
            }
    }

    private fun findBasedOnName(dto: D): Mono<E> {
        return dto.name.toMono().flatMapMany { repository.findByName(it) }.first()
            .doOnNext { e ->
                log.debug("findOrCreateEntity > found entity with id = {} by name: {}", e.id, dto.name)
                e.isNotifyAll = true
            }
    }

    private fun createNewEntity(dto: D): Mono<E> {
        return Mono.fromSupplier { createEntityFromDTO(dto) }
            .doOnSubscribe { _ -> log.debug("findOrCreateEntity > creating new entity...") }
    }

    protected abstract fun createEntityFromDTO(dto: D): E

    private fun saveChange(entity: E): Mono<LibrarySaveResult<E>> {
        return getSessionClientId()
            .mapNotNull { createChange(it, entity) }
            .flatMap { changeDTO -> changeService.saveChange(changeDTO) }
            .map { listOf(it) }
            .defaultIfEmpty(emptyList())
            .map { list ->
                LibrarySaveResult(
                    savedObject = entity,
                    generatedChanges = list
                )
            }
    }

    private fun createChange(clientId: String, entity: E): ChangeDTO? {
        val id = entity.id ?: return null
        return ChangeDTO(
            type = getObjectType(),
            clientId = clientId,
            objectId = id,
            isForceFetch = entity.isNotifyAll
        )
    }

    override fun deleteObjectAndPublishChanges(dto: D): Mono<Unit> {
        return deleteObject(dto)
            .transactional(transactionManager)
            .flatMap { dtoList ->
                changeListenerService.publishChanges(dtoList)
            }
    }

    private fun deleteObject(dto: D): Mono<List<ChangeDTO>> {
        return getSessionClientId().flatMap { clientId ->
            dto.toMono()
                .flatMapMany { d ->
                    val playniteId = d.id
                    val name = d.name
                    if (playniteId == null || name == null) {
                        Flux.empty()
                    } else {
                        repository.findByPlayniteIdAndNameAndRemovedIsFalse(
                            playniteId,
                            name
                        )
                    }
                }
                .map { e ->
                    log.debug("deleteObject > marking entity with id = {} as removed", e.id)
                    e.isRemoved = true
                    e
                }
                .flatMap { entity -> repository.save(entity) }
                .mapNotNull { e -> createChange(clientId, e) }
                .flatMap { changeDTO -> changeService.saveChange(changeDTO) }
                .collectList()
        }
    }

    protected abstract fun getObjectType(): ObjectType

    override fun findById(id: Long): Mono<D> {
        return repository.findById(id)
            .flatMap { entity -> mapper.toDTO(entity) }
            .transactionalReadOnly(transactionManager)
    }
}