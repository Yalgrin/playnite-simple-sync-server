package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Platform
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO

@Component
class PlatformMapper : LibraryObjectWithDiffMapper<Platform, PlatformDiff, PlatformDTO, PlatformDiffDTO>() {

    override fun fillBasicFields(
        dto: PlatformDTO,
        entity: Platform,
        diffDTO: PlatformDiffDTO
    ): Triple<Platform, PlatformDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, diffDTO)
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

    override fun fillBasicDiffDtoFields(diffDto: PlatformDiffDTO, entity: Platform): PlatformDiffDTO {
        val result = super.fillBasicDiffDtoFields(diffDto, entity)
        if (diffDto.changedFields.contains("SpecificationId")) {
            result.specificationId = entity.specificationId
        }
        return result
    }

    override fun createDTO(): PlatformDTO = PlatformDTO()

    override fun createDiffDTO(): PlatformDiffDTO = PlatformDiffDTO()

    override fun createDiffEntity(): PlatformDiff = PlatformDiff()

    override fun getDiffClass(): Class<PlatformDiffDTO> = PlatformDiffDTO::class.java
}