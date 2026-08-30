package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import pl.yalgrin.playnite.simplesync.common.util.pairWith
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectDiffEntity
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.LibraryDiffDatabaseModelDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectFields
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

interface LibraryObjectWithDiffMapper<E : LibraryObjectEntity, DIFF_E : LibraryObjectDiffEntity, DTO : LibraryObjectDTO, DIFF_DTO : LibraryObjectDiffDTO> {
    fun fillEntityAndGenerateDiff(dto: DTO, entity: E): Mono<Pair<E, DIFF_DTO>>
    fun toDTO(entity: E): Mono<DTO>
    fun toEntity(entity: E, dto: DIFF_DTO): Mono<DIFF_E>
    fun toDiffDTO(entity: E, diffEntity: DIFF_E): Mono<DIFF_DTO>
    fun fillEntityAndGenerateDiff(dto: DIFF_DTO, entity: E): Mono<Pair<E, DIFF_DTO>>
}

abstract class LibraryObjectWithDiffMapperImpl<
        E : LibraryObjectEntity,
        DIFF_E : LibraryObjectDiffEntity,
        DTO : LibraryObjectDTO,
        DIFF_DTO : LibraryObjectDiffDTO,
        DB_MODEL : LibraryDiffDatabaseModelDTO> : LibraryObjectWithDiffMapper<E, DIFF_E, DTO, DIFF_DTO> {

    override fun fillEntityAndGenerateDiff(dto: DTO, entity: E): Mono<Pair<E, DIFF_DTO>> {
        return Mono.fromSupplier { createDiffDTO() }
            .map { diffDTO -> fillBasicFields(dto, entity, diffDTO) }
            .flatMap { (entity, diffDTO, changedFields) -> fillOtherFields(entity, dto, diffDTO, changedFields) }
            .map { t ->
                t.second.changedFields = t.third
                t.first.isChanged = t.third.isNotEmpty()
                Pair(t.first, t.second)
            }
    }

    protected open fun fillBasicFields(
        dto: DTO,
        entity: E,
        generatedDiffDTO: DIFF_DTO
    ): Triple<E, DIFF_DTO, MutableList<String>> {
        val changedFields = mutableListOf<String>()
        generatedDiffDTO.id = dto.id
        generatedDiffDTO.name = dto.name
        if (entity.id == null) {
            changedFields.add(LibraryObjectFields.ID)
        }
        if (!Strings.CS.equals(entity.name, dto.name)) {
            entity.name = dto.name
            changedFields.add(LibraryObjectFields.NAME)
        }
        if (entity.isRemoved) {
            entity.isRemoved = false
            entity.playniteId = dto.id
            generatedDiffDTO.isRemoved = dto.isRemoved
            changedFields.add(LibraryObjectFields.REMOVED)
        }
        return Triple(entity, generatedDiffDTO, changedFields)
    }

    protected open fun fillOtherFields(
        entity: E,
        dto: DTO,
        generatedDiffDTO: DIFF_DTO,
        changedFields: MutableList<String>
    ): Mono<Triple<E, DIFF_DTO, MutableList<String>>> {
        return Triple(entity, generatedDiffDTO, changedFields).toMono()
    }

    override fun toDTO(entity: E): Mono<DTO> {
        return Mono.fromSupplier { createDTO() }
            .map { fillBasicDtoFields(it, entity) }
            .flatMap { fillOtherDtoFields(entity, it) }
    }

    protected open fun fillBasicDtoFields(dto: DTO, entity: E): DTO {
        dto.serverId = entity.id
        dto.id = entity.playniteId
        dto.name = entity.name
        dto.isRemoved = entity.isRemoved
        dto.createdAt = entity.createdAt
        dto.createdBy = entity.createdBy
        dto.modifiedAt = entity.modifiedAt
        dto.modifiedBy = entity.modifiedBy
        return dto
    }

    protected open fun fillOtherDtoFields(entity: E, dto: DTO): Mono<DTO> {
        return dto.toMono()
    }

    override fun toEntity(entity: E, dto: DIFF_DTO): Mono<DIFF_E> {
        return Mono.fromSupplier { createDiffEntity() }
            .map { fillBasicDiffEntityFields(it, entity, dto) }
            .flatMap { fillOtherDiffEntityFields(it, entity, dto) }
            .doOnNext { entity ->
                entity.isForEntireObject = dto.changedFields.contains("Id") || dto.changedFields.contains("Removed")
            }
    }

    protected open fun fillBasicDiffEntityFields(diffEntity: DIFF_E, entity: E, dto: DIFF_DTO): DIFF_E {
        diffEntity.playniteId = dto.id
        diffEntity.name = dto.name
        diffEntity.isRemoved = dto.isRemoved
        return diffEntity
    }

    protected open fun fillOtherDiffEntityFields(diffEntity: DIFF_E, entity: E, dto: DIFF_DTO): Mono<DIFF_E> {
        return diffEntity.toMono()
    }

    override fun toDiffDTO(entity: E, diffEntity: DIFF_E): Mono<DIFF_DTO> {
        return Mono.fromSupplier { createDiffDTO() }
            .pairWith(getDbModel(diffEntity))
            .map { (diffDto, dbModel) -> fillBasicDiffDtoFields(diffDto, dbModel, entity, diffEntity) }
            .flatMap { (diffDto, dbModel) ->
                fillOtherFieldsFromDiffEntity(diffDto, dbModel, entity, diffEntity)
            }
    }

    protected open fun fillBasicDiffDtoFields(
        diffDto: DIFF_DTO,
        dbModel: DB_MODEL,
        entity: E,
        diffEntity: DIFF_E
    ): Pair<DIFF_DTO, DB_MODEL> {
        val changedFields: List<String> = dbModel.changedFields
        diffDto.serverId = diffEntity.id
        diffDto.createdAt = diffEntity.createdAt
        diffDto.createdBy = diffEntity.createdBy
        if (changedFields.contains(LibraryObjectFields.ID)) {
            diffDto.id = entity.playniteId
        }
        if (changedFields.contains(LibraryObjectFields.NAME)) {
            diffDto.name = entity.name
        }
        if (changedFields.contains(LibraryObjectFields.REMOVED)) {
            diffDto.isRemoved = entity.isRemoved
        }
        diffDto.changedFields = dbModel.changedFields
        return diffDto to dbModel
    }

    protected open fun fillOtherFieldsFromDiffEntity(
        diffDTO: DIFF_DTO,
        dbModel: DB_MODEL,
        entity: E,
        diffEntity: DIFF_E
    ): Mono<DIFF_DTO> {
        return diffDTO.toMono()
    }

    override fun fillEntityAndGenerateDiff(dto: DIFF_DTO, entity: E): Mono<Pair<E, DIFF_DTO>> {
        return Mono.fromSupplier { createDiffDTO() }
            .map { diffDTO -> fillBasicFields(dto, entity, diffDTO) }
            .flatMap { (entity, diffDTO, changedFields) -> fillOtherFields(entity, dto, diffDTO, changedFields) }
            .map { t ->
                t.second.changedFields = t.third
                t.first.isChanged = t.third.isNotEmpty()
                Pair(t.first, t.second)
            }
    }

    protected open fun fillBasicFields(
        referenceDTO: DIFF_DTO,
        entity: E,
        newDTO: DIFF_DTO
    ): Triple<E, DIFF_DTO, MutableList<String>> {
        val changedFields = mutableListOf<String>()
        newDTO.id = referenceDTO.id
        if (referenceDTO.changedFields.contains(LibraryObjectFields.NAME) && !Strings.CS.equals(
                entity.name,
                referenceDTO.name
            )
        ) {
            newDTO.name = referenceDTO.name
            entity.name = referenceDTO.name
            changedFields.add(LibraryObjectFields.NAME)
        }
        newDTO.name = referenceDTO.name
        if (entity.isRemoved) {
            entity.isRemoved = false
            newDTO.isRemoved = referenceDTO.isRemoved
            changedFields.add(LibraryObjectFields.REMOVED)
        }
        return Triple(entity, newDTO, changedFields)
    }

    protected open fun fillOtherFields(
        entity: E,
        referenceDTO: DIFF_DTO,
        diffDTO: DIFF_DTO,
        changedFields: MutableList<String>
    ): Mono<Triple<E, DIFF_DTO, MutableList<String>>> {
        return Triple(entity, diffDTO, changedFields).toMono()
    }

    protected abstract fun createDTO(): DTO

    protected abstract fun createDiffDTO(): DIFF_DTO

    protected abstract fun createDiffEntity(): DIFF_E

    protected abstract fun getDbModel(entity: DIFF_E): Mono<DB_MODEL>
}