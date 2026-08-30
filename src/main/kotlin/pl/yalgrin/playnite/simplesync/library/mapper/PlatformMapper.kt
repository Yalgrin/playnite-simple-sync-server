package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.library.domain.Platform
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDatabaseModel
import reactor.core.publisher.Mono

@Component
class PlatformMapper :
    LibraryObjectWithDiffMapperImpl<Platform, PlatformDiff, PlatformDTO, PlatformDiffDTO, PlatformDiffDatabaseModel>() {

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

    override fun fillOtherDiffEntityFields(
        diffEntity: PlatformDiff,
        entity: Platform,
        dto: PlatformDiffDTO
    ): Mono<PlatformDiff> {
        return diffEntity.extractDbModelOrEmpty()
            .map { databaseModel ->
                databaseModel.baseObjectId = dto.baseObjectId
                databaseModel.changedFields = dto.changedFields
                databaseModel.specificationId = dto.specificationId
                databaseModel
            }
            .flatMap { it.asJson() }
            .doOnNext { diffEntity.diffData = it }
            .thenReturn(diffEntity)
    }

    override fun fillBasicDiffDtoFields(
        diffDto: PlatformDiffDTO,
        dbModel: PlatformDiffDatabaseModel,
        entity: Platform,
        diffEntity: PlatformDiff
    ): Pair<PlatformDiffDTO, PlatformDiffDatabaseModel> {
        val result = super.fillBasicDiffDtoFields(diffDto, dbModel, entity, diffEntity)
        if (diffDto.changedFields.contains("SpecificationId")) {
            result.first.specificationId = entity.specificationId
        }
        return result
    }

    override fun fillOtherFieldsFromDiffEntity(
        diffDTO: PlatformDiffDTO,
        dbModel: PlatformDiffDatabaseModel,
        entity: Platform,
        diffEntity: PlatformDiff
    ): Mono<PlatformDiffDTO> {
        return Mono.fromSupplier { dbModel.changedFields }
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

    override fun getDbModel(entity: PlatformDiff): Mono<PlatformDiffDatabaseModel> = entity.extractDbModelOrEmpty()
}