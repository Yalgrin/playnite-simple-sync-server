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
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.common.util.defaultIfEmpty
import pl.yalgrin.playnite.simplesync.common.util.first
import pl.yalgrin.playnite.simplesync.common.util.isNotNullAndNotBlank
import pl.yalgrin.playnite.simplesync.exception.ForceFetchRequiredException
import pl.yalgrin.playnite.simplesync.exception.ManualSynchronizationRequiredException
import pl.yalgrin.playnite.simplesync.library.domain.*
import pl.yalgrin.playnite.simplesync.library.dto.*
import pl.yalgrin.playnite.simplesync.library.mapper.GameMapper
import pl.yalgrin.playnite.simplesync.library.repository.GameDiffRepository
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.util.clearModificationData
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGameDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelGameMapper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.*

private const val EMPTY_PLUGIN_ID = "00000000-0000-0000-0000-000000000000"

@Service
class GameService(
    protected val gameRepository: GameRepository,
    diffRepository: GameDiffRepository,
    mapper: GameMapper,
    panelMapper: PanelGameMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    metadataService: MetadataService,
    transactionManager: ReactiveTransactionManager,
    protected val libraryPluginService: LibraryPluginService,
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
) : BaseLibraryObjectWithDiffService<GameDTO, GameDiffDTO, PanelGameDTO, Game, GameDiff>(
    gameRepository,
    diffRepository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    metadataService,
    transactionManager
) {
    companion object {
        const val CTX_CURRENT_GAME_ID = "currentGameId"
        const val CTX_CURRENT_GAME_DB_ID = "currentGameDbId"
    }

    override fun saveObjectAndPublishChanges(
        dto: GameDTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<GameDTO> {
        return saveGameWithDependencies(dto, fileParts, saveFiles)
            .contextWrite { ctx -> ctx.put(CTX_CURRENT_GAME_ID, dto.gameId to dto.pluginId) }
            .transactional()
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    private fun saveGameWithDependencies(
        dto: GameDTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<LibrarySaveResult<GameDTO>> {
        return Flux.concat(
            saveRelatedObject(dto, libraryPluginService, {
                if (it.pluginId != null && it.pluginName.isNotNullAndNotBlank()) {
                    LibraryPluginDTO(
                        id = it.pluginId,
                        name = it.pluginName
                    )
                } else {
                    null
                }
            }, { dto, r -> dto.pluginName = r.name }),
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
        service: LibraryObjectSaveService<T, *>,
        dtoGetter: (GameDTO) -> List<T>,
        dtoSetter: (GameDTO, List<T>) -> Unit,
    ): Mono<List<ChangeDTO>> {
        return Mono.fromSupplier { dtoGetter(dto) }
            .flatMapMany { Flux.fromIterable(it) }
            .concatMap { d ->
                service.saveObject(d)
                    .switchIfEmpty(Mono.fromSupplier { LibrarySaveResult(d, emptyList()) })
                    .doOnNext { result ->
                        result.savedObject.clearModificationData()
                    }
            }
            .collectList()
            .doOnNext { list -> dtoSetter(dto, list.map { it.savedObject }) }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { it.generatedChanges.toFlux() }
            .collectList()
    }

    private fun <T : LibraryObjectDTO> saveRelatedObject(
        dto: GameDTO,
        service: LibraryObjectSaveService<T, *>,
        dtoGetter: (GameDTO) -> T?,
        dtoSetter: (GameDTO, T) -> Unit,
    ): Mono<List<ChangeDTO>> {
        return Mono.fromSupplier { dtoGetter(dto) }
            .flatMap { d ->
                service.saveObject(d)
                    .switchIfEmpty(Mono.fromSupplier { LibrarySaveResult(d, emptyList()) })
                    .doOnNext { result ->
                        result.savedObject.clearModificationData()
                    }
            }
            .doOnNext { result -> dtoSetter(dto, result.savedObject) }
            .map { it.generatedChanges }
    }

    override fun findOrCreateEntity(dto: GameDTO): Mono<Game> {
        //TODO handle cases where plugin id changes (for example GOG OSS migration)
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
                    "findOrCreateEntity > found entity with id = {} (name: {}) by game id: {} and plugin id: {}",
                    e.id,
                    e.name,
                    dto.gameId, dto.pluginId
                )
                if (!e.isRemoved && !Strings.CS.equals(e.playniteId, dto.id)) {
                    log.warn(
                        "findOrCreateEntity > force fetch required (isRemoved: {}, e.playniteId: {}, dto.id: {}, e.name: {})!",
                        e.isRemoved,
                        e.playniteId,
                        dto.id,
                        e.name
                    )
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
                    log.warn(
                        "findOrCreateEntity > force fetch required (e.playniteId: {}, dto.id: {})!",
                        e.playniteId,
                        dto.id
                    )
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
            .transactional()
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    private fun saveGameDiffWithDependencies(
        diffDto: GameDiffDTO,
        fileParts: Flux<FilePart>
    ): Mono<LibrarySaveResult<GameDTO>> = Flux.concat(
        saveRelatedDiffObject(
            diffDto,
            libraryPluginService,
            null,
            {
                if (it.pluginId != null && !it.pluginName.isNullOrBlank()) {
                    LibraryPluginDTO(
                        id = it.pluginId,
                        name = it.pluginName
                    )
                }
                null
            },
            { dto, r -> dto.pluginName = r.name }),
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
        service: LibraryObjectSaveService<T, *>,
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
                            .doOnNext { result ->
                                result.savedObject.clearModificationData()
                            }
                    }
                    .collectList()
                    .doOnNext { t ->
                        dtoSetter(dto, t.map { it.savedObject })
                    }
                    .map { results -> results.flatMap { it.generatedChanges } }
            }
        }
    }

    private fun <T : LibraryObjectDTO> saveRelatedDiffObject(
        dto: GameDiffDTO,
        service: LibraryObjectSaveService<T, *>,
        fieldName: String?,
        dtoGetter: (GameDiffDTO) -> T?,
        dtoSetter: (GameDiffDTO, T) -> Unit
    ): Mono<List<ChangeDTO>> {
        return Mono.defer {
            if (fieldName != null && !dto.changedFields.contains(fieldName)) {
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
                            .doOnNext { result ->
                                result.savedObject.clearModificationData()
                            }
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

    override fun saveObjectAndPublishChanges(
        dto: PanelGameDTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<PanelGameDTO> {
        return saveGameWithDependencies(dto, fileParts, saveFiles)
            .contextWrite { ctx ->
                val id = dto.id
                if (id != null) {
                    ctx.put(CTX_CURRENT_GAME_DB_ID, id)
                }
                ctx
            }
            .transactional()
            .flatMap { t -> changeListenerService.publishChanges(t.generatedChanges).thenReturn(t.savedObject) }
    }

    private fun saveGameWithDependencies(
        dto: PanelGameDTO,
        fileParts: Flux<FilePart>,
        saveFiles: Boolean
    ): Mono<LibrarySaveResult<PanelGameDTO>> {
        return Flux.concat(
            Mono.defer {
                val id = dto.id
                if (id == null) {
                    libraryPluginService.findByPlayniteIdForPanel(EMPTY_PLUGIN_ID)
                        .doOnNext { dto.plugin = it }
                        .map { emptyList<ChangeDTO>() }
                        .switchIfEmpty {
                            libraryPluginService.saveObject(LibraryPluginDTO(id = EMPTY_PLUGIN_ID, name = "Playnite"))
                                .flatMap { saved ->
                                    libraryPluginService.findByPlayniteIdForPanel(EMPTY_PLUGIN_ID)
                                        .doOnNext { dto.plugin = it }
                                        .thenReturn(saved.generatedChanges)
                                }
                        }
                } else {
                    dto.plugin?.id.toMono().flatMap { id -> libraryPluginService.findByIdForPanel(id) }
                        .doOnNext { dto.plugin = it }.thenReturn(emptyList())
                }
            },
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

    private fun <T : PanelObjectDTO> saveRelatedObjects(
        dto: PanelGameDTO,
        service: LibraryObjectSaveService<*, T>,
        dtoGetter: (PanelGameDTO) -> List<T>,
        dtoSetter: (PanelGameDTO, List<T>) -> Unit,
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

    private fun <T : PanelObjectDTO> saveRelatedObject(
        dto: PanelGameDTO,
        service: LibraryObjectSaveService<*, T>,
        dtoGetter: (PanelGameDTO) -> T?,
        dtoSetter: (PanelGameDTO, T) -> Unit,
    ): Mono<List<ChangeDTO>> {
        return Mono.fromSupplier { dtoGetter(dto) }
            .flatMap { d ->
                service.saveObject(d)
                    .switchIfEmpty(Mono.fromSupplier { LibrarySaveResult(d, emptyList()) })
            }
            .doOnNext { result -> dtoSetter(dto, result.savedObject) }
            .map { it.generatedChanges }
    }

    override fun createEntityFromPanelDTO(dto: PanelGameDTO): Game {
        return Game(
            playniteId = UUID.randomUUID().toString(),
            gameId = UUID.randomUUID().toString(),
            pluginId = EMPTY_PLUGIN_ID
        )
    }

    override fun getMetadataFields(): Set<String> {
        return setOf(ICON, COVER_IMAGE, BACKGROUND_IMAGE)
    }

    override fun shouldSaveFile(entity: Game, field: String): Boolean {
        var isSavingField = false
        when (field) {
            ICON -> {
                isSavingField = entity.isSavingIcon
            }

            COVER_IMAGE -> {
                isSavingField = entity.isSavingCoverImage
            }

            BACKGROUND_IMAGE -> {
                isSavingField = entity.isSavingBackground
            }
        }
        return isSavingField
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

    fun updateLibraryPlugins(saveResult: LibrarySaveResult<LibraryPlugin>): Mono<LibrarySaveResult<LibraryPlugin>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByLibraryPluginIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                if (savedObject.playniteId == dbModel.pluginId && savedObject.name != dbModel.pluginName) {
                    dbModel.pluginName = savedObject.name
                    gameDiffDTO.pluginName = savedObject.name
                    gameDiffDTO.changedFields = mutableListOf(GameFields.PLUGIN_NAME)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                } else {
                    null
                }
            })
    }

    fun updateGenres(saveResult: LibrarySaveResult<Genre>): Mono<LibrarySaveResult<Genre>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByGenreIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.genres.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        GenreDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.genres = changedObjects
                    gameDiffDTO.genres = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.GENRES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateGenresAfterDelete(results: List<LibrarySaveResult<Genre>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByGenreIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.genres.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.genres = changedObjects
                    gameDiffDTO.genres = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.GENRES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updatePlatforms(saveResult: LibrarySaveResult<Platform>): Mono<LibrarySaveResult<Platform>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByPlatformIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.platforms.map { subObject ->
                    if (subObject.id == savedObject.playniteId && (subObject.name != savedObject.name || subObject.specificationId != savedObject.specificationId)) {
                        changed = true
                        PlatformDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name,
                            specificationId = savedObject.specificationId
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.platforms = changedObjects
                    gameDiffDTO.platforms = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.PLATFORMS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updatePlatformsAfterDelete(results: List<LibrarySaveResult<Platform>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByPlatformIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.platforms.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.platforms = changedObjects
                    gameDiffDTO.platforms = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.PLATFORMS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateCompanies(saveResult: LibrarySaveResult<Company>): Mono<LibrarySaveResult<Company>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByCompanyIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changedAnyPublishers = false
                var changedAnyDevelopers = false
                val changedPublishers = dbModel.publishers.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changedAnyPublishers = true
                        CompanyDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                val changedDevelopers = dbModel.developers.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changedAnyDevelopers = true
                        CompanyDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                if (!changedAnyPublishers && !changedAnyDevelopers) {
                    null
                } else {
                    val changedFields = mutableListOf<String>()
                    if (changedAnyPublishers) {
                        dbModel.publishers = changedPublishers
                        gameDiffDTO.publishers = changedPublishers
                        changedFields.add(GameFields.PUBLISHERS)
                    }
                    if (changedAnyDevelopers) {
                        dbModel.developers = changedDevelopers
                        gameDiffDTO.developers = changedDevelopers
                        changedFields.add(GameFields.DEVELOPERS)
                    }
                    gameDiffDTO.changedFields = changedFields
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateCompaniesAfterDelete(results: List<LibrarySaveResult<Company>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByCompanyIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changedAnyPublishers = false
                var changedAnyDevelopers = false
                val changedPublishers = dbModel.publishers.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changedAnyPublishers = true
                        null
                    } else {
                        subObject
                    }
                }
                val changedDevelopers = dbModel.developers.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changedAnyDevelopers = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changedAnyPublishers && !changedAnyDevelopers) {
                    null
                } else {
                    val changedFields = mutableListOf<String>()
                    if (changedAnyPublishers) {
                        dbModel.publishers = changedPublishers
                        gameDiffDTO.publishers = changedPublishers
                        changedFields.add(GameFields.PUBLISHERS)
                    }
                    if (changedAnyDevelopers) {
                        dbModel.developers = changedDevelopers
                        gameDiffDTO.developers = changedDevelopers
                        changedFields.add(GameFields.DEVELOPERS)
                    }
                    gameDiffDTO.changedFields = changedFields
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateCategories(saveResult: LibrarySaveResult<Category>): Mono<LibrarySaveResult<Category>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByCategoryIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.categories.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        CategoryDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.categories = changedObjects
                    gameDiffDTO.categories = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.CATEGORIES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateCategoriesAfterDelete(results: List<LibrarySaveResult<Category>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByCategoryIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.categories.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.categories = changedObjects
                    gameDiffDTO.categories = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.CATEGORIES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateTags(saveResult: LibrarySaveResult<Tag>): Mono<LibrarySaveResult<Tag>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByTagIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.tags.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        TagDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.tags = changedObjects
                    gameDiffDTO.tags = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.TAGS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateTagsAfterDelete(results: List<LibrarySaveResult<Tag>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByTagIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.tags.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.tags = changedObjects
                    gameDiffDTO.tags = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.TAGS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateFeatures(saveResult: LibrarySaveResult<Feature>): Mono<LibrarySaveResult<Feature>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByFeatureIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.features.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        FeatureDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.features = changedObjects
                    gameDiffDTO.features = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.FEATURES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateFeaturesAfterDelete(results: List<LibrarySaveResult<Feature>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByFeatureIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.features.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.features = changedObjects
                    gameDiffDTO.features = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.FEATURES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateSeries(saveResult: LibrarySaveResult<Series>): Mono<LibrarySaveResult<Series>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findBySeriesIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.series.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        SeriesDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.series = changedObjects
                    gameDiffDTO.series = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.SERIES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateSeriesAfterDelete(results: List<LibrarySaveResult<Series>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findBySeriesIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.series.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.series = changedObjects
                    gameDiffDTO.series = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.SERIES)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateAgeRatings(saveResult: LibrarySaveResult<AgeRating>): Mono<LibrarySaveResult<AgeRating>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByAgeRatingIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.ageRatings.map { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        AgeRatingDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.ageRatings = changedObjects
                    gameDiffDTO.ageRatings = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.AGE_RATINGS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateAgeRatingsAfterDelete(results: List<LibrarySaveResult<AgeRating>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByAgeRatingIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.ageRatings.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.ageRatings = changedObjects
                    gameDiffDTO.ageRatings = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.AGE_RATINGS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateRegions(saveResult: LibrarySaveResult<Region>): Mono<LibrarySaveResult<Region>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByRegionIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.regions.map { subObject ->
                    if (subObject.id == savedObject.playniteId && (subObject.name != savedObject.name || subObject.specificationId != savedObject.specificationId)) {
                        changed = true
                        RegionDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name,
                            specificationId = savedObject.specificationId
                        )
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.regions = changedObjects
                    gameDiffDTO.regions = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.REGIONS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateRegionsAfterDelete(results: List<LibrarySaveResult<Region>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByRegionIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObjects = dbModel.regions.mapNotNull { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                        null
                    } else {
                        subObject
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.regions = changedObjects
                    gameDiffDTO.regions = changedObjects
                    gameDiffDTO.changedFields = mutableListOf(GameFields.REGIONS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateSources(saveResult: LibrarySaveResult<Source>): Mono<LibrarySaveResult<Source>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findBySourceIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObject = dbModel.source?.let { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        SourceDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        null
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.source = changedObject
                    gameDiffDTO.source = changedObject
                    gameDiffDTO.changedFields = mutableListOf(GameFields.SOURCE)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateSourcesAfterDelete(results: List<LibrarySaveResult<Source>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findBySourceIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                dbModel.source?.let { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.source = null
                    gameDiffDTO.source = null
                    gameDiffDTO.changedFields = mutableListOf(GameFields.SOURCE)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateCompletionStatus(saveResult: LibrarySaveResult<CompletionStatus>): Mono<LibrarySaveResult<CompletionStatus>> {
        return propagateReferenceUpdate(
            saveResult,
            { result ->
                Mono.fromSupplier { result.savedObject.playniteId }
                    .flatMapMany { gameRepository.findByCompletionStatusIds(listOf(it)) }
            },
            { game, dbModel, savedObject ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                val changedObject = dbModel.completionStatus?.let { subObject ->
                    if (subObject.id == savedObject.playniteId && subObject.name != savedObject.name) {
                        changed = true
                        CompletionStatusDTO(
                            serverId = savedObject.id,
                            id = savedObject.playniteId,
                            name = savedObject.name
                        )
                    } else {
                        null
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.completionStatus = changedObject
                    gameDiffDTO.completionStatus = changedObject
                    gameDiffDTO.changedFields = mutableListOf(GameFields.COMPLETION_STATUS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    fun updateCompletionStatusesAfterDelete(results: List<LibrarySaveResult<CompletionStatus>>): Mono<List<ChangeDTO>> {
        return propagateReferenceDelete(
            results,
            { ids -> gameRepository.findByCompletionStatusIds(ids) },
            { game, dbModel, ids ->
                val gameDiffDTO = createDiffDTO(game)
                var changed = false
                dbModel.completionStatus?.let { subObject ->
                    if (ids.contains(subObject.id)) {
                        changed = true
                    }
                }
                if (!changed) {
                    null
                } else {
                    dbModel.completionStatus = null
                    gameDiffDTO.completionStatus = null
                    gameDiffDTO.changedFields = mutableListOf(GameFields.COMPLETION_STATUS)
                    game.isChanged = true
                    dbModel to gameDiffDTO
                }
            })
    }

    private fun createDiffDTO(game: Game): GameDiffDTO = GameDiffDTO(
        id = game.playniteId,
        name = game.name,
        baseObjectId = game.id
    )

    //TODO test everything here

    private fun <T> propagateReferenceUpdate(
        saveResult: LibrarySaveResult<T>,
        findGames: (LibrarySaveResult<T>) -> Flux<Game>,
        updateReferences: (Game, GameDatabaseModel, T) -> Pair<GameDatabaseModel, GameDiffDTO>?
    ): Mono<LibrarySaveResult<T>> {
        return Mono.fromSupplier { saveResult }
            .filter { it.generatedChanges.isNotEmpty() }
            .flatMap { result ->
                Mono.deferContextual { ctx ->
                    val currentlyEditedGame = ctx.getOrEmpty<Pair<String?, String?>>(CTX_CURRENT_GAME_ID).orElse(null)
                    val currentlyEditedGameDbId = ctx.getOrEmpty<Long>(CTX_CURRENT_GAME_DB_ID).orElse(null)
                    if (currentlyEditedGame?.first != null && currentlyEditedGame.second != null) {
                        findGames(result)
                            .filter { it.gameId != currentlyEditedGame.first || it.pluginId != currentlyEditedGame.second }
                            .collectList()
                    } else if (currentlyEditedGameDbId != null) {
                        findGames(result)
                            .filter { it.id != currentlyEditedGameDbId }
                            .collectList()
                    } else {
                        findGames(result)
                            .collectList()
                    }
                }
                    .doOnNext { games ->
                        log.debug(
                            "propagateReferenceUpdate > number of games found to change: {}",
                            games.size
                        )
                    }
                    .flatMapMany { games -> games.toFlux() }
                    .concatMap { game ->
                        game.extractDbModelOrEmpty()
                            .mapNotNull { dbModel ->
                                updateReferences(game, dbModel, result.savedObject)
                            }.flatMap { (dbModel, diffDTO) ->
                                dbModel.asJson()
                                    .doOnNext { game.savedData = it }
                                    .map { game to diffDTO }
                            }
                            .flatMap { saveEntityWithDiff(it, Flux.empty(), false) }
                    }
                    .collectList()
                    .map { parentResults ->
                        LibrarySaveResult(
                            result.savedObject,
                            result.generatedChanges + parentResults.flatMap { it.generatedChanges }
                        )
                    }
            }
            .defaultIfEmpty { saveResult }
    }

    fun <T : LibraryObjectEntity> propagateReferenceDelete(
        results: List<LibrarySaveResult<T>>,
        findGames: (List<String>) -> Flux<Game>,
        updateReferences: (Game, GameDatabaseModel, List<String>) -> Pair<GameDatabaseModel, GameDiffDTO>?
    ): Mono<List<ChangeDTO>> {
        return Mono.fromSupplier { results }
            .flatMapMany { it.toFlux() }
            .filter { it.savedObject.playniteId != null && it.generatedChanges.isNotEmpty() }
            .collectList()
            .flatMap { resultList ->
                Mono.fromSupplier { resultList.mapNotNull { it.savedObject.playniteId } }
                    .filter { it.isNotEmpty() }
                    .flatMapMany { ids ->
                        findGames(ids)
                            .collectList()
                            .doOnNext { games ->
                                log.debug(
                                    "propagateReferenceDelete > number of games found to change: {}",
                                    games.size
                                )
                            }
                            .flatMapMany { it.toFlux() }
                            .concatMap { game ->
                                game.extractDbModelOrEmpty()
                                    .mapNotNull { dbModel ->
                                        updateReferences(game, dbModel, ids)
                                    }.flatMap { (dbModel, diffDTO) ->
                                        dbModel.asJson()
                                            .doOnNext { game.savedData = it }
                                            .map { game to diffDTO }
                                    }
                                    .flatMap { saveEntityWithDiff(it, Flux.empty(), false) }
                            }
                    }
                    .map { it.generatedChanges }
                    .flatMap { it.toFlux() }
                    .collectList()
                    .map { newChanges -> resultList.flatMap { it.generatedChanges }.plus(newChanges) }
                    .defaultIfEmpty { resultList.flatMap { it.generatedChanges } }
            }
    }
}