package pl.yalgrin.playnite.simplesync.panel.mapper

import io.vavr.Tuple
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.*
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.GameDatabaseModel
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameFields
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.enums.toPlaytimeCategory
import pl.yalgrin.playnite.simplesync.library.enums.toScoreGroup
import pl.yalgrin.playnite.simplesync.library.service.*
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGameDTO
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.ZonedDateTime

@Component
class PanelGameMapper(
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
) : PanelObjectWithDiffMapperImpl<Game, PanelGameDTO, GameDiffDTO>() {
    override fun fillBasicFields(
        dto: PanelGameDTO,
        entity: Game,
        generatedDiffDTO: GameDiffDTO
    ): Triple<Game, GameDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, generatedDiffDTO)
        val changedEntity = result.first
        val changedDiff = result.second
        val changedFields = result.third

        if (entity.id == null) {
            changedFields.add(GameFields.GAME_ID)
            changedFields.add(GameFields.PLUGIN_ID)
        }
        changedDiff.gameId = changedEntity.gameId
        changedDiff.pluginId = changedEntity.pluginId
        changedEntity.isSavingIcon = dto.isSavingIcon
        changedEntity.isSavingCoverImage = dto.isSavingCoverImage
        changedEntity.isSavingBackground = dto.isSavingBackground
        return result
    }

    override fun fillOtherFields(
        entity: Game,
        dto: PanelGameDTO,
        generatedDiffDTO: GameDiffDTO,
        changedFields: MutableList<String>
    ): Mono<Triple<Game, GameDiffDTO, MutableList<String>>> {
        return entity.extractDbModelOrEmpty()
            .map { databaseModel ->
                val lastActivity = dto.lastActivity?.normalize()
                val now = currentZonedDateTime()
                val added: ZonedDateTime
                val modified: ZonedDateTime
                if (dto.added != null) {
                    added = dto.added!!.normalize()
                } else if (entity.id == null) {
                    added = now
                } else if (databaseModel.added != null) {
                    added = databaseModel.added!!.normalize()
                } else {
                    added = now
                }
                if (dto.modified != null) {
                    modified = dto.modified!!.normalize()
                } else if (databaseModel.modified != null) {
                    modified = databaseModel.modified!!.normalize()
                } else {
                    modified = now
                }

                if (MapperUtil.hasChanged(databaseModel.description, dto.description)) {
                    generatedDiffDTO.description = dto.description
                    databaseModel.description = dto.description
                    changedFields.add(GameFields.DESCRIPTION)
                }
                if (MapperUtil.hasChanged(databaseModel.notes, dto.notes)) {
                    generatedDiffDTO.notes = dto.notes
                    databaseModel.notes = dto.notes
                    changedFields.add(GameFields.NOTES)
                }
                if (MapperUtil.hasChanged(databaseModel.isHidden, dto.isHidden)) {
                    generatedDiffDTO.isHidden = dto.isHidden
                    databaseModel.isHidden = dto.isHidden
                    changedFields.add(GameFields.HIDDEN)
                }
                if (MapperUtil.hasChanged(databaseModel.isFavorite, dto.isFavorite)) {
                    generatedDiffDTO.isFavorite = dto.isFavorite
                    databaseModel.isFavorite = dto.isFavorite
                    changedFields.add(GameFields.FAVORITE)
                }
                if (MapperUtil.hasChanged(databaseModel.lastActivity, lastActivity)) {
                    generatedDiffDTO.lastActivity = lastActivity
                    databaseModel.lastActivity = lastActivity
                    databaseModel.lastActivityInMs = databaseModel.lastActivity?.toInstant()?.toEpochMilli()
                    databaseModel.recentActivity = lastActivity?.max(databaseModel.added)
                    databaseModel.recentActivityInMs = databaseModel.recentActivity?.toInstant()?.toEpochMilli()
                    changedFields.add(GameFields.LAST_ACTIVITY)
                }
                if (MapperUtil.hasChanged(databaseModel.sortingName, dto.sortingName)) {
                    generatedDiffDTO.sortingName = dto.sortingName
                    databaseModel.sortingName = dto.sortingName
                    changedFields.add(GameFields.SORTING_NAME)
                }
                if (MapperUtil.hasChanged(databaseModel.releaseDate, dto.releaseDate)) {
                    generatedDiffDTO.releaseDate = dto.releaseDate
                    databaseModel.releaseDate = dto.releaseDate
                    databaseModel.releaseYear = dto.releaseDate?.year
                    changedFields.add(GameFields.RELEASE_DATE)
                }
                if (MapperUtil.haveLinksChanged(databaseModel.links, dto.links)) {
                    generatedDiffDTO.links = dto.links
                    databaseModel.links = dto.links
                    changedFields.add(GameFields.LINKS)
                }
                if (MapperUtil.hasChanged(databaseModel.playtime, dto.playtime)) {
                    generatedDiffDTO.playtime = dto.playtime
                    databaseModel.playtime = dto.playtime
                    databaseModel.playtimeCategory = dto.playtime.toPlaytimeCategory()
                    changedFields.add(GameFields.PLAYTIME)
                }
                if (MapperUtil.hasChanged(databaseModel.added, added)) {
                    generatedDiffDTO.added = added
                    databaseModel.added = added
                    databaseModel.addedInMs = databaseModel.added?.toInstant()?.toEpochMilli()
                    databaseModel.recentActivity = lastActivity?.max(databaseModel.added)
                    databaseModel.recentActivityInMs = databaseModel.recentActivity?.toInstant()?.toEpochMilli()
                    changedFields.add(GameFields.ADDED)
                }
                if (MapperUtil.hasChanged(databaseModel.modified, modified)) {
                    generatedDiffDTO.modified = modified
                    databaseModel.modified = modified
                    databaseModel.modifiedInMs = databaseModel.modified?.toInstant()?.toEpochMilli()
                    changedFields.add(GameFields.MODIFIED)
                }
                if (MapperUtil.hasChanged(databaseModel.playCount, dto.playCount)) {
                    generatedDiffDTO.playCount = dto.playCount
                    databaseModel.playCount = dto.playCount
                    changedFields.add(GameFields.PLAY_COUNT)
                }
                if (MapperUtil.hasChanged(databaseModel.version, dto.version)) {
                    generatedDiffDTO.version = dto.version
                    databaseModel.version = dto.version
                    changedFields.add(GameFields.VERSION)
                }
                if (MapperUtil.hasChanged(databaseModel.userScore, dto.userScore)) {
                    generatedDiffDTO.userScore = dto.userScore
                    databaseModel.userScore = dto.userScore
                    databaseModel.userScoreGroup = dto.userScore.toScoreGroup()
                    changedFields.add(GameFields.USER_SCORE)
                }
                if (MapperUtil.hasChanged(databaseModel.criticScore, dto.criticScore)) {
                    generatedDiffDTO.criticScore = dto.criticScore
                    databaseModel.criticScore = dto.criticScore
                    databaseModel.criticScoreGroup = dto.criticScore.toScoreGroup()
                    changedFields.add(GameFields.CRITIC_SCORE)
                }
                if (MapperUtil.hasChanged(databaseModel.communityScore, dto.communityScore)) {
                    generatedDiffDTO.communityScore = dto.communityScore
                    databaseModel.communityScore = dto.communityScore
                    databaseModel.communityScoreGroup = dto.communityScore.toScoreGroup()
                    changedFields.add(GameFields.COMMUNITY_SCORE)
                }
                if (MapperUtil.hasChanged(databaseModel.manual, dto.manual)) {
                    generatedDiffDTO.manual = dto.manual
                    databaseModel.manual = dto.manual
                    changedFields.add(GameFields.MANUAL)
                }
                databaseModel
            }.flatMap { databaseModel ->
                setRelatedObject(
                    databaseModel,
                    { dto.plugin?.id },
                    { libraryPluginService.findById(it) },
                    { result ->
                        if (MapperUtil.hasChanged(databaseModel.pluginId, result?.id)) {
                            changedFields.add(GameFields.PLUGIN_ID)
                        }
                        generatedDiffDTO.pluginId = result?.id
                        databaseModel.pluginId = result?.id
                        entity.pluginId = result?.id

                        if (MapperUtil.hasChanged(databaseModel.pluginName, result?.name)) {
                            changedFields.add(GameFields.PLUGIN_NAME)
                        }
                        generatedDiffDTO.pluginName = result?.name
                        databaseModel.pluginName = result?.name
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.genres.mapNotNull { it.id } },
                    { genreService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.genres, results)) {
                            generatedDiffDTO.genres = results
                            databaseModel.genres = results
                            changedFields.add(GameFields.GENRES)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.platforms.mapNotNull { it.id } },
                    { platformService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.platforms, results)) {
                            generatedDiffDTO.platforms = results
                            databaseModel.platforms = results
                            changedFields.add(GameFields.PLATFORMS)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.publishers.mapNotNull { it.id } },
                    { companyService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.publishers, results)) {
                            generatedDiffDTO.publishers = results
                            databaseModel.publishers = results
                            changedFields.add(GameFields.PUBLISHERS)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.developers.mapNotNull { it.id } },
                    { companyService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.developers, results)) {
                            generatedDiffDTO.developers = results
                            databaseModel.developers = results
                            changedFields.add(GameFields.DEVELOPERS)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.categories.mapNotNull { it.id } },
                    { categoryService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.categories, results)) {
                            generatedDiffDTO.categories = results
                            databaseModel.categories = results
                            changedFields.add(GameFields.CATEGORIES)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.tags.mapNotNull { it.id } },
                    { tagService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.tags, results)) {
                            generatedDiffDTO.tags = results
                            databaseModel.tags = results
                            changedFields.add(GameFields.TAGS)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.features.mapNotNull { it.id } },
                    { featureService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.features, results)) {
                            generatedDiffDTO.features = results
                            databaseModel.features = results
                            changedFields.add(GameFields.FEATURES)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.series.mapNotNull { it.id } },
                    { seriesService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.series, results)) {
                            generatedDiffDTO.series = results
                            databaseModel.series = results
                            changedFields.add(GameFields.SERIES)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.ageRatings.mapNotNull { it.id } },
                    { ageRatingService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.ageRatings, results)) {
                            generatedDiffDTO.ageRatings = results
                            databaseModel.ageRatings = results
                            changedFields.add(GameFields.AGE_RATINGS)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObjects(
                    databaseModel,
                    { dto.regions.mapNotNull { it.id } },
                    { regionService.findById(it) },
                    { results ->
                        if (MapperUtil.hasChanged(databaseModel.regions, results)) {
                            generatedDiffDTO.regions = results
                            databaseModel.regions = results
                            changedFields.add(GameFields.REGIONS)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObject(
                    databaseModel,
                    { dto.source?.id },
                    { sourceService.findById(it) },
                    { result ->
                        if (MapperUtil.hasChanged(databaseModel.source?.id, result?.id)) {
                            generatedDiffDTO.source = result
                            databaseModel.source = result
                            changedFields.add(GameFields.SOURCE)
                        }
                    })
            }.flatMap { databaseModel ->
                setRelatedObject(
                    databaseModel,
                    { dto.completionStatus?.id },
                    { completionStatusService.findById(it) },
                    { result ->
                        if (MapperUtil.hasChanged(
                                databaseModel.completionStatus?.id,
                                result?.id
                            )
                        ) {
                            generatedDiffDTO.completionStatus = result
                            databaseModel.completionStatus = result
                            changedFields.add(GameFields.COMPLETION_STATUS)
                        }
                    })
            }
            .flatMap { it.asJson() }
            .doOnNext { entity.savedData = it }
            .thenReturn(Triple(entity, generatedDiffDTO, changedFields))
    }

    private fun <D : LibraryObjectDTO> setRelatedObjects(
        databaseModel: GameDatabaseModel,
        idGetter: () -> List<Long>?,
        dtoGetter: (Long) -> Mono<D>,
        dtoSetter: (List<D>) -> Unit
    ): Mono<GameDatabaseModel> {
        return Mono.fromSupplier { idGetter() }
            .flatMap { ids ->
                ids.toFlux().flatMapSequential { id -> dtoGetter(id) }.collectList()
            }
            .doOnNext {
                dtoSetter(it)
            }
            .thenReturn(databaseModel)
    }

    private fun <D : LibraryObjectDTO> setRelatedObject(
        databaseModel: GameDatabaseModel,
        idGetter: () -> Long?,
        dtoGetter: (Long) -> Mono<D>,
        dtoSetter: (D?) -> Unit
    ): Mono<GameDatabaseModel> {
        return Mono.fromSupplier { idGetter() }
            .flatMap { id ->
                dtoGetter(id)
                    .map { Tuple.of(it) }
                    .defaultIfEmpty { Tuple.of<D>(null) }
            }
            .doOnNext {
                dtoSetter(it._1)
            }
            .thenReturn(databaseModel)
    }


    override fun fillBasicDtoFields(dto: PanelGameDTO, entity: Game): PanelGameDTO {
        val result = super.fillBasicDtoFields(dto, entity)
        result.gameId = entity.gameId
        result.hasIcon = entity.iconMd5 != null
        result.hasCoverImage = entity.coverImageMd5 != null
        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        result.iconHash = entity.iconMd5
        result.coverImageHash = entity.coverImageMd5
        result.backgroundImageHash = entity.backgroundImageMd5
        return result
    }

    override fun fillOtherDtoFields(entity: Game, dto: PanelGameDTO): Mono<PanelGameDTO> {
        return entity.extractDbModelOrEmpty()
            .doOnNext { databaseModel ->
                dto.description = databaseModel.description
                dto.notes = databaseModel.notes
                dto.isHidden = databaseModel.isHidden
                dto.isFavorite = databaseModel.isFavorite
                dto.lastActivity = databaseModel.lastActivity
                dto.sortingName = databaseModel.sortingName
                dto.releaseDate = databaseModel.releaseDate
                dto.links = databaseModel.links
                dto.playtime = databaseModel.playtime
                dto.added = databaseModel.added
                dto.modified = databaseModel.modified
                dto.playCount = databaseModel.playCount
                dto.version = databaseModel.version
                dto.userScore = databaseModel.userScore
                dto.criticScore = databaseModel.criticScore
                dto.communityScore = databaseModel.communityScore
                dto.manual = databaseModel.manual
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteId(
                    databaseModel,
                    { databaseModel.pluginId },
                    libraryPluginService::findByPlayniteIdForPanel,
                    { obj -> dto.plugin = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.genres.mapNotNull { it.id } },
                    genreService::findByPlayniteIdsForPanel,
                    { obj -> dto.genres = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.platforms.mapNotNull { it.id } },
                    platformService::findByPlayniteIdsForPanel,
                    { obj -> dto.platforms = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.publishers.mapNotNull { it.id } },
                    companyService::findByPlayniteIdsForPanel,
                    { obj -> dto.publishers = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.developers.mapNotNull { it.id } },
                    companyService::findByPlayniteIdsForPanel,
                    { obj -> dto.developers = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.categories.mapNotNull { it.id } },
                    categoryService::findByPlayniteIdsForPanel,
                    { obj -> dto.categories = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.tags.mapNotNull { it.id } },
                    tagService::findByPlayniteIdsForPanel,
                    { obj -> dto.tags = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.features.mapNotNull { it.id } },
                    featureService::findByPlayniteIdsForPanel,
                    { obj -> dto.features = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.series.mapNotNull { it.id } },
                    seriesService::findByPlayniteIdsForPanel,
                    { obj -> dto.series = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.ageRatings.mapNotNull { it.id } },
                    ageRatingService::findByPlayniteIdsForPanel,
                    { obj -> dto.ageRatings = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteIds(
                    databaseModel,
                    { databaseModel.regions.mapNotNull { it.id } },
                    regionService::findByPlayniteIdsForPanel,
                    { obj -> dto.regions = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteId(
                    databaseModel,
                    { databaseModel.source?.id },
                    sourceService::findByPlayniteIdForPanel,
                    { obj -> dto.source = obj })
            }
            .flatMap { databaseModel ->
                setFieldsByPlayniteId(
                    databaseModel,
                    { databaseModel.completionStatus?.id },
                    completionStatusService::findByPlayniteIdForPanel,
                    { obj -> dto.completionStatus = obj })
            }
            .thenReturn(dto)
    }

    private fun <T> setFieldsByPlayniteIds(
        databaseModel: GameDatabaseModel,
        playniteIdGetter: () -> List<String>,
        objectFunction: (List<String>) -> Mono<List<T>>,
        setter: (List<T>) -> Unit
    ): Mono<GameDatabaseModel> {
        return Mono.fromSupplier { playniteIdGetter() }
            .flatMap { playniteIds -> objectFunction(playniteIds) }
            .doOnNext { setter(it) }
            .thenReturn(databaseModel)
    }

    private fun <T : Any> setFieldsByPlayniteId(
        databaseModel: GameDatabaseModel,
        playniteIdGetter: () -> String?,
        objectFunction: (String) -> Mono<T>,
        setter: (T) -> Unit
    ): Mono<GameDatabaseModel> {
        return Mono.fromSupplier { playniteIdGetter() }
            .flatMap { playniteIds -> objectFunction(playniteIds) }
            .doOnNext { setter(it) }
            .thenReturn(databaseModel)
    }

    override fun createDTO(): PanelGameDTO = PanelGameDTO()

    override fun createDiffDTO(): GameDiffDTO = GameDiffDTO()
}