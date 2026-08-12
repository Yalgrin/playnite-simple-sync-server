package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.common.util.asObject
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectDiffEntity
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectFields
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class LibraryObjectWithDiffMapper<
        E : LibraryObjectEntity,
        DIFF_E : LibraryObjectDiffEntity,
        DTO : LibraryObjectDTO,
        DIFF_DTO : LibraryObjectDiffDTO> {

    open fun fillEntityAndGenerateDiff(dto: DTO, entity: E): Mono<Pair<E, DIFF_DTO>> {
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
        diffDTO: DIFF_DTO
    ): Triple<E, DIFF_DTO, MutableList<String>> {
        val changedFields = mutableListOf<String>()
        diffDTO.id = dto.id
        diffDTO.name = dto.name
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
            diffDTO.isRemoved = dto.isRemoved
            changedFields.add(LibraryObjectFields.REMOVED)
        }
        return Triple(entity, diffDTO, changedFields)
    }

    protected open fun fillOtherFields(
        entity: E,
        dto: DTO,
        diffDTO: DIFF_DTO,
        changedFields: MutableList<String>
    ): Mono<Triple<E, DIFF_DTO, MutableList<String>>> {
        return Triple(entity, diffDTO, changedFields).toMono()
    }

    fun toDTO(entity: E): Mono<DTO> {
        return Mono.fromSupplier { createDTO() }
            .map { fillBasicDtoFields(it, entity) }
            .flatMap { fillOtherDtoFields(entity, it) }
    }

    protected open fun fillBasicDtoFields(dto: DTO, entity: E): DTO {
        dto.id = entity.playniteId
        dto.name = entity.name
        dto.isRemoved = entity.isRemoved
        return dto
    }

    protected open fun fillOtherDtoFields(entity: E, dto: DTO): Mono<DTO> {
        return dto.toMono()
    }

    fun toEntity(dto: DIFF_DTO): Mono<DIFF_E> {
        return Mono.fromSupplier { createDiffEntity() }
            .map { fillBasicDiffEntityFields(it, dto) }
            .flatMap { fillOtherDiffEntityFields(it, dto) }
            .flatMap { entity ->
                entity.isForEntireObject = dto.changedFields.contains("Id") || dto.changedFields.contains("Removed")
                dto.asJson()
                    .doOnNext { entity.diffData = it }
                    .thenReturn(entity)
            }
    }

    protected open fun fillBasicDiffEntityFields(diffEntity: DIFF_E, dto: DIFF_DTO): DIFF_E {
        diffEntity.playniteId = dto.id
        diffEntity.name = dto.name
        diffEntity.isRemoved = dto.isRemoved
        return diffEntity
    }

    protected open fun fillOtherDiffEntityFields(entity: DIFF_E, dto: DIFF_DTO): Mono<DIFF_E> {
        return entity.toMono()
    }

    fun toDiffDTO(entity: E, diffEntity: DIFF_E): Mono<DIFF_DTO> {
        return diffEntity.diffData.asObject(getDiffClass())
            .switchIfEmpty(Mono.fromSupplier { createDiffDTO() })
            .map { diffDto -> fillBasicDiffDtoFields(diffDto, entity) }
            .flatMap { diffDto ->
                fillOtherFieldsFromDiffEntity(diffDto, entity, diffEntity)
            }
    }

    protected open fun fillBasicDiffDtoFields(diffDto: DIFF_DTO, entity: E): DIFF_DTO {
        val changedFields: List<String> = diffDto.changedFields
        if (changedFields.contains(LibraryObjectFields.ID)) {
            diffDto.id = entity.playniteId
        }
        if (changedFields.contains(LibraryObjectFields.NAME)) {
            diffDto.name = entity.name
        }
        if (changedFields.contains(LibraryObjectFields.REMOVED)) {
            diffDto.isRemoved = entity.isRemoved
        }
        return diffDto
    }

    protected open fun fillOtherFieldsFromDiffEntity(
        diffDTO: DIFF_DTO,
        entity: E,
        diffEntity: DIFF_E
    ): Mono<DIFF_DTO> {
        return diffDTO.toMono()
    }

    fun fillEntityAndGenerateDiff(dto: DIFF_DTO, entity: E): Mono<Pair<E, DIFF_DTO>> {
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
        newDTO.name = entity.name
        if (changedFields.contains(LibraryObjectFields.NAME) && !Strings.CS.equals(entity.name, referenceDTO.name)) {
            newDTO.name = referenceDTO.name
            entity.name = referenceDTO.name
            changedFields.add(LibraryObjectFields.NAME)
        }
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

    protected abstract fun getDiffClass(): Class<DIFF_DTO>
}