package pl.yalgrin.playnite.simplesync.library.service

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.config.BACKGROUND_IMAGE
import pl.yalgrin.playnite.simplesync.common.config.ICON
import pl.yalgrin.playnite.simplesync.common.config.PLUGIN
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPluginDiff
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDTO
import pl.yalgrin.playnite.simplesync.library.mapper.LibraryPluginMapper
import pl.yalgrin.playnite.simplesync.library.repository.LibraryPluginDiffRepository
import pl.yalgrin.playnite.simplesync.library.repository.LibraryPluginRepository
import reactor.core.publisher.Mono

@Service
class LibraryPluginService(
    gameRepository: LibraryPluginRepository,
    diffRepository: LibraryPluginDiffRepository,
    mapper: LibraryPluginMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    metadataService: MetadataService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectWithDiffService<LibraryPluginDTO, LibraryPluginDiffDTO, LibraryPlugin, LibraryPluginDiff>(
    gameRepository,
    diffRepository,
    mapper,
    changeService,
    changeListenerService,
    metadataService,
    transactionManager
) {
    override fun applyFileChange(
        entity: LibraryPlugin,
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
        return PLUGIN
    }

    override fun createEntityFromDTO(dto: LibraryPluginDTO): LibraryPlugin {
        return LibraryPlugin(
            playniteId = dto.id
        )
    }

    override fun getMetadataFields(): Set<String> {
        return setOf(ICON, BACKGROUND_IMAGE)
    }

    override fun hasFileDataChanged(
        entity: LibraryPlugin,
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

                BACKGROUND_IMAGE -> {
                    md5ToCompare = entity.backgroundImageMd5
                }
            }
            md5ToCompare == null || !Strings.CS.equals(md5ToCompare, md5)
        }
    }

    override fun getObjectType(): ObjectType = ObjectType.LIBRARY_PLUGIN

    override fun getDiffType(): ObjectType = ObjectType.LIBRARY_PLUGIN_DIFF
}