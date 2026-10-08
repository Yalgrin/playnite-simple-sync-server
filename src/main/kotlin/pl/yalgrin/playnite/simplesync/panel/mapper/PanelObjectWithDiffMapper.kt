package pl.yalgrin.playnite.simplesync.panel.mapper

import org.apache.commons.lang3.Strings
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectFields
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

interface PanelObjectWithDiffMapper<E : LibraryObjectEntity, DTO : PanelObjectDTO, DIFF_DTO : LibraryObjectDiffDTO> {
    fun fillEntityAndGenerateDiff(dto: DTO, entity: E): Mono<Pair<E, DIFF_DTO>>
    fun toDTO(entity: E): Mono<DTO>
}

abstract class PanelObjectWithDiffMapperImpl<
        E : LibraryObjectEntity,
        DTO : PanelObjectDTO,
        DIFF_DTO : LibraryObjectDiffDTO> : PanelObjectWithDiffMapper<E, DTO, DIFF_DTO> {

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
        generatedDiffDTO.id = entity.playniteId
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
            generatedDiffDTO.isRemoved = false
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
        dto.id = entity.id
        dto.name = entity.name
        return dto
    }

    protected open fun fillOtherDtoFields(entity: E, dto: DTO): Mono<DTO> {
        return dto.toMono()
    }

    protected abstract fun createDTO(): DTO

    protected abstract fun createDiffDTO(): DIFF_DTO
}