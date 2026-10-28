package pl.yalgrin.playnite.simplesync.library.service

import org.apache.commons.lang3.Strings
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.config.BACKGROUND_IMAGE
import pl.yalgrin.playnite.simplesync.common.config.COVER_IMAGE
import pl.yalgrin.playnite.simplesync.common.config.ICON
import pl.yalgrin.playnite.simplesync.common.config.PLATFORM
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Platform
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO
import pl.yalgrin.playnite.simplesync.library.mapper.PlatformMapper
import pl.yalgrin.playnite.simplesync.library.repository.PlatformDiffRepository
import pl.yalgrin.playnite.simplesync.library.repository.PlatformRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelPlatformDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelPlatformMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class PlatformService(
    gameRepository: PlatformRepository,
    diffRepository: PlatformDiffRepository,
    mapper: PlatformMapper,
    panelMapper: PanelPlatformMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    metadataService: MetadataService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectWithDiffService<PlatformDTO, PlatformDiffDTO, PanelPlatformDTO, Platform, PlatformDiff>(
    gameRepository,
    diffRepository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    metadataService,
    transactionManager
) {
    override fun applyFileChange(
        entity: Platform,
        baseName: String,
        md5: String?
    ): Mono<Boolean> {
        return Mono.fromSupplier {
            var changed = false
            when (baseName) {
                ICON -> {
                    changed = entity.iconMd5 != md5
                    entity.iconMd5 = md5
                }

                COVER_IMAGE -> {
                    changed = entity.coverImageMd5 != md5
                    entity.coverImageMd5 = md5
                }

                BACKGROUND_IMAGE -> {
                    changed = entity.backgroundImageMd5 != md5
                    entity.backgroundImageMd5 = md5
                }
            }
            entity.isChanged = changed || entity.isChanged
            changed
        }
    }

    override fun getMetadataFolder(): String {
        return PLATFORM
    }

    override fun createEntityFromDTO(dto: PlatformDTO): Platform {
        return Platform(
            playniteId = dto.id
        )
    }

    override fun getMetadataFields(): Set<String> {
        return setOf(ICON, COVER_IMAGE, BACKGROUND_IMAGE)
    }

    override fun shouldSaveFile(entity: Platform, field: String): Boolean {
        var isSavingField = false
        when (field) {
            ICON -> {
                isSavingField = entity.isSavingIcon
            }

            COVER_IMAGE -> {
                isSavingField = entity.isSavingCoverImage
            }

            BACKGROUND_IMAGE -> {
                isSavingField = entity.isSavingBackgroundImage
            }
        }
        return isSavingField
    }

    override fun hasFileDataChanged(
        entity: Platform,
        bytes: ByteArray,
        md5: String,
        fieldName: String
    ): Mono<Boolean> {
        return Mono.fromSupplier {
            if (entity.id == null) {
                return@fromSupplier true
            }
            var md5ToCompare: String? = null
            when (fieldName) {
                ICON -> {
                    md5ToCompare = entity.iconMd5
                }

                COVER_IMAGE -> {
                    md5ToCompare = entity.coverImageMd5
                }

                BACKGROUND_IMAGE -> {
                    md5ToCompare = entity.backgroundImageMd5
                }
            }
            md5ToCompare == null || !Strings.CS.equals(md5ToCompare, md5)
        }
    }

    override fun getObjectType(): ObjectType = ObjectType.PLATFORM

    override fun getDiffType(): ObjectType = ObjectType.PLATFORM_DIFF

    override fun doActionsAfterSave(result: LibrarySaveResult<Platform>): Mono<LibrarySaveResult<Platform>> {
        return gameService.updatePlatforms(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Platform>>): Mono<List<ChangeDTO>> {
        return gameService.updatePlatformsAfterDelete(results)
    }

    override fun createEntityFromPanelDTO(dto: PanelPlatformDTO): Platform {
        return Platform(
            playniteId = UUID.randomUUID().toString()
        )
    }
}