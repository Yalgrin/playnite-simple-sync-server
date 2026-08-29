package pl.yalgrin.playnite.simplesync.library.service

import org.apache.commons.lang3.Strings
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.config.BACKGROUND_IMAGE
import pl.yalgrin.playnite.simplesync.common.config.COVER_IMAGE
import pl.yalgrin.playnite.simplesync.common.config.GAME
import pl.yalgrin.playnite.simplesync.common.config.ICON
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.common.util.first
import pl.yalgrin.playnite.simplesync.common.util.transactional
import pl.yalgrin.playnite.simplesync.exception.ForceFetchRequiredException
import pl.yalgrin.playnite.simplesync.exception.ManualSynchronizationRequiredException
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffFields
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.mapper.GameMapper
import pl.yalgrin.playnite.simplesync.library.repository.GameDiffRepository
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Service
class GameService(
    protected val gameRepository: GameRepository,
    diffRepository: GameDiffRepository,
    mapper: GameMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    metadataService: MetadataService,
    transactionManager: ReactiveTransactionManager,
    protected val genreService: GenreService,
    protected val platformService: PlatformService,
    protected val companyService: CompanyService,
    protected val categoryService: CategoryService,
    protected val tagService: TagService,
    protected val featureService: FeatureService,
    protected val seriesService: SeriesService,
    protected val ageRatingService: AgeRatingService,
    protected val regionService: RegionService,
    protected val sourceService: SourceService,
    protected val completionStatusService: CompletionStatusService
) : BaseLibraryObjectWithDiffService<GameDTO, GameDiffDTO, Game, GameDiff>(
    gameRepository,
    diffRepository,
    mapper,
    changeService,
    changeListenerService,
    metadataService,
    transactionManager
) {

    override fun saveObjectAndPublishChanges(
        dto: GameDTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<GameDTO> {
        return saveGameWithDependencies(dto, fileParts, saveFiles)
            .transactional(transactionManager)
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    private fun saveGameWithDependencies(
        dto: GameDTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<LibrarySaveResult<GameDTO>> {
        return Flux.concat(
            saveRelatedObjects(dto, genreService, { it.genres }, { dto, list -> dto.genres = list }),
            saveRelatedObjects(dto, platformService, { it.platforms }, { dto, list -> dto.platforms = list }),
            saveRelatedObjects(dto, companyService, { it.publishers }, { dto, list -> dto.publishers = list }),
            saveRelatedObjects(dto, companyService, { it.developers }, { dto, list -> dto.developers = list }),
            saveRelatedObjects(dto, categoryService, { it.categories }, { dto, list -> dto.categories = list }),
            saveRelatedObjects(dto, tagService, { it.tags }, { dto, list -> dto.tags = list }),
            saveRelatedObjects(dto, featureService, { it.features }, { dto, list -> dto.features = list }),
            saveRelatedObjects(dto, seriesService, { it.series }, { dto, list -> dto.series = list }),
            saveRelatedObjects(dto, ageRatingService, { it.ageRatings }, { dto, list -> dto.ageRatings = list }),
            saveRelatedObjects(dto, regionService, { it.regions }, { dto, list -> dto.regions = list }),
            saveRelatedObject(dto, sourceService, { it.source }, { dto, r -> dto.source = r }),
            saveRelatedObject(
                dto,
                completionStatusService,
                { it.completionStatus },
                { dto, r -> dto.completionStatus = r })
        ).collectList()
            .map { it.flatten() }
            .flatMap { changes ->
                saveObject(dto, fileParts, saveFiles)
                    .map { LibrarySaveResult(it.savedObject, changes.plus(it.generatedChanges)) }
            }
    }

    private fun <T : LibraryObjectDTO> saveRelatedObjects(
        dto: GameDTO,
        service: LibraryObjectSaveService<T>,
        dtoGetter: (GameDTO) -> List<T>,
        dtoSetter: (GameDTO, List<T>) -> Unit,
    ): Mono<List<ChangeDTO>> {
        return Mono.fromSupplier { dtoGetter(dto) }
            .flatMapMany { Flux.fromIterable(it) }
            .concatMap { d ->
                service.saveObject(d)
                    .switchIfEmpty(Mono.fromSupplier { LibrarySaveResult(d, emptyList()) })
            }
            .collectList()
            .doOnNext { list -> dtoSetter(dto, list.map { it.savedObject }) }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { it.generatedChanges.toFlux() }
            .collectList()
    }

    private fun <T : LibraryObjectDTO> saveRelatedObject(
        dto: GameDTO,
        service: LibraryObjectSaveService<T>,
        dtoGetter: (GameDTO) -> T?,
        dtoSetter: (GameDTO, T) -> Unit,
    ): Mono<List<ChangeDTO>> {
        return Mono.fromSupplier { dtoGetter(dto) }
            .flatMap { d ->
                service.saveObject(d)
                    .switchIfEmpty(Mono.fromSupplier { LibrarySaveResult(d, emptyList()) })
            }
            .doOnNext { result -> dtoSetter(dto, result.savedObject) }
            .map { it.generatedChanges }
    }

    override fun findOrCreateEntity(dto: GameDTO): Mono<Game> {
        return Mono.justOrEmpty(dto)
            .filter { d -> d.gameId != null && d.pluginId != null }
            .flatMap { d ->
                gameRepository.findByGameIdAndPluginId(
                    d.gameId!!,
                    d.pluginId!!
                ).first()
            }
            .flatMap { e ->
                log.debug(
                    "findOrCreateEntity > found entity with id = {} by game id: {} and plugin id: {}",
                    e.id,
                    dto.gameId, dto.pluginId
                )
                if (!e.isRemoved && !Strings.CS.equals(e.playniteId, dto.id)) {
                    Mono.error(ForceFetchRequiredException("Force fetch required!"))
                } else {
                    Mono.just(e)
                }
            }
            .switchIfEmpty(
                Mono.fromSupplier { createEntityFromDTO(dto) }
                    .doOnNext { log.debug("findOrCreateEntity > creating new entity...") }
            )
    }

    override fun findOrCreateEntity(dto: GameDiffDTO): Mono<Game> {
        return Mono.justOrEmpty(dto)
            .filter { d -> d.gameId != null && d.pluginId != null }
            .flatMap { gameRepository.findByGameIdAndPluginId(dto.gameId!!, dto.pluginId!!).first() }
            .flatMap { e ->
                log.debug(
                    "findOrCreateEntity > found entity with id = {} by game id: {} and plugin id: {}",
                    e.id,
                    dto.gameId, dto.pluginId
                )
                if (e.isRemoved) {
                    return@flatMap Mono.error(
                        ManualSynchronizationRequiredException("Manual synchronization required!")
                    )
                }
                if (!Strings.CS.equals(e.playniteId, dto.id)) {
                    Mono.error<Game>(ForceFetchRequiredException("Force fetch required!"))
                } else {
                    Mono.just(e)
                }
            }
            .switchIfEmpty(
                Mono.error(ManualSynchronizationRequiredException("Manual synchronization required!"))
            )
    }

    override fun saveObjectDiffAndPublishChanges(
        diffDto: GameDiffDTO,
        fileParts: Flux<FilePart>
    ): Mono<GameDTO> {
        return saveGameDiffWithDependencies(diffDto, fileParts)
            .transactional(transactionManager)
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    private fun saveGameDiffWithDependencies(
        diffDto: GameDiffDTO,
        fileParts: Flux<FilePart>
    ): Mono<LibrarySaveResult<GameDTO>> = Flux.concat(
        saveRelatedDiffObjects(
            diffDto,
            genreService,
            GameDiffFields.GENRES,
            { it.genres },
            { dto, list -> dto.genres = list }),
        saveRelatedDiffObjects(
            diffDto,
            platformService,
            GameDiffFields.PLATFORMS,
            { it.platforms },
            { dto, list -> dto.platforms = list }),
        saveRelatedDiffObjects(
            diffDto,
            companyService,
            GameDiffFields.PUBLISHERS,
            { it.publishers },
            { dto, list -> dto.publishers = list }),
        saveRelatedDiffObjects(
            diffDto,
            companyService,
            GameDiffFields.DEVELOPERS,
            { it.developers },
            { dto, list -> dto.developers = list }),
        saveRelatedDiffObjects(
            diffDto,
            categoryService,
            GameDiffFields.CATEGORIES,
            { it.categories },
            { dto, list -> dto.categories = list }),
        saveRelatedDiffObjects(diffDto, tagService, GameDiffFields.TAGS, { it.tags }, { dto, list -> dto.tags = list }),
        saveRelatedDiffObjects(
            diffDto,
            featureService,
            GameDiffFields.FEATURES,
            { it.features },
            { dto, list -> dto.features = list }),
        saveRelatedDiffObjects(
            diffDto,
            seriesService,
            GameDiffFields.SERIES,
            { it.series },
            { dto, list -> dto.series = list }),
        saveRelatedDiffObjects(
            diffDto,
            ageRatingService,
            GameDiffFields.AGE_RATINGS,
            { it.ageRatings },
            { dto, list -> dto.ageRatings = list }),
        saveRelatedDiffObjects(
            diffDto,
            regionService,
            GameDiffFields.REGIONS,
            { it.regions },
            { dto, list -> dto.regions = list }),
        saveRelatedDiffObject(
            diffDto,
            sourceService,
            GameDiffFields.SOURCE,
            { it.source },
            { dto, r -> dto.source = r }),
        saveRelatedDiffObject(
            diffDto,
            completionStatusService,
            GameDiffFields.COMPLETION_STATUS,
            { it.completionStatus },
            { dto, r -> dto.completionStatus = r })
    ).collectList()
        .map { it.flatten() }
        .flatMap { changes ->
            saveObjectDiff(diffDto, fileParts)
                .map { LibrarySaveResult(it.savedObject, changes.plus(it.generatedChanges)) }
        }

    private fun <T : LibraryObjectDTO> saveRelatedDiffObjects(
        dto: GameDiffDTO,
        service: LibraryObjectSaveService<T>,
        fieldName: String,
        dtoGetter: (GameDiffDTO) -> List<T>,
        dtoSetter: (GameDiffDTO, List<T>) -> Unit
    ): Mono<List<ChangeDTO>> {
        return Mono.defer {
            if (!dto.changedFields.contains(fieldName)) {
                Mono.empty()
            } else {
                Mono.fromSupplier { dtoGetter(dto) }
                    .flatMapMany { Flux.fromIterable(it) }
                    .concatMap { d ->
                        service.saveObject(d).switchIfEmpty(
                            Mono.fromSupplier {
                                LibrarySaveResult(
                                    savedObject = d,
                                    generatedChanges = emptyList()
                                )
                            }
                        )
                    }.collectList()
                    .doOnNext { t ->
                        dtoSetter(dto, t.map { it.savedObject })
                    }
                    .map { results -> results.flatMap { it.generatedChanges } }
            }
        }
    }

    private fun <T : LibraryObjectDTO> saveRelatedDiffObject(
        dto: GameDiffDTO,
        service: LibraryObjectSaveService<T>,
        fieldName: String,
        dtoGetter: (GameDiffDTO) -> T?,
        dtoSetter: (GameDiffDTO, T) -> Unit
    ): Mono<List<ChangeDTO>> {
        return Mono.defer {
            if (!dto.changedFields.contains(fieldName)) {
                Mono.empty()
            } else {
                Mono.fromSupplier { dtoGetter(dto) }
                    .flatMap { d ->
                        service.saveObject(d).switchIfEmpty(
                            Mono.fromSupplier {
                                LibrarySaveResult(
                                    savedObject = d,
                                    generatedChanges = emptyList()
                                )
                            }
                        )
                    }
                    .doOnNext { t ->
                        dtoSetter(dto, t.savedObject)
                    }
                    .map { results -> results.generatedChanges }
            }
        }
    }

    override fun findObjectToDelete(dto: GameDTO): Flux<Game> {
        return Flux.defer {
            val id = dto.id
            val gameId = dto.gameId
            val pluginId = dto.pluginId
            if (id == null || gameId == null || pluginId == null) {
                Flux.empty()
            } else {
                gameRepository.findByPlayniteIdAndGameIdAndPluginIdAndRemovedIsFalse(id, gameId, pluginId)
            }
        }
    }

    override fun applyFileChange(
        entity: Game,
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
        return GAME
    }

    override fun createEntityFromDTO(dto: GameDTO): Game {
        return Game(
            playniteId = dto.id
        )
    }

    override fun getMetadataFields(): Set<String> {
        return setOf(ICON, COVER_IMAGE, BACKGROUND_IMAGE)
    }

    override fun hasFileDataChanged(
        entity: Game,
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

    override fun getObjectType(): ObjectType = ObjectType.GAME

    override fun getDiffType(): ObjectType = ObjectType.GAME_DIFF
}