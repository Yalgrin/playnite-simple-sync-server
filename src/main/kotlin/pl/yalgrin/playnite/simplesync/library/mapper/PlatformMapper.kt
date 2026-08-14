package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.library.domain.Platform
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO
import reactor.core.publisher.Mono

@Component
class PlatformMapper : LibraryObjectWithDiffMapperImpl<Platform, PlatformDiff, PlatformDTO, PlatformDiffDTO>() {

    override fun fillBasicFields(
        dto: PlatformDTO,
        entity: Platform,
        generatedDiffDTO: PlatformDiffDTO
    ): Triple<Platform, PlatformDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, generatedDiffDTO)
        if (!Strings.CS.equals(entity.specificationId, dto.specificationId)) {
            result.first.specificationId = dto.specificationId
            result.second.specificationId = dto.specificationId
            result.third.add("SpecificationId")
        }
        return result
    }

    override fun fillBasicFields(
        referenceDTO: PlatformDiffDTO,
        entity: Platform,
        newDTO: PlatformDiffDTO
    ): Triple<Platform, PlatformDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(referenceDTO, entity, newDTO)
        val changedFields = referenceDTO.changedFields
        if (changedFields.isEmpty()) {
            return result
        }

        if (changedFields.contains("SpecificationId") && !Strings.CS.equals(
                entity.specificationId,
                referenceDTO.specificationId
            )
        ) {
            result.first.specificationId = referenceDTO.specificationId
            result.second.specificationId = referenceDTO.specificationId
            result.third.add("SpecificationId")
        }
        return result
    }

    override fun fillBasicDtoFields(dto: PlatformDTO, entity: Platform): PlatformDTO {
        val result = super.fillBasicDtoFields(dto, entity)
        result.specificationId = entity.specificationId
        result.hasIcon = entity.iconMd5 != null
        result.hasCoverImage = entity.coverImageMd5 != null
        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        return result
    }

    override fun fillOtherDiffEntityFields(entity: PlatformDiff, dto: PlatformDiffDTO): Mono<PlatformDiff> {
        return entity.extractDbModelOrEmpty()
            .map { databaseModel ->
                databaseModel.baseObjectId = dto.baseObjectId
                databaseModel.changedFields = dto.changedFields
                databaseModel.specificationId = dto.specificationId
            }
            .flatMap { it.asJson() }
            .doOnNext { entity.diffData = it }
            .thenReturn(entity)
    }

    override fun fillBasicDiffDtoFields(diffDto: PlatformDiffDTO, entity: Platform): PlatformDiffDTO {
        val result = super.fillBasicDiffDtoFields(diffDto, entity)
        if (diffDto.changedFields.contains("SpecificationId")) {
            result.specificationId = entity.specificationId
        }
        return result
    }

    override fun fillOtherFieldsFromDiffEntity(
        diffDTO: PlatformDiffDTO,
        entity: Platform,
        diffEntity: PlatformDiff
    ): Mono<PlatformDiffDTO> {
        return diffEntity.extractDbModelOrEmpty().map { it.changedFields }.defaultIfEmpty(emptyList())
            .doOnNext { changedFields ->
                if (changedFields.contains("SpecificationId")) {
                    diffDTO.specificationId = entity.specificationId
                }
            }
            .thenReturn(diffDTO)
    }

    override fun createDTO(): PlatformDTO = PlatformDTO()

    override fun createDiffDTO(): PlatformDiffDTO = PlatformDiffDTO()

    override fun createDiffEntity(): PlatformDiff = PlatformDiff()
}