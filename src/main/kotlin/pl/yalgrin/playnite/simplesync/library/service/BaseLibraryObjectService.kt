package pl.yalgrin.playnite.simplesync.library.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.common.helper.ITransactional
import pl.yalgrin.playnite.simplesync.common.util.first
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import pl.yalgrin.playnite.simplesync.library.mapper.LibraryObjectMapperImpl
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelObjectMapperImpl
import pl.yalgrin.playnite.simplesync.security.getSessionClientId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

abstract class BaseLibraryObjectService<D : LibraryObjectDTO, E : LibraryObjectEntity, P : PanelObjectDTO>(
    protected val repository: ObjectRepository<E>,
    protected val mapper: LibraryObjectMapperImpl<E, D>,
    protected val panelMapper: PanelObjectMapperImpl<E, P>,
    protected val changeService: ChangeService,
    protected val changeListenerService: ChangeListenerService,
    override val reactiveTransactionManager: ReactiveTransactionManager
) : LibraryObjectSaveService<D, P>, ITransactional {

    protected val log: Logger by lazy { LoggerFactory.getLogger(AopUtils.getTargetClass(this)) }

    override fun saveObjectAndPublishChanges(dto: D): Mono<D> {
        return saveObject(dto)
            .transactional()
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    override fun saveObject(dto: D): Mono<LibrarySaveResult<D>> {
        return findOrCreateEntity(dto)
            .flatMap { entity -> mapper.fillEntity(dto, entity) }
            .flatMap { entity -> updateAuditingFields(entity) }
            .doOnNext { entity ->
                log.debug(
                    "saveObject > entity has{} been changed!",
                    if (entity.isChanged) "" else " not"
                )
            }
            .filter { it.isChanged }
            .flatMap { entity -> repository.save(entity) }
            .flatMap { entity -> saveChange(entity) }
            .flatMap { doActionsAfterSave(it) }
            .flatMap { result ->
                mapper.toDTO(result.savedObject)
                    .map { LibrarySaveResult(it, result.generatedChanges) }
            }
            .switchIfEmpty {
                findOrCreateEntity(dto)
                    .flatMap { entity -> mapper.toDTO(entity) }
                    .map { LibrarySaveResult(it, emptyList()) }
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
        return dto.name.toMono().flatMapMany { repository.findByName(it) }.collectList().mapNotNull { objList ->
            objList.sortedWith(compareBy({ it.isRemoved }, { it.id })).firstOrNull()
        }
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

    protected open fun doActionsAfterSave(result: LibrarySaveResult<E>): Mono<LibrarySaveResult<E>> = result.toMono()

    override fun deleteObjectAndPublishChanges(dto: D): Mono<Unit> {
        return deleteObject(dto)
            .transactional()
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
                .flatMap { entity -> updateAuditingFields(entity) }
                .flatMap { entity -> repository.save(entity) }
                .mapNotNull { e ->
                    val change = createChange(clientId, e)
                    if (change != null) {
                        e to change
                    } else {
                        null
                    }
                }
                .flatMap { (entity, changeDTO) ->
                    changeService.saveChange(changeDTO).map { LibrarySaveResult(entity, listOf(it)) }
                }
                .collectList()
                .flatMap { doActionsAfterDelete(it) }
        }
    }

    protected open fun doActionsAfterDelete(results: List<LibrarySaveResult<E>>): Mono<List<ChangeDTO>> {
        return Flux.fromIterable(results)
            .map { it.generatedChanges }
            .flatMap { it.toFlux() }
            .collectList()
    }
    override fun saveObjectAndPublishChanges(dto: P): Mono<P> {
        return saveObject(dto)
            .transactional()
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    override fun saveObject(dto: P): Mono<LibrarySaveResult<P>> {
        return findOrCreateEntity(dto)
            .flatMap { entity -> panelMapper.fillEntity(dto, entity) }
            .flatMap { entity -> updateAuditingFields(entity) }
            .doOnNext { entity ->
                log.debug(
                    "saveObject > entity has{} been changed!",
                    if (entity.isChanged) "" else " not"
                )
            }
            .filter { it.isChanged }
            .flatMap { entity -> repository.save(entity) }
            .flatMap { entity -> saveChange(entity) }
            .flatMap { doActionsAfterSave(it) }
            .flatMap { result ->
                panelMapper.toDTO(result.savedObject)
                    .map { LibrarySaveResult(it, result.generatedChanges) }
            }
            .switchIfEmpty {
                findOrCreateEntity(dto)
                    .flatMap { entity -> panelMapper.toDTO(entity) }
                    .map { LibrarySaveResult(it, emptyList()) }
            }
    }

    private fun findOrCreateEntity(dto: P): Mono<E> {
        return findBasedOnId(dto)
            .switchIfEmpty(createNewEntity(dto))
    }

    private fun findBasedOnId(dto: P): Mono<E> {
        return dto.id.toMono().flatMapMany { repository.findById(it) }.first()
            .doOnNext { e ->
                log.debug("findOrCreateEntity > found entity with id = {}", e.id)
            }
    }

    private fun createNewEntity(dto: P): Mono<E> {
        return Mono.fromSupplier { createEntityFromPanelDTO(dto) }
            .doOnSubscribe { _ -> log.debug("findOrCreateEntity > creating new entity...") }
    }

    protected abstract fun createEntityFromPanelDTO(dto: P): E

    override fun deleteObjectAndPublishChanges(dto: P): Mono<Unit> {
        return deleteObject(dto)
            .transactional()
            .flatMap { dtoList ->
                changeListenerService.publishChanges(dtoList)
            }
    }

    private fun deleteObject(dto: P): Mono<List<ChangeDTO>> {
        return getSessionClientId().flatMap { clientId ->
            dto.toMono()
                .flatMapMany { d ->
                    val id = d.id
                    val name = d.name
                    if (id == null || name == null) {
                        Flux.empty()
                    } else {
                        repository.findByIdAndNameAndRemovedIsFalse(
                            id,
                            name
                        )
                    }
                }
                .map { e ->
                    log.debug("deleteObject > marking entity with id = {} as removed", e.id)
                    e.isRemoved = true
                    e
                }
                .flatMap { entity -> updateAuditingFields(entity) }
                .flatMap { entity -> repository.save(entity) }
                .mapNotNull { e ->
                    val change = createChange(clientId, e)
                    if (change != null) {
                        e to change
                    } else {
                        null
                    }
                }
                .flatMap { (entity, changeDTO) ->
                    changeService.saveChange(changeDTO).map { LibrarySaveResult(entity, listOf(it)) }
                }
                .collectList()
                .flatMap { doActionsAfterDelete(it) }
        }
    }

    protected abstract fun getObjectType(): ObjectType

    private fun updateAuditingFields(entity: E): Mono<E> = getSessionClientId()
        .doOnNext { clientId ->
            if (entity.id == null) {
                entity.createdAt = Instant.now()
                entity.createdBy = clientId
            }
            entity.modifiedAt = Instant.now()
            entity.modifiedBy = clientId
        }
        .thenReturn(entity)

    override fun findById(id: Long): Mono<D> {
        return repository.findById(id)
            .flatMap { entity -> mapper.toDTO(entity) }
            .transactionalReadOnly()
    }

    override fun findByIdForPanel(id: Long): Mono<P> {
        return repository.findById(id)
            .flatMap { entity -> panelMapper.toDTO(entity) }
            .transactionalReadOnly()
    }

    override fun findAllForPanel(): Mono<List<P>> {
        return repository.findAll()
            .filter { !it.isRemoved }
            .flatMap { entity -> panelMapper.toDTO(entity) }
            .sort(compareBy { (it.name ?: "").lowercase() })
            .collectList()
            .transactionalReadOnly()
    }
}