package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.MapperUtil
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDatabaseModel
import pl.yalgrin.playnite.simplesync.library.dto.GameFields
import reactor.core.publisher.Mono

@Component
class GameMapper : LibraryObjectWithDiffMapperImpl<Game, GameDiff, GameDTO, GameDiffDTO, GameDiffDatabaseModel>() {
    override fun fillBasicFields(
        dto: GameDTO,
        entity: Game,
        generatedDiffDTO: GameDiffDTO
    ): Triple<Game, GameDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, generatedDiffDTO)
        val changedEntity = result.first
        val changedFields = result.third
        if (MapperUtil.hasChanged(changedEntity.gameId, dto.gameId)) {
            changedEntity.gameId = dto.gameId
            changedFields.add(GameFields.GAME_ID)
        }
        if (MapperUtil.hasChanged(changedEntity.pluginId, dto.pluginId)) {
            changedEntity.pluginId = dto.pluginId
            changedFields.add(GameFields.PLUGIN_ID)
        }
        return result
    }

    override fun fillOtherFields(
        entity: Game,
        dto: GameDTO,
        generatedDiffDTO: GameDiffDTO,
        changedFields: MutableList<String>
    ): Mono<Triple<Game, GameDiffDTO, MutableList<String>>> {
        return entity.extractDbModelOrEmpty()
            .map { databaseModel ->
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
                if (MapperUtil.hasChanged(databaseModel.genres, dto.genres)) {
                    generatedDiffDTO.genres = dto.genres
                    databaseModel.genres = dto.genres
                    changedFields.add(GameFields.GENRES)
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
                if (MapperUtil.hasChanged(databaseModel.lastActivity, dto.lastActivity)) {
                    generatedDiffDTO.lastActivity = dto.lastActivity
                    databaseModel.lastActivity = dto.lastActivity
                    changedFields.add(GameFields.LAST_ACTIVITY)
                }
                if (MapperUtil.hasChanged(databaseModel.sortingName, dto.sortingName)) {
                    generatedDiffDTO.sortingName = dto.sortingName
                    databaseModel.sortingName = dto.sortingName
                    changedFields.add(GameFields.SORTING_NAME)
                }
                if (MapperUtil.hasChanged(databaseModel.platforms, dto.platforms)) {
                    generatedDiffDTO.platforms = dto.platforms
                    databaseModel.platforms = dto.platforms
                    changedFields.add(GameFields.PLATFORMS)
                }
                if (MapperUtil.hasChanged(databaseModel.publishers, dto.publishers)) {
                    generatedDiffDTO.publishers = dto.publishers
                    databaseModel.publishers = dto.publishers
                    changedFields.add(GameFields.PUBLISHERS)
                }
                if (MapperUtil.hasChanged(databaseModel.developers, dto.developers)) {
                    generatedDiffDTO.developers = dto.developers
                    databaseModel.developers = dto.developers
                    changedFields.add(GameFields.DEVELOPERS)
                }
                if (MapperUtil.hasChanged(databaseModel.releaseDate, dto.releaseDate)) {
                    generatedDiffDTO.releaseDate = dto.releaseDate
                    databaseModel.releaseDate = dto.releaseDate
                    changedFields.add(GameFields.RELEASE_DATE)
                }
                if (MapperUtil.hasChanged(databaseModel.categories, dto.categories)) {
                    generatedDiffDTO.categories = dto.categories
                    databaseModel.categories = dto.categories
                    changedFields.add(GameFields.CATEGORIES)
                }
                if (MapperUtil.hasChanged(databaseModel.tags, dto.tags)) {
                    generatedDiffDTO.tags = dto.tags
                    databaseModel.tags = dto.tags
                    changedFields.add(GameFields.TAGS)
                }
                if (MapperUtil.hasChanged(databaseModel.features, dto.features)) {
                    generatedDiffDTO.features = dto.features
                    databaseModel.features = dto.features
                    changedFields.add(GameFields.FEATURES)
                }
                if (MapperUtil.haveLinksChanged(databaseModel.links, dto.links)) {
                    generatedDiffDTO.links = dto.links
                    databaseModel.links = dto.links
                    changedFields.add(GameFields.LINKS)
                }
                if (MapperUtil.hasChanged(databaseModel.playtime, dto.playtime)) {
                    generatedDiffDTO.playtime = dto.playtime
                    databaseModel.playtime = dto.playtime
                    changedFields.add(GameFields.PLAYTIME)
                }
                if (MapperUtil.hasChanged(databaseModel.added, dto.added)) {
                    generatedDiffDTO.added = dto.added
                    databaseModel.added = dto.added
                    changedFields.add(GameFields.ADDED)
                }
                if (MapperUtil.hasChanged(databaseModel.modified, dto.modified)) {
                    generatedDiffDTO.modified = dto.modified
                    databaseModel.modified = dto.modified
                    changedFields.add(GameFields.MODIFIED)
                }
                if (MapperUtil.hasChanged(databaseModel.playCount, dto.playCount)) {
                    generatedDiffDTO.playCount = dto.playCount
                    databaseModel.playCount = dto.playCount
                    changedFields.add(GameFields.PLAY_COUNT)
                }
                if (MapperUtil.hasChanged(databaseModel.installSize, dto.installSize)) {
                    generatedDiffDTO.installSize = dto.installSize
                    databaseModel.installSize = dto.installSize
                    changedFields.add(GameFields.INSTALL_SIZE)
                }
                if (MapperUtil.hasChanged(databaseModel.lastSizeScanDate, dto.lastSizeScanDate)) {
                    generatedDiffDTO.lastSizeScanDate = dto.lastSizeScanDate
                    databaseModel.lastSizeScanDate = dto.lastSizeScanDate
                    changedFields.add(GameFields.LAST_SIZE_SCAN_DATE)
                }
                if (MapperUtil.hasChanged(databaseModel.series, dto.series)) {
                    generatedDiffDTO.series = dto.series
                    databaseModel.series = dto.series
                    changedFields.add(GameFields.SERIES)
                }
                if (MapperUtil.hasChanged(databaseModel.version, dto.version)) {
                    generatedDiffDTO.version = dto.version
                    databaseModel.version = dto.version
                    changedFields.add(GameFields.VERSION)
                }
                if (MapperUtil.hasChanged(databaseModel.ageRatings, dto.ageRatings)) {
                    generatedDiffDTO.ageRatings = dto.ageRatings
                    databaseModel.ageRatings = dto.ageRatings
                    changedFields.add(GameFields.AGE_RATINGS)
                }
                if (MapperUtil.hasChanged(databaseModel.regions, dto.regions)) {
                    generatedDiffDTO.regions = dto.regions
                    databaseModel.regions = dto.regions
                    changedFields.add(GameFields.REGIONS)
                }
                if (MapperUtil.hasChanged(databaseModel.source?.id, dto.source?.id)) {
                    generatedDiffDTO.source = dto.source
                    databaseModel.source = dto.source
                    changedFields.add(GameFields.SOURCE)
                }
                if (MapperUtil.hasChanged(
                        databaseModel.completionStatus?.id,
                        dto.completionStatus?.id
                    )
                ) {
                    generatedDiffDTO.completionStatus = dto.completionStatus
                    databaseModel.completionStatus = dto.completionStatus
                    changedFields.add(GameFields.COMPLETION_STATUS)
                }
                if (MapperUtil.hasChanged(databaseModel.userScore, dto.userScore)) {
                    generatedDiffDTO.userScore = dto.userScore
                    databaseModel.userScore = dto.userScore
                    changedFields.add(GameFields.USER_SCORE)
                }
                if (MapperUtil.hasChanged(databaseModel.criticScore, dto.criticScore)) {
                    generatedDiffDTO.criticScore = dto.criticScore
                    databaseModel.criticScore = dto.criticScore
                    changedFields.add(GameFields.CRITIC_SCORE)
                }
                if (MapperUtil.hasChanged(databaseModel.communityScore, dto.communityScore)) {
                    generatedDiffDTO.communityScore = dto.communityScore
                    databaseModel.communityScore = dto.communityScore
                    changedFields.add(GameFields.COMMUNITY_SCORE)
                }
                if (MapperUtil.hasChanged(databaseModel.manual, dto.manual)) {
                    generatedDiffDTO.manual = dto.manual
                    databaseModel.manual = dto.manual
                    changedFields.add(GameFields.MANUAL)
                }
                databaseModel
            }.flatMap { it.asJson() }
            .doOnNext { entity.savedData = it }
            .thenReturn(Triple(entity, generatedDiffDTO, changedFields))
    }

    override fun fillBasicFields(
        referenceDTO: GameDiffDTO,
        entity: Game,
        newDTO: GameDiffDTO
    ): Triple<Game, GameDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(referenceDTO, entity, newDTO)
        val changedEntity = result.first
        val changedFields = result.third
        if (MapperUtil.hasChanged(changedEntity.gameId, referenceDTO.gameId)) {
            changedEntity.gameId = referenceDTO.gameId
            changedFields.add(GameFields.GAME_ID)
        }
        if (MapperUtil.hasChanged(changedEntity.pluginId, referenceDTO.pluginId)) {
            changedEntity.pluginId = referenceDTO.pluginId
            changedFields.add(GameFields.PLUGIN_ID)
        }
        return result
    }

    override fun fillOtherFields(
        entity: Game,
        referenceDTO: GameDiffDTO,
        diffDTO: GameDiffDTO,
        changedFields: MutableList<String>
    ): Mono<Triple<Game, GameDiffDTO, MutableList<String>>> {
        return Mono.defer {
            if (referenceDTO.changedFields.isEmpty()) {
                return@defer Mono.just(Triple(entity, diffDTO, changedFields))
            }
            entity.extractDbModelOrEmpty()
                .map { databaseModel ->
                    if (referenceDTO.changedFields.contains(GameFields.DESCRIPTION) && MapperUtil.hasChanged(
                            databaseModel.description,
                            referenceDTO.description
                        )
                    ) {
                        databaseModel.description = referenceDTO.description
                        diffDTO.description = referenceDTO.description
                        changedFields.add(GameFields.DESCRIPTION)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.NOTES) && MapperUtil.hasChanged(
                            databaseModel.notes,
                            referenceDTO.notes
                        )
                    ) {
                        databaseModel.notes = referenceDTO.notes
                        diffDTO.notes = referenceDTO.notes
                        changedFields.add(GameFields.NOTES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.GENRES) && MapperUtil.hasChanged(
                            databaseModel.genres,
                            referenceDTO.genres
                        )
                    ) {
                        databaseModel.genres = referenceDTO.genres
                        diffDTO.genres = referenceDTO.genres
                        changedFields.add(GameFields.GENRES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.HIDDEN) && MapperUtil.hasChanged(
                            databaseModel.isHidden,
                            referenceDTO.isHidden
                        )
                    ) {
                        databaseModel.isHidden = referenceDTO.isHidden
                        diffDTO.isHidden = referenceDTO.isHidden
                        changedFields.add(GameFields.HIDDEN)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.FAVORITE) && MapperUtil.hasChanged(
                            databaseModel.isFavorite,
                            referenceDTO.isFavorite
                        )
                    ) {
                        databaseModel.isFavorite = referenceDTO.isFavorite
                        diffDTO.isFavorite = referenceDTO.isFavorite
                        changedFields.add(GameFields.FAVORITE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.LAST_ACTIVITY) && MapperUtil.hasChanged(
                            databaseModel.lastActivity,
                            referenceDTO.lastActivity
                        )
                    ) {
                        databaseModel.lastActivity = referenceDTO.lastActivity
                        diffDTO.lastActivity = referenceDTO.lastActivity
                        changedFields.add(GameFields.LAST_ACTIVITY)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.SORTING_NAME) && MapperUtil.hasChanged(
                            databaseModel.sortingName,
                            referenceDTO.sortingName
                        )
                    ) {
                        databaseModel.sortingName = referenceDTO.sortingName
                        diffDTO.sortingName = referenceDTO.sortingName
                        changedFields.add(GameFields.SORTING_NAME)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.PLATFORMS) && MapperUtil.hasChanged(
                            databaseModel.platforms,
                            referenceDTO.platforms
                        )
                    ) {
                        databaseModel.platforms = referenceDTO.platforms
                        diffDTO.platforms = referenceDTO.platforms
                        changedFields.add(GameFields.PLATFORMS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.PUBLISHERS) && MapperUtil.hasChanged(
                            databaseModel.publishers,
                            referenceDTO.publishers
                        )
                    ) {
                        databaseModel.publishers = referenceDTO.publishers
                        diffDTO.publishers = referenceDTO.publishers
                        changedFields.add(GameFields.PUBLISHERS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.DEVELOPERS) && MapperUtil.hasChanged(
                            databaseModel.developers,
                            referenceDTO.developers
                        )
                    ) {
                        databaseModel.developers = referenceDTO.developers
                        diffDTO.developers = referenceDTO.developers
                        changedFields.add(GameFields.DEVELOPERS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.RELEASE_DATE) && MapperUtil.hasChanged(
                            databaseModel.releaseDate,
                            referenceDTO.releaseDate
                        )
                    ) {
                        databaseModel.releaseDate = referenceDTO.releaseDate
                        diffDTO.releaseDate = referenceDTO.releaseDate
                        changedFields.add(GameFields.RELEASE_DATE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.CATEGORIES) && MapperUtil.hasChanged(
                            databaseModel.categories,
                            referenceDTO.categories
                        )
                    ) {
                        databaseModel.categories = referenceDTO.categories
                        diffDTO.categories = referenceDTO.categories
                        changedFields.add(GameFields.CATEGORIES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.TAGS) && MapperUtil.hasChanged(
                            databaseModel.tags,
                            referenceDTO.tags
                        )
                    ) {
                        databaseModel.tags = referenceDTO.tags
                        diffDTO.tags = referenceDTO.tags
                        changedFields.add(GameFields.TAGS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.FEATURES) && MapperUtil.hasChanged(
                            databaseModel.features,
                            referenceDTO.features
                        )
                    ) {
                        databaseModel.features = referenceDTO.features
                        diffDTO.features = referenceDTO.features
                        changedFields.add(GameFields.FEATURES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.LINKS) && MapperUtil.haveLinksChanged(
                            databaseModel.links,
                            referenceDTO.links
                        )
                    ) {
                        databaseModel.links = referenceDTO.links
                        diffDTO.links = referenceDTO.links
                        changedFields.add(GameFields.LINKS)
                    }
                    val playtimeDiff = referenceDTO.playtimeDiff ?: 0
                    if (referenceDTO.changedFields.contains(GameFields.PLAYTIME) && (MapperUtil.hasChanged(
                            databaseModel.playtime,
                            referenceDTO.playtime
                        ) || playtimeDiff > 0)
                    ) {
                        if (playtimeDiff > 0) {
                            databaseModel.playtime += playtimeDiff
                            diffDTO.playtimeDiff = playtimeDiff
                            diffDTO.playtime = databaseModel.playtime
                        } else {
                            databaseModel.playtime = referenceDTO.playtime
                            diffDTO.playtime = referenceDTO.playtime
                        }
                        changedFields.add(GameFields.PLAYTIME)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.ADDED) && referenceDTO.added != null && MapperUtil.hasChanged(
                            databaseModel.added,
                            referenceDTO.added
                        )
                    ) {
                        databaseModel.added = referenceDTO.added
                        diffDTO.added = referenceDTO.added
                        changedFields.add(GameFields.ADDED)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.MODIFIED) && referenceDTO.modified != null && MapperUtil.hasChanged(
                            databaseModel.modified, referenceDTO.modified
                        )
                    ) {
                        databaseModel.modified = referenceDTO.modified
                        diffDTO.modified = referenceDTO.modified
                        changedFields.add(GameFields.MODIFIED)
                    }
                    val playCountDiff = referenceDTO.playCountDiff ?: 0
                    if (referenceDTO.changedFields.contains(GameFields.PLAY_COUNT) && (MapperUtil.hasChanged(
                            databaseModel.playCount,
                            referenceDTO.playCount
                        ) || playCountDiff > 0)
                    ) {
                        if (playCountDiff > 0) {
                            databaseModel.playCount += playCountDiff
                            diffDTO.playCountDiff = playCountDiff
                            diffDTO.playCount = databaseModel.playCount
                        } else {
                            databaseModel.playCount = referenceDTO.playCount
                            diffDTO.playCount = referenceDTO.playCount
                        }
                        changedFields.add(GameFields.PLAY_COUNT)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.INSTALL_SIZE) && MapperUtil.hasChanged(
                            databaseModel.installSize,
                            referenceDTO.installSize
                        )
                    ) {
                        databaseModel.installSize = referenceDTO.installSize
                        diffDTO.installSize = referenceDTO.installSize
                        changedFields.add(GameFields.INSTALL_SIZE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.LAST_SIZE_SCAN_DATE) && MapperUtil.hasChanged(
                            databaseModel.lastSizeScanDate,
                            referenceDTO.lastSizeScanDate
                        )
                    ) {
                        databaseModel.lastSizeScanDate = referenceDTO.lastSizeScanDate
                        diffDTO.lastSizeScanDate = referenceDTO.lastSizeScanDate
                        changedFields.add(GameFields.LAST_SIZE_SCAN_DATE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.SERIES) && MapperUtil.hasChanged(
                            databaseModel.series,
                            referenceDTO.series
                        )
                    ) {
                        databaseModel.series = referenceDTO.series
                        diffDTO.series = referenceDTO.series
                        changedFields.add(GameFields.SERIES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.VERSION) && MapperUtil.hasChanged(
                            databaseModel.version,
                            referenceDTO.version
                        )
                    ) {
                        databaseModel.version = referenceDTO.version
                        diffDTO.version = referenceDTO.version
                        changedFields.add(GameFields.VERSION)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.AGE_RATINGS) && MapperUtil.hasChanged(
                            databaseModel.ageRatings,
                            referenceDTO.ageRatings
                        )
                    ) {
                        databaseModel.ageRatings = referenceDTO.ageRatings
                        diffDTO.ageRatings = referenceDTO.ageRatings
                        changedFields.add(GameFields.AGE_RATINGS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.REGIONS) && MapperUtil.hasChanged(
                            databaseModel.regions,
                            referenceDTO.regions
                        )
                    ) {
                        databaseModel.regions = referenceDTO.regions
                        diffDTO.regions = referenceDTO.regions
                        changedFields.add(GameFields.REGIONS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.SOURCE) && MapperUtil.hasChanged(
                            databaseModel.source?.id,
                            referenceDTO.source?.id
                        )
                    ) {
                        databaseModel.source = referenceDTO.source
                        diffDTO.source = referenceDTO.source
                        changedFields.add(GameFields.SOURCE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.COMPLETION_STATUS) && MapperUtil.hasChanged(
                            databaseModel.completionStatus?.id,
                            referenceDTO.completionStatus?.id
                        )
                    ) {
                        databaseModel.completionStatus = referenceDTO.completionStatus
                        diffDTO.completionStatus = referenceDTO.completionStatus
                        changedFields.add(GameFields.COMPLETION_STATUS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.USER_SCORE) && MapperUtil.hasChanged(
                            databaseModel.userScore,
                            referenceDTO.userScore
                        )
                    ) {
                        databaseModel.userScore = referenceDTO.userScore
                        diffDTO.userScore = referenceDTO.userScore
                        changedFields.add(GameFields.USER_SCORE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.CRITIC_SCORE) && MapperUtil.hasChanged(
                            databaseModel.criticScore,
                            referenceDTO.criticScore
                        )
                    ) {
                        databaseModel.criticScore = referenceDTO.criticScore
                        diffDTO.criticScore = referenceDTO.criticScore
                        changedFields.add(GameFields.CRITIC_SCORE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.COMMUNITY_SCORE) && MapperUtil.hasChanged(
                            databaseModel.communityScore,
                            referenceDTO.communityScore
                        )
                    ) {
                        databaseModel.communityScore = referenceDTO.communityScore
                        diffDTO.communityScore = referenceDTO.communityScore
                        changedFields.add(GameFields.COMMUNITY_SCORE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.MANUAL) && MapperUtil.hasChanged(
                            databaseModel.manual,
                            referenceDTO.manual
                        )
                    ) {
                        databaseModel.manual = referenceDTO.manual
                        diffDTO.manual = referenceDTO.manual
                        changedFields.add(GameFields.MANUAL)
                    }
                    databaseModel
                }
                .flatMap { it.asJson() }
                .doOnNext { entity.savedData = it }
                .thenReturn(Triple(entity, diffDTO, changedFields))
        }
    }

    override fun fillBasicDtoFields(dto: GameDTO, entity: Game): GameDTO {
        val result = super.fillBasicDtoFields(dto, entity)
        result.gameId = entity.gameId
        result.pluginId = entity.pluginId
        result.hasIcon = entity.iconMd5 != null
        result.hasCoverImage = entity.coverImageMd5 != null
        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        return result
    }

    override fun fillOtherDtoFields(entity: Game, dto: GameDTO): Mono<GameDTO> {
        return entity.extractDbModelOrEmpty()
            .doOnNext { databaseModel ->
                dto.description = databaseModel.description
                dto.notes = databaseModel.notes
                dto.genres = databaseModel.genres
                dto.isHidden = databaseModel.isHidden
                dto.isFavorite = databaseModel.isFavorite
                dto.lastActivity = databaseModel.lastActivity
                dto.sortingName = databaseModel.sortingName
                dto.platforms = databaseModel.platforms
                dto.publishers = databaseModel.publishers
                dto.developers = databaseModel.developers
                dto.releaseDate = databaseModel.releaseDate
                dto.categories = databaseModel.categories
                dto.tags = databaseModel.tags
                dto.features = databaseModel.features
                dto.links = databaseModel.links
                dto.playtime = databaseModel.playtime
                dto.added = databaseModel.added
                dto.modified = databaseModel.modified
                dto.playCount = databaseModel.playCount
                dto.installSize = databaseModel.installSize
                dto.lastSizeScanDate = databaseModel.lastSizeScanDate
                dto.series = databaseModel.series
                dto.version = databaseModel.version
                dto.ageRatings = databaseModel.ageRatings
                dto.regions = databaseModel.regions
                dto.source = databaseModel.source
                dto.completionStatus = databaseModel.completionStatus
                dto.userScore = databaseModel.userScore
                dto.criticScore = databaseModel.criticScore
                dto.communityScore = databaseModel.communityScore
                dto.manual = databaseModel.manual
            }
            .thenReturn(dto)
    }

    override fun fillBasicDiffEntityFields(diffEntity: GameDiff, entity: Game, dto: GameDiffDTO): GameDiff {
        val result = super.fillBasicDiffEntityFields(diffEntity, entity, dto)
        result.gameId = entity.gameId
        result.pluginId = entity.pluginId
        return result
    }

    override fun fillOtherDiffEntityFields(diffEntity: GameDiff, entity: Game, dto: GameDiffDTO): Mono<GameDiff> {
        return diffEntity.extractDbModelOrEmpty()
            .map { databaseModel ->
                databaseModel.baseObjectId = dto.baseObjectId
                databaseModel.changedFields = dto.changedFields
                databaseModel.description = dto.description
                databaseModel.notes = dto.notes
                databaseModel.genres = dto.genres
                databaseModel.isHidden = dto.isHidden
                databaseModel.isFavorite = dto.isFavorite
                databaseModel.lastActivity = dto.lastActivity
                databaseModel.sortingName = dto.sortingName
                databaseModel.platforms = dto.platforms
                databaseModel.publishers = dto.publishers
                databaseModel.developers = dto.developers
                databaseModel.releaseDate = dto.releaseDate
                databaseModel.categories = dto.categories
                databaseModel.tags = dto.tags
                databaseModel.features = dto.features
                databaseModel.links = dto.links
                databaseModel.playtime = dto.playtime
                databaseModel.added = dto.added
                databaseModel.modified = dto.modified
                databaseModel.playCount = dto.playCount
                databaseModel.installSize = dto.installSize
                databaseModel.lastSizeScanDate = dto.lastSizeScanDate
                databaseModel.series = dto.series
                databaseModel.version = dto.version
                databaseModel.ageRatings = dto.ageRatings
                databaseModel.regions = dto.regions
                databaseModel.source = dto.source
                databaseModel.completionStatus = dto.completionStatus
                databaseModel.userScore = dto.userScore
                databaseModel.criticScore = dto.criticScore
                databaseModel.communityScore = dto.communityScore
                databaseModel.manual = dto.manual
                databaseModel
            }
            .flatMap { it.asJson() }
            .doOnNext { diffEntity.diffData = it }
            .thenReturn(diffEntity)
    }

    override fun fillBasicDiffDtoFields(
        diffDto: GameDiffDTO,
        dbModel: GameDiffDatabaseModel,
        entity: Game,
        diffEntity: GameDiff
    ): Pair<GameDiffDTO, GameDiffDatabaseModel> {
        val result = super.fillBasicDiffDtoFields(diffDto, dbModel, entity, diffEntity)
        val changedFields = result.second.changedFields
        if (changedFields.contains(GameFields.GAME_ID)) {
            result.first.gameId = entity.gameId
        }
        if (changedFields.contains(GameFields.PLUGIN_ID)) {
            result.first.pluginId = entity.pluginId
        }
        return result
    }

    override fun fillOtherFieldsFromDiffEntity(
        diffDTO: GameDiffDTO,
        dbModel: GameDiffDatabaseModel,
        entity: Game,
        diffEntity: GameDiff
    ): Mono<GameDiffDTO> {
        return entity.extractDbModelOrEmpty()
            .doOnNext { targetDto ->
                val changedFields = dbModel.changedFields
                if (changedFields.contains(GameFields.DESCRIPTION)) {
                    diffDTO.description = targetDto.description
                }
                if (changedFields.contains(GameFields.NOTES)) {
                    diffDTO.notes = targetDto.notes
                }
                if (changedFields.contains(GameFields.GENRES)) {
                    diffDTO.genres = targetDto.genres
                }
                if (changedFields.contains(GameFields.HIDDEN)) {
                    diffDTO.isHidden = targetDto.isHidden
                }
                if (changedFields.contains(GameFields.FAVORITE)) {
                    diffDTO.isFavorite = targetDto.isFavorite
                }
                if (changedFields.contains(GameFields.LAST_ACTIVITY)) {
                    diffDTO.lastActivity = targetDto.lastActivity
                }
                if (changedFields.contains(GameFields.SORTING_NAME)) {
                    diffDTO.sortingName = targetDto.sortingName
                }
                if (changedFields.contains(GameFields.PLATFORMS)) {
                    diffDTO.platforms = targetDto.platforms
                }
                if (changedFields.contains(GameFields.PUBLISHERS)) {
                    diffDTO.publishers = targetDto.publishers
                }
                if (changedFields.contains(GameFields.DEVELOPERS)) {
                    diffDTO.developers = targetDto.developers
                }
                if (changedFields.contains(GameFields.RELEASE_DATE)) {
                    diffDTO.releaseDate = targetDto.releaseDate
                }
                if (changedFields.contains(GameFields.CATEGORIES)) {
                    diffDTO.categories = targetDto.categories
                }
                if (changedFields.contains(GameFields.TAGS)) {
                    diffDTO.tags = targetDto.tags
                }
                if (changedFields.contains(GameFields.FEATURES)) {
                    diffDTO.features = targetDto.features
                }
                if (changedFields.contains(GameFields.LINKS)) {
                    diffDTO.links = targetDto.links
                }
                if (changedFields.contains(GameFields.PLAYTIME)) {
                    diffDTO.playtime = targetDto.playtime
                }
                if (changedFields.contains(GameFields.ADDED)) {
                    diffDTO.added = targetDto.added
                }
                if (changedFields.contains(GameFields.MODIFIED)) {
                    diffDTO.modified = targetDto.modified
                }
                if (changedFields.contains(GameFields.PLAY_COUNT)) {
                    diffDTO.playCount = targetDto.playCount
                }
                if (changedFields.contains(GameFields.INSTALL_SIZE)) {
                    diffDTO.installSize = targetDto.installSize
                }
                if (changedFields.contains(GameFields.LAST_SIZE_SCAN_DATE)) {
                    diffDTO.lastSizeScanDate = targetDto.lastSizeScanDate
                }
                if (changedFields.contains(GameFields.SERIES)) {
                    diffDTO.series = targetDto.series
                }
                if (changedFields.contains(GameFields.VERSION)) {
                    diffDTO.version = targetDto.version
                }
                if (changedFields.contains(GameFields.AGE_RATINGS)) {
                    diffDTO.ageRatings = targetDto.ageRatings
                }
                if (changedFields.contains(GameFields.REGIONS)) {
                    diffDTO.regions = targetDto.regions
                }
                if (changedFields.contains(GameFields.SOURCE)) {
                    diffDTO.source = targetDto.source
                }
                if (changedFields.contains(GameFields.COMPLETION_STATUS)) {
                    diffDTO.completionStatus = targetDto.completionStatus
                }
                if (changedFields.contains(GameFields.USER_SCORE)) {
                    diffDTO.userScore = targetDto.userScore
                }
                if (changedFields.contains(GameFields.CRITIC_SCORE)) {
                    diffDTO.criticScore = targetDto.criticScore
                }
                if (changedFields.contains(GameFields.COMMUNITY_SCORE)) {
                    diffDTO.communityScore = targetDto.communityScore
                }
                if (changedFields.contains(GameFields.MANUAL)) {
                    diffDTO.manual = targetDto.manual
                }
            }
            .thenReturn(diffDTO)
    }

    override fun createDTO(): GameDTO = GameDTO()

    override fun createDiffDTO(): GameDiffDTO = GameDiffDTO()

    override fun createDiffEntity(): GameDiff = GameDiff()

    override fun getDbModel(entity: GameDiff): Mono<GameDiffDatabaseModel> = entity.extractDbModelOrEmpty()
}