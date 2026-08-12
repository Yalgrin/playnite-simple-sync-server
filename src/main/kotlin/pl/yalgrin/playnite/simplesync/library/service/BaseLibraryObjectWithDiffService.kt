package pl.yalgrin.playnite.simplesync.library.service

import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.http.codec.multipart.FilePart
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.util.DigestUtils
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.common.util.first
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.common.util.transactional
import pl.yalgrin.playnite.simplesync.common.util.transactionalReadOnly
import pl.yalgrin.playnite.simplesync.exception.ManualSynchronizationRequiredException
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectDiffEntity
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDiffDTO
import pl.yalgrin.playnite.simplesync.library.mapper.LibraryObjectWithDiffMapper
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.security.getSessionClientId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

abstract class BaseLibraryObjectWithDiffService<DTO : LibraryObjectDTO,
        DIFF_DTO : LibraryObjectDiffDTO,
        E : LibraryObjectEntity,
        DIFF_E : LibraryObjectDiffEntity>(
    protected val repository: ObjectRepository<E>,
    protected val diffRepository: R2dbcRepository<DIFF_E, Long>,
    protected val mapper: LibraryObjectWithDiffMapper<E, DIFF_E, DTO, DIFF_DTO>,
    protected val changeService: ChangeService,
    protected val changeListenerService: ChangeListenerService,
    protected val metadataService: MetadataService,
    protected val transactionManager: ReactiveTransactionManager
) : LibraryObjectWithDiffSaveService<DTO, DIFF_DTO> {

    protected val log: Logger by lazy { LoggerFactory.getLogger(AopUtils.getTargetClass(this)) }

    override fun saveObjectAndPublishChanges(
        dto: DTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<DTO> {
        return saveObject(dto, fileParts, saveFiles)
            .transactional(transactionManager)
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    override fun saveObject(
        dto: DTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<LibrarySaveResult<DTO>> {
        return dto.toMono()
            .flatMap { findOrCreateEntity(it) }
            .flatMap { entity -> mapper.fillEntityAndGenerateDiff(dto, entity) }
            .flatMap { pair -> saveEntityAndFiles(pair, fileParts, saveFiles) }
            .flatMap { pair -> saveDiffIfNeeded(pair) }
            .flatMap { pair -> saveChange(pair) }
            .flatMap { triple ->
                mapper.toDTO(triple.first)
                    .map { LibrarySaveResult(it, listOf(triple.third)) }
            }
    }

    protected open fun findOrCreateEntity(dto: DTO): Mono<E> {
        return Mono.justOrEmpty(dto.id)
            .flatMapMany { playniteId -> repository.findByPlayniteId(playniteId) }
            .first()
            .doOnNext { e ->
                log.debug("findOrCreateEntity > found entity with id = {} by playnite id: {}", e.id, dto.id)
            }
            .switchIfEmpty(
                dto.name.toMono()
                    .flatMapMany { name: String? -> repository.findByName(name!!) }
                    .first()
                    .doOnNext { e ->
                        log.debug("findOrCreateEntity > found entity with id = {} by name: {}", e.id, dto.name)
                        e.isNotifyAll = true
                    }
            )
            .switchIfEmpty(
                Mono.fromSupplier { createEntityFromDTO(dto) }
                    .doOnNext { _ -> log.debug("findOrCreateEntity > creating new entity...") }
            )
    }

    private fun saveEntityAndFiles(
        pair: Pair<E, DIFF_DTO>,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<Pair<E, DIFF_DTO>> {
        return saveEntityIfNeeded(pair)
            .flatMap { p ->
                saveOrDeleteMetadataFiles(p.first, p.second, fileParts, saveFiles).then(
                    saveEntityIfChanged(p.first)
                )
            }
            .thenReturn(pair)
    }

    private fun saveEntityIfNeeded(pair: Pair<E, DIFF_DTO>): Mono<Pair<E, DIFF_DTO>> {
        return pair.first.toMono()
            .flatMap { e ->
                if (e.id == null) {
                    repository.save(e)
                } else {
                    e.toMono()
                }
            }
            .map { it to pair.second }
    }

    private fun saveEntityIfChanged(entity: E): Mono<E> {
        return entity.toMono()
            .doOnNext { e ->
                log.debug("saveObject > entity has{} been changed!", if (e.isChanged) "" else " not")
            }
            .filter(LibraryObjectEntity::isChanged)
            .flatMap { e -> repository.save(e) }
            .defaultIfEmpty(entity)
    }

    private fun saveOrDeleteMetadataFiles(
        entity: E,
        diffDTO: DIFF_DTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<Unit> {
        if (!saveFiles) {
            return Mono.empty()
        }
        return Flux.fromIterable(getMetadataFields())
            .flatMap { field ->
                fileParts
                    .filter { p -> field == FilenameUtils.getBaseName(p.filename()) }
                    .first()
                    .flatMap { filePart -> readFile(filePart) }
                    .flatMap { t ->
                        toFileData(field, t)
                            .flatMap { fileData ->
                                handleSaving(entity, fileData)
                            }
                    }
                    .switchIfEmpty(
                        deleteFileIfPossible(entity, field)
                    )
            }
            .collectList()
            .doOnNext { results ->
                val changedFields = results.filter { it.isSaved }.map { it.fieldName }
                diffDTO.changedFields = diffDTO.changedFields.plus(changedFields)
                if (changedFields.isNotEmpty()) {
                    entity.isChanged = true
                }
            }.thenAny()
    }

    private fun toFileData(
        field: String,
        t: Pair<ByteArray, String>
    ): Mono<FileData> = Mono.fromCallable {
        FileData(
            fieldName = field,
            bytes = t.first,
            md5 = DigestUtils.md5DigestAsHex(t.first),
            filename = t.second,
            toSave = true
        )
    }.subscribeOn(Schedulers.parallel())

    private fun handleSaving(
        entity: E,
        fileData: FileData
    ): Mono<SaveResult> {
        return hasFileDataChanged(entity, fileData.bytes, fileData.md5, fileData.fieldName)
            .defaultIfEmpty(false)
            .flatMap {
                if (it) {
                    true.toMono()
                } else {
                    fileDoesNotExist(getMetadataFolder(), getIdPart(entity), fileData.fieldName)
                }
            }
            .flatMap { changed ->
                if (changed) {
                    metadataService.saveMetadata(
                        getMetadataFolder(), getIdPart(entity), fileData.filename,
                        fileData.bytes, fileData.fieldName
                    )
                        .flatMap { _ ->
                            applyFileChange(entity, fileData.fieldName, fileData.md5)
                        }.map { SaveResult(fileData.fieldName, true) }
                } else {
                    Mono.fromRunnable<Unit> {
                        log.debug("saveOrDeleteMetadataFiles > skipping file: {}", fileData.fieldName)
                    }
                        .then(
                            metadataService.deleteExcessiveMetadata(
                                getMetadataFolder(), getIdPart(entity),
                                fileData.filename, fileData.fieldName
                            )
                        ).thenReturn(SaveResult(fileData.fieldName, false))
                }
            }
    }

    private fun deleteFileIfPossible(entity: E, field: String): Mono<SaveResult> =
        metadataService.deleteMetadata(getMetadataFolder(), getIdPart(entity), field)
            .defaultIfEmpty(false)
            .flatMap { deletedAnything ->
                applyFileChange(entity, field, null)
                    .defaultIfEmpty(false)
                    .map { it || deletedAnything }
            }.map { SaveResult(field, it) }

    private fun fileDoesNotExist(metadataFolder: String, idPart: String, fieldName: String): Mono<Boolean> {
        return metadataService.fileDoesNotExist(metadataFolder, idPart, fieldName)
    }

    protected fun readFile(filePart: FilePart): Mono<Pair<ByteArray, String>> {
        return DataBufferUtils.join(filePart.content())
            .map { dataBuffer ->
                try {
                    val bytes = ByteArray(dataBuffer.readableByteCount())
                    dataBuffer.read(bytes)
                    bytes to filePart.filename()
                } finally {
                    DataBufferUtils.release(dataBuffer)
                }
            }
    }

    private fun saveDiffIfNeeded(t: Pair<E, DIFF_DTO>): Mono<Pair<E, DIFF_E>> {
        return t.second.toMono()
            .filter { dto -> dto.changedFields.isNotEmpty() }
            .doOnNext { dto ->
                dto.baseObjectId = t.first.id
                log.debug("saveDiffIfNeeded > diffDTO: {}", dto)
            }
            .flatMap { dto -> mapper.toEntity(dto) }
            .flatMap { entity -> diffRepository.save(entity) }
            .map { e -> t.first to e }
    }

    private fun saveChange(
        pair: Pair<E, DIFF_E>
    ): Mono<Triple<E, DIFF_E, ChangeDTO>> {
        return getSessionClientId()
            .flatMap { clientId -> createChange(clientId, pair) }
            .flatMap { changeService.saveChange(it) }
            .map { Triple(pair.first, pair.second, it) }
    }

    private fun createChange(clientId: String, tuple: Pair<E, DIFF_E>): Mono<ChangeDTO> {
        return Mono.fromSupplier { tuple.second.isForEntireObject }
            .defaultIfEmpty(false)
            .map { isForEntireObject ->
                if (isForEntireObject) {
                    ChangeDTO(
                        type = getObjectType(),
                        clientId = clientId,
                        objectId = tuple.first.id!!,
                        isForceFetch = tuple.first.isNotifyAll
                    )
                } else {
                    ChangeDTO(
                        type = getDiffType(),
                        clientId = clientId,
                        objectId = tuple.second.id!!,
                        isForceFetch = tuple.first.isNotifyAll
                    )
                }
            }
    }

    override fun saveObjectDiffAndPublishChanges(diffDto: DIFF_DTO, fileParts: Flux<FilePart>): Mono<DTO> {
        return saveObjectDiff(diffDto, fileParts)
            .transactional(transactionManager)
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    override fun saveObjectDiff(
        diffDto: DIFF_DTO,
        fileParts: Flux<FilePart>
    ): Mono<LibrarySaveResult<DTO>> {
        return getSessionClientId()
            .flatMap { clientId ->
                diffDto.toMono()
                    .doOnNext { dto ->
                        log.debug("saveObjectDiff > START, dto: {}, clientId: {}", dto, clientId)
                    }
                    .flatMap { dto -> findOrCreateEntity(dto) }
                    .flatMap { entity -> mapper.fillEntityAndGenerateDiff(diffDto, entity) }
                    .flatMap { tuple ->
                        saveEntityAndFilesFromDiff(
                            diffDto,
                            fileParts,
                            tuple
                        )
                    }
                    .flatMap { t -> saveDiffIfNeeded(t) }
                    .flatMap { c -> saveChange(c) }
                    .flatMap { t ->
                        mapper.toDTO(t.first)
                            .map { LibrarySaveResult(it, listOf(t.third)) }
                    }
                    .doOnSuccess { dto ->
                        log.debug(
                            "saveObjectDiff > END, dto: {}",
                            dto
                        )
                    }
            }
    }

    protected open fun findOrCreateEntity(dto: DIFF_DTO): Mono<E> {
        return dto.id.toMono()
            .flatMap { repository.findByPlayniteId(it).first() }
            .doOnNext { e ->
                log.debug("findOrCreateEntity > found entity with id = {} by playnite id: {}", e.id, dto.id)
            }
            .switchIfEmpty(Mono.defer { Mono.error(ManualSynchronizationRequiredException("Manual synchronization required!")) })
    }

    private fun saveEntityAndFilesFromDiff(
        referenceDiffDTO: DIFF_DTO,
        fileParts: Flux<FilePart>,
        tuple: Pair<E, DIFF_DTO>
    ): Mono<Pair<E, DIFF_DTO>> {
        return Mono.fromSupplier { referenceDiffDTO.changedFields }
            .map { it.isNotEmpty() }
            .flatMap { changed ->
                if (changed) {
                    saveOrDeleteMetadataFilesFromDiff(referenceDiffDTO, fileParts, tuple)
                        .then(saveEntityIfChanged(tuple.first))
                        .map { it to tuple.second }
                        .defaultIfEmpty(tuple)
                } else {
                    tuple.toMono()
                }
            }
    }

    private fun saveOrDeleteMetadataFilesFromDiff(
        referenceDiffDTO: DIFF_DTO,
        fileParts: Flux<FilePart>,
        tuple: Pair<E, DIFF_DTO>
    ): Mono<Unit> {
        return Flux.fromIterable(referenceDiffDTO.changedFields)
            .filter { str -> getMetadataFields().contains(str) }
            .flatMap { field ->
                fileParts.filter { p -> field == FilenameUtils.getBaseName(p.filename()) }
                    .first()
                    .flatMap { filePart -> this.readFile(filePart) }
                    .flatMap { t ->
                        toFileData(field, t)
                            .flatMap { fileData ->
                                handleSaving(tuple.first, fileData)
                            }
                    }
                    .switchIfEmpty(
                        deleteFileIfPossible(tuple.first, field)
                    )
            }
            .collectList()
            .doOnNext { results ->
                val changedFields = results.filter { it.isSaved }.map { it.fieldName }
                tuple.second.changedFields = tuple.second.changedFields.plus(changedFields)
                if (changedFields.isNotEmpty()) {
                    tuple.first.isChanged = true
                }
            }
            .thenAny()
    }

    override fun deleteObjectAndPublishChanges(dto: DTO): Mono<Unit> {
        return getSessionClientId()
            .flatMap { clientId ->
                doDeleteObject(dto, clientId)
                    .transactional(transactionManager)
                    .flatMap { dtoList -> changeListenerService.publishChanges(dtoList) }
            }
    }

    private fun doDeleteObject(dto: DTO, clientId: String): Mono<List<ChangeDTO>> {
        return dto.toMono()
            .doOnNext { d ->
                log.debug("deleteObject > START, dto: {}, clientId: {}", d, clientId)
            }
            .flatMapMany { categoryDTO -> this.findObjectToDelete(categoryDTO) }
            .map { e ->
                log.debug(
                    "deleteObject > marking entity with id = {} as removed",
                    e.id
                )
                e.isRemoved = true
                e
            }
            .flatMap { entity -> repository.save(entity) }
            .flatMap { e -> changeService.saveChange(createDeleteChange(clientId, e)) }
            .collectList()
            .doOnSuccess { log.debug("deleteObject > END") }
    }

    protected open fun findObjectToDelete(dto: DTO): Flux<E> {
        return Mono.fromSupplier {
            val id = dto.id
            val name = dto.name
            if (id != null && name != null) {
                id to name
            } else {
                null
            }
        }.flatMapMany { repository.findByPlayniteIdAndNameAndRemovedIsFalse(it.first, it.second) }
    }

    private fun createDeleteChange(clientId: String?, entity: E): ChangeDTO {
        return ChangeDTO(null, getObjectType(), clientId, entity.id!!, entity.isNotifyAll)
    }

    override fun findById(id: Long): Mono<DTO> {
        return repository.findById(id).flatMap { entity -> mapper.toDTO(entity) }
            .transactionalReadOnly(transactionManager)
    }

    override fun findDiffById(id: Long): Mono<DIFF_DTO> {
        return diffRepository.findById(id).flatMap { diff ->
            diff.playniteId.toMono()
                .flatMap { repository.findByPlayniteId(it).first() }
                .flatMap { e -> mapper.toDiffDTO(e, diff) }
        }.transactionalReadOnly(transactionManager)
    }

    protected abstract fun applyFileChange(entity: E, baseName: String, md5: String?): Mono<Boolean>

    protected abstract fun getMetadataFolder(): String

    protected fun getIdPart(e: E): String {
        return e.id.toString() + ""
    }

    protected abstract fun createEntityFromDTO(dto: DTO): E

    protected abstract fun getMetadataFields(): Set<String>

    protected abstract fun hasFileDataChanged(
        entity: E,
        bytes: ByteArray,
        md5: String,
        fieldName: String
    ): Mono<Boolean>

    protected abstract fun getObjectType(): ObjectType

    protected abstract fun getDiffType(): ObjectType
}


private data class FileData(
    val fieldName: String,
    val bytes: ByteArray = byteArrayOf(),
    val md5: String,
    val filename: String,
    val toSave: Boolean = false
)

private data class SaveResult(
    val fieldName: String,
    val isSaved: Boolean
)