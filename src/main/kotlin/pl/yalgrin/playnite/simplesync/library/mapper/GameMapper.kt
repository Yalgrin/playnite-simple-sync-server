package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.MapperUtil
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.common.util.asObject
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameFields
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectFields
import reactor.core.publisher.Mono

@Component
class GameMapper : LibraryObjectWithDiffMapper<Game, GameDiff, GameDTO, GameDiffDTO>() {
    override fun fillBasicFields(
        dto: GameDTO,
        entity: Game,
        diffDTO: GameDiffDTO
    ): Triple<Game, GameDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, diffDTO)
        result.second.gameId = result.first.gameId
        result.second.pluginId = result.first.pluginId
        return result
    }

    override fun fillOtherFields(
        entity: Game,
        dto: GameDTO,
        diffDTO: GameDiffDTO,
        changedFields: MutableList<String>
    ): Mono<Triple<Game, GameDiffDTO, MutableList<String>>> {
        return entity.savedData.asObject(GameDTO::class.java)
            .switchIfEmpty(Mono.fromSupplier { GameDTO() })
            .map { targetDto ->
                if (MapperUtil.hasChanged(targetDto.description, dto.description)) {
                    diffDTO.description = dto.description
                    changedFields.add(GameFields.DESCRIPTION)
                }
                if (MapperUtil.hasChanged(targetDto.notes, dto.notes)) {
                    diffDTO.notes = dto.notes
                    changedFields.add(GameFields.NOTES)
                }
                if (MapperUtil.hasChanged(targetDto.genres, dto.genres)) {
                    diffDTO.genres = dto.genres
                    changedFields.add(GameFields.GENRES)
                }
                if (MapperUtil.hasChanged(targetDto.isHidden, dto.isHidden)) {
                    diffDTO.isHidden = dto.isHidden
                    changedFields.add(GameFields.HIDDEN)
                }
                if (MapperUtil.hasChanged(targetDto.isFavorite, dto.isFavorite)) {
                    diffDTO.isFavorite = dto.isFavorite
                    changedFields.add(GameFields.FAVORITE)
                }
                if (MapperUtil.hasChanged(targetDto.lastActivity, dto.lastActivity)) {
                    diffDTO.lastActivity = dto.lastActivity
                    changedFields.add(GameFields.LAST_ACTIVITY)
                }
                if (MapperUtil.hasChanged(targetDto.sortingName, dto.sortingName)) {
                    diffDTO.sortingName = dto.sortingName
                    changedFields.add(GameFields.SORTING_NAME)
                }
                if (MapperUtil.hasChanged(targetDto.gameId, dto.gameId)) {
                    entity.gameId = dto.gameId
                    changedFields.add(GameFields.GAME_ID)
                }
                if (MapperUtil.hasChanged(targetDto.pluginId, dto.pluginId)) {
                    entity.pluginId = dto.pluginId
                    changedFields.add(GameFields.PLUGIN_ID)
                }
                if (MapperUtil.hasChanged(targetDto.platforms, dto.platforms)) {
                    diffDTO.platforms = dto.platforms
                    changedFields.add(GameFields.PLATFORMS)
                }
                if (MapperUtil.hasChanged(targetDto.publishers, dto.publishers)) {
                    diffDTO.publishers = dto.publishers
                    changedFields.add(GameFields.PUBLISHERS)
                }
                if (MapperUtil.hasChanged(targetDto.developers, dto.developers)) {
                    diffDTO.developers = dto.developers
                    changedFields.add(GameFields.DEVELOPERS)
                }
                if (MapperUtil.hasChanged(targetDto.releaseDate, dto.releaseDate)) {
                    diffDTO.releaseDate = dto.releaseDate
                    changedFields.add(GameFields.RELEASE_DATE)
                }
                if (MapperUtil.hasChanged(targetDto.categories, dto.categories)) {
                    diffDTO.categories = dto.categories
                    changedFields.add(GameFields.CATEGORIES)
                }
                if (MapperUtil.hasChanged(targetDto.tags, dto.tags)) {
                    diffDTO.tags = dto.tags
                    changedFields.add(GameFields.TAGS)
                }
                if (MapperUtil.hasChanged(targetDto.features, dto.features)) {
                    diffDTO.features = dto.features
                    changedFields.add(GameFields.FEATURES)
                }
                if (MapperUtil.haveLinksChanged(targetDto.links, dto.links)) {
                    diffDTO.links = dto.links
                    changedFields.add(GameFields.LINKS)
                }
                if (MapperUtil.hasChanged(targetDto.playtime, dto.playtime)) {
                    diffDTO.playtime = dto.playtime
                    changedFields.add(GameFields.PLAYTIME)
                }
                if (MapperUtil.hasChanged(targetDto.added, dto.added)) {
                    diffDTO.added = dto.added
                    changedFields.add(GameFields.ADDED)
                }
                if (MapperUtil.hasChanged(targetDto.modified, dto.modified)) {
                    diffDTO.modified = dto.modified
                    changedFields.add(GameFields.MODIFIED)
                }
                if (MapperUtil.hasChanged(targetDto.playCount, dto.playCount)) {
                    diffDTO.playCount = dto.playCount
                    changedFields.add(GameFields.PLAY_COUNT)
                }
                if (MapperUtil.hasChanged(targetDto.installSize, dto.installSize)) {
                    diffDTO.installSize = dto.installSize
                    changedFields.add(GameFields.INSTALL_SIZE)
                }
                if (MapperUtil.hasChanged(targetDto.lastSizeScanDate, dto.lastSizeScanDate)) {
                    diffDTO.lastSizeScanDate = dto.lastSizeScanDate
                    changedFields.add(GameFields.LAST_SIZE_SCAN_DATE)
                }
                if (MapperUtil.hasChanged(targetDto.series, dto.series)) {
                    diffDTO.series = dto.series
                    changedFields.add(GameFields.SERIES)
                }
                if (MapperUtil.hasChanged(targetDto.version, dto.version)) {
                    diffDTO.version = dto.version
                    changedFields.add(GameFields.VERSION)
                }
                if (MapperUtil.hasChanged(targetDto.ageRatings, dto.ageRatings)) {
                    diffDTO.ageRatings = dto.ageRatings
                    changedFields.add(GameFields.AGE_RATINGS)
                }
                if (MapperUtil.hasChanged(targetDto.regions, dto.regions)) {
                    diffDTO.regions = dto.regions
                    changedFields.add(GameFields.REGIONS)
                }
                if (MapperUtil.hasChanged(targetDto.source?.id, dto.source?.id)) {
                    diffDTO.source = dto.source
                    changedFields.add(GameFields.SOURCE)
                }
                if (MapperUtil.hasChanged(
                        targetDto.completionStatus?.id,
                        dto.completionStatus?.id
                    )
                ) {
                    diffDTO.completionStatus = dto.completionStatus
                    changedFields.add(GameFields.COMPLETION_STATUS)
                }
                if (MapperUtil.hasChanged(targetDto.userScore, dto.userScore)) {
                    diffDTO.userScore = dto.userScore
                    changedFields.add(GameFields.USER_SCORE)
                }
                if (MapperUtil.hasChanged(targetDto.criticScore, dto.criticScore)) {
                    diffDTO.criticScore = dto.criticScore
                    changedFields.add(GameFields.CRITIC_SCORE)
                }
                if (MapperUtil.hasChanged(targetDto.communityScore, dto.communityScore)) {
                    diffDTO.communityScore = dto.communityScore
                    changedFields.add(GameFields.COMMUNITY_SCORE)
                }
                if (MapperUtil.hasChanged(targetDto.manual, dto.manual)) {
                    diffDTO.manual = dto.manual
                    changedFields.add(GameFields.MANUAL)
                }
                dto
            }.flatMap { it.asJson() }
            .doOnNext { entity.savedData = it }
            .thenReturn(Triple(entity, diffDTO, changedFields))
    }

    override fun fillBasicFields(
        referenceDTO: GameDiffDTO,
        entity: Game,
        newDTO: GameDiffDTO
    ): Triple<Game, GameDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(referenceDTO, entity, newDTO)
        result.second.gameId = referenceDTO.gameId
        result.second.pluginId = referenceDTO.pluginId
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
            entity.savedData.asObject(GameDTO::class.java)
                .switchIfEmpty(Mono.fromSupplier { GameDTO() })
                .map { targetDto ->
                    if (changedFields.contains(LibraryObjectFields.NAME)) {
                        targetDto.name = entity.name
                    }
                    if (changedFields.contains(LibraryObjectFields.REMOVED)) {
                        targetDto.isRemoved = entity.isRemoved
                    }
                    if (referenceDTO.changedFields.contains(GameFields.DESCRIPTION) && MapperUtil.hasChanged(
                            targetDto.description,
                            referenceDTO.description
                        )
                    ) {
                        targetDto.description = referenceDTO.description
                        diffDTO.description = referenceDTO.description
                        changedFields.add(GameFields.DESCRIPTION)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.NOTES) && MapperUtil.hasChanged(
                            targetDto.notes,
                            referenceDTO.notes
                        )
                    ) {
                        targetDto.notes = referenceDTO.notes
                        diffDTO.notes = referenceDTO.notes
                        changedFields.add(GameFields.NOTES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.GENRES) && MapperUtil.hasChanged(
                            targetDto.genres,
                            referenceDTO.genres
                        )
                    ) {
                        targetDto.genres = referenceDTO.genres
                        diffDTO.genres = referenceDTO.genres
                        changedFields.add(GameFields.GENRES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.HIDDEN) && MapperUtil.hasChanged(
                            targetDto.isHidden,
                            referenceDTO.isHidden
                        )
                    ) {
                        targetDto.isHidden = referenceDTO.isHidden
                        diffDTO.isHidden = referenceDTO.isHidden
                        changedFields.add(GameFields.HIDDEN)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.FAVORITE) && MapperUtil.hasChanged(
                            targetDto.isFavorite,
                            referenceDTO.isFavorite
                        )
                    ) {
                        targetDto.isFavorite = referenceDTO.isFavorite
                        diffDTO.isFavorite = referenceDTO.isFavorite
                        changedFields.add(GameFields.FAVORITE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.LAST_ACTIVITY) && MapperUtil.hasChanged(
                            targetDto.lastActivity,
                            referenceDTO.lastActivity
                        )
                    ) {
                        targetDto.lastActivity = referenceDTO.lastActivity
                        diffDTO.lastActivity = referenceDTO.lastActivity
                        changedFields.add(GameFields.LAST_ACTIVITY)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.SORTING_NAME) && MapperUtil.hasChanged(
                            targetDto.sortingName,
                            referenceDTO.sortingName
                        )
                    ) {
                        targetDto.sortingName = referenceDTO.sortingName
                        diffDTO.sortingName = referenceDTO.sortingName
                        changedFields.add(GameFields.SORTING_NAME)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.PLATFORMS) && MapperUtil.hasChanged(
                            targetDto.platforms,
                            referenceDTO.platforms
                        )
                    ) {
                        targetDto.platforms = referenceDTO.platforms
                        diffDTO.platforms = referenceDTO.platforms
                        changedFields.add(GameFields.PLATFORMS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.PUBLISHERS) && MapperUtil.hasChanged(
                            targetDto.publishers,
                            referenceDTO.publishers
                        )
                    ) {
                        targetDto.publishers = referenceDTO.publishers
                        diffDTO.publishers = referenceDTO.publishers
                        changedFields.add(GameFields.PUBLISHERS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.DEVELOPERS) && MapperUtil.hasChanged(
                            targetDto.developers,
                            referenceDTO.developers
                        )
                    ) {
                        targetDto.developers = referenceDTO.developers
                        diffDTO.developers = referenceDTO.developers
                        changedFields.add(GameFields.DEVELOPERS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.RELEASE_DATE) && MapperUtil.hasChanged(
                            targetDto.releaseDate,
                            referenceDTO.releaseDate
                        )
                    ) {
                        targetDto.releaseDate = referenceDTO.releaseDate
                        diffDTO.releaseDate = referenceDTO.releaseDate
                        changedFields.add(GameFields.RELEASE_DATE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.CATEGORIES) && MapperUtil.hasChanged(
                            targetDto.categories,
                            referenceDTO.categories
                        )
                    ) {
                        targetDto.categories = referenceDTO.categories
                        diffDTO.categories = referenceDTO.categories
                        changedFields.add(GameFields.CATEGORIES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.TAGS) && MapperUtil.hasChanged(
                            targetDto.tags,
                            referenceDTO.tags
                        )
                    ) {
                        targetDto.tags = referenceDTO.tags
                        diffDTO.tags = referenceDTO.tags
                        changedFields.add(GameFields.TAGS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.FEATURES) && MapperUtil.hasChanged(
                            targetDto.features,
                            referenceDTO.features
                        )
                    ) {
                        targetDto.features = referenceDTO.features
                        diffDTO.features = referenceDTO.features
                        changedFields.add(GameFields.FEATURES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.LINKS) && MapperUtil.haveLinksChanged(
                            targetDto.links,
                            referenceDTO.links
                        )
                    ) {
                        targetDto.links = referenceDTO.links
                        diffDTO.links = referenceDTO.links
                        changedFields.add(GameFields.LINKS)
                    }
                    val playtimeDiff = referenceDTO.playtimeDiff
                    if (referenceDTO.changedFields.contains(GameFields.PLAYTIME) && (MapperUtil.hasChanged(
                            targetDto.playtime,
                            referenceDTO.playtime
                        ) || (playtimeDiff != null && playtimeDiff > 0))
                    ) {
                        if (playtimeDiff != null && playtimeDiff > 0) {
                            targetDto.playtime += playtimeDiff
                            diffDTO.playtimeDiff = playtimeDiff
                            diffDTO.playtime = targetDto.playtime
                        } else {
                            targetDto.playtime = referenceDTO.playtime
                            diffDTO.playtime = referenceDTO.playtime
                        }
                        changedFields.add(GameFields.PLAYTIME)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.ADDED) && referenceDTO.added != null && MapperUtil.hasChanged(
                            targetDto.added,
                            referenceDTO.added
                        )
                    ) {
                        targetDto.added = referenceDTO.added
                        diffDTO.added = referenceDTO.added
                        changedFields.add(GameFields.ADDED)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.MODIFIED) && referenceDTO.modified != null && MapperUtil.hasChanged(
                            targetDto.modified, referenceDTO.modified
                        )
                    ) {
                        targetDto.modified = referenceDTO.modified
                        diffDTO.modified = referenceDTO.modified
                        changedFields.add(GameFields.MODIFIED)
                    }
                    val playCountDiff = referenceDTO.playCountDiff
                    if (referenceDTO.changedFields.contains(GameFields.PLAY_COUNT) && (MapperUtil.hasChanged(
                            targetDto.playCount,
                            referenceDTO.playCount
                        ) || (playCountDiff != null && playCountDiff > 0))
                    ) {
                        if (playCountDiff != null && playCountDiff > 0) {
                            targetDto.playCount += playCountDiff
                            diffDTO.playCountDiff = playCountDiff
                            diffDTO.playCount = targetDto.playCount
                        } else {
                            targetDto.playCount = referenceDTO.playCount
                            diffDTO.playCount = referenceDTO.playCount
                        }
                        changedFields.add(GameFields.PLAY_COUNT)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.INSTALL_SIZE) && MapperUtil.hasChanged(
                            targetDto.installSize,
                            referenceDTO.installSize
                        )
                    ) {
                        targetDto.installSize = referenceDTO.installSize
                        diffDTO.installSize = referenceDTO.installSize
                        changedFields.add(GameFields.INSTALL_SIZE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.LAST_SIZE_SCAN_DATE) && MapperUtil.hasChanged(
                            targetDto.lastSizeScanDate,
                            referenceDTO.lastSizeScanDate
                        )
                    ) {
                        targetDto.lastSizeScanDate = referenceDTO.lastSizeScanDate
                        diffDTO.lastSizeScanDate = referenceDTO.lastSizeScanDate
                        changedFields.add(GameFields.LAST_SIZE_SCAN_DATE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.SERIES) && MapperUtil.hasChanged(
                            targetDto.series,
                            referenceDTO.series
                        )
                    ) {
                        targetDto.series = referenceDTO.series
                        diffDTO.series = referenceDTO.series
                        changedFields.add(GameFields.SERIES)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.VERSION) && MapperUtil.hasChanged(
                            targetDto.version,
                            referenceDTO.version
                        )
                    ) {
                        targetDto.version = referenceDTO.version
                        diffDTO.version = referenceDTO.version
                        changedFields.add(GameFields.VERSION)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.AGE_RATINGS) && MapperUtil.hasChanged(
                            targetDto.ageRatings,
                            referenceDTO.ageRatings
                        )
                    ) {
                        targetDto.ageRatings = referenceDTO.ageRatings
                        diffDTO.ageRatings = referenceDTO.ageRatings
                        changedFields.add(GameFields.AGE_RATINGS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.REGIONS) && MapperUtil.hasChanged(
                            targetDto.regions,
                            referenceDTO.regions
                        )
                    ) {
                        targetDto.regions = referenceDTO.regions
                        diffDTO.regions = referenceDTO.regions
                        changedFields.add(GameFields.REGIONS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.SOURCE) && MapperUtil.hasChanged(
                            targetDto.source?.id,
                            referenceDTO.source?.id
                        )
                    ) {
                        targetDto.source = referenceDTO.source
                        diffDTO.source = referenceDTO.source
                        changedFields.add(GameFields.SOURCE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.COMPLETION_STATUS) && MapperUtil.hasChanged(
                            targetDto.completionStatus?.id,
                            referenceDTO.completionStatus?.id
                        )
                    ) {
                        targetDto.completionStatus = referenceDTO.completionStatus
                        diffDTO.completionStatus = referenceDTO.completionStatus
                        changedFields.add(GameFields.COMPLETION_STATUS)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.USER_SCORE) && MapperUtil.hasChanged(
                            targetDto.userScore,
                            referenceDTO.userScore
                        )
                    ) {
                        targetDto.userScore = referenceDTO.userScore
                        diffDTO.userScore = referenceDTO.userScore
                        changedFields.add(GameFields.USER_SCORE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.CRITIC_SCORE) && MapperUtil.hasChanged(
                            targetDto.criticScore,
                            referenceDTO.criticScore
                        )
                    ) {
                        targetDto.criticScore = referenceDTO.criticScore
                        diffDTO.criticScore = referenceDTO.criticScore
                        changedFields.add(GameFields.CRITIC_SCORE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.COMMUNITY_SCORE) && MapperUtil.hasChanged(
                            targetDto.communityScore,
                            referenceDTO.communityScore
                        )
                    ) {
                        targetDto.communityScore = referenceDTO.communityScore
                        diffDTO.communityScore = referenceDTO.communityScore
                        changedFields.add(GameFields.COMMUNITY_SCORE)
                    }
                    if (referenceDTO.changedFields.contains(GameFields.MANUAL) && MapperUtil.hasChanged(
                            targetDto.manual,
                            referenceDTO.manual
                        )
                    ) {
                        targetDto.manual = referenceDTO.manual
                        diffDTO.manual = referenceDTO.manual
                        changedFields.add(GameFields.MANUAL)
                    }
                    targetDto
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
        return entity.savedData.asObject(GameDTO::class.java)
            .doOnNext { targetDto ->
                dto.description = targetDto.description
                dto.notes = targetDto.notes
                dto.genres = targetDto.genres
                dto.isHidden = targetDto.isHidden
                dto.isFavorite = targetDto.isFavorite
                dto.lastActivity = targetDto.lastActivity
                dto.sortingName = targetDto.sortingName
                dto.platforms = targetDto.platforms
                dto.publishers = targetDto.publishers
                dto.developers = targetDto.developers
                dto.releaseDate = targetDto.releaseDate
                dto.categories = targetDto.categories
                dto.tags = targetDto.tags
                dto.features = targetDto.features
                dto.links = targetDto.links
                dto.playtime = targetDto.playtime
                dto.added = targetDto.added
                dto.modified = targetDto.modified
                dto.playCount = targetDto.playCount
                dto.installSize = targetDto.installSize
                dto.lastSizeScanDate = targetDto.lastSizeScanDate
                dto.series = targetDto.series
                dto.version = targetDto.version
                dto.ageRatings = targetDto.ageRatings
                dto.regions = targetDto.regions
                dto.source = targetDto.source
                dto.completionStatus = targetDto.completionStatus
                dto.userScore = targetDto.userScore
                dto.criticScore = targetDto.criticScore
                dto.communityScore = targetDto.communityScore
                dto.manual = targetDto.manual
            }
            .thenReturn(dto)
    }

    override fun fillBasicDiffDtoFields(diffDto: GameDiffDTO, entity: Game): GameDiffDTO {
        val changedFields = diffDto.changedFields
        val result = super.fillBasicDiffDtoFields(diffDto, entity)
        if (changedFields.contains(GameFields.GAME_ID)) {
            result.gameId = entity.gameId
        }
        if (changedFields.contains(GameFields.PLUGIN_ID)) {
            result.pluginId = entity.pluginId
        }
        return result
    }

    override fun fillOtherFieldsFromDiffEntity(
        diffDTO: GameDiffDTO,
        entity: Game,
        diffEntity: GameDiff
    ): Mono<GameDiffDTO> {
        return entity.savedData.asObject(GameDTO::class.java)
            .doOnNext { targetDto ->
                val changedFields = diffDTO.changedFields
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

    override fun fillBasicDiffEntityFields(diffEntity: GameDiff, dto: GameDiffDTO): GameDiff {
        val result = super.fillBasicDiffEntityFields(diffEntity, dto)
        result.gameId = dto.gameId
        result.pluginId = dto.pluginId
        return result
    }

    override fun createDTO(): GameDTO = GameDTO()

    override fun createDiffDTO(): GameDiffDTO = GameDiffDTO()

    override fun createDiffEntity(): GameDiff = GameDiff()

    override fun getDiffClass(): Class<GameDiffDTO> = GameDiffDTO::class.java
}