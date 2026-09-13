package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPluginDiff
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDatabaseModel
import reactor.core.publisher.Mono

@Component
class LibraryPluginMapper :
    LibraryObjectWithDiffMapperImpl<LibraryPlugin, LibraryPluginDiff, LibraryPluginDTO, LibraryPluginDiffDTO, LibraryPluginDiffDatabaseModel>() {

    override fun fillBasicDtoFields(dto: LibraryPluginDTO, entity: LibraryPlugin): LibraryPluginDTO {
        val result = super.fillBasicDtoFields(dto, entity)
        result.hasIcon = entity.iconMd5 != null
        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        return result
    }

    override fun fillOtherDiffEntityFields(
        diffEntity: LibraryPluginDiff,
        entity: LibraryPlugin,
        dto: LibraryPluginDiffDTO
    ): Mono<LibraryPluginDiff> {
        return diffEntity.extractDbModelOrEmpty()
            .map { databaseModel ->
                databaseModel.baseObjectId = dto.baseObjectId
                databaseModel.changedFields = dto.changedFields
                databaseModel
            }
            .flatMap { it.asJson() }
            .doOnNext { diffEntity.diffData = it }
            .thenReturn(diffEntity)
    }

    override fun createDTO(): LibraryPluginDTO = LibraryPluginDTO()

    override fun createDiffDTO(): LibraryPluginDiffDTO = LibraryPluginDiffDTO()

    override fun createDiffEntity(): LibraryPluginDiff = LibraryPluginDiff()

    override fun getDbModel(entity: LibraryPluginDiff): Mono<LibraryPluginDiffDatabaseModel> =
        entity.extractDbModelOrEmpty()
}