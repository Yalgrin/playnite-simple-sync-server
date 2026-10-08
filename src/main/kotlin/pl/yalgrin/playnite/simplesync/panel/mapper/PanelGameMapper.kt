package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGameDTO
import reactor.core.publisher.Mono

@Component
class PanelGameMapper : PanelObjectWithDiffMapperImpl<Game, PanelGameDTO, GameDiffDTO>() {
    override fun fillBasicFields(
        dto: PanelGameDTO,
        entity: Game,
        generatedDiffDTO: GameDiffDTO
    ): Triple<Game, GameDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, generatedDiffDTO)
        val changedEntity = result.first
        val changedDiff = result.second
        val changedFields = result.third

//        if (MapperUtil.hasChanged(changedEntity.gameId, dto.gameId)) {
//            changedEntity.gameId = dto.gameId
//            changedFields.add(GameFields.GAME_ID)
//        }
//        if (MapperUtil.hasChanged(changedEntity.pluginId, dto.pluginId)) {
//            changedEntity.pluginId = dto.pluginId
//            changedFields.add(GameFields.PLUGIN_ID)
//        }
        changedDiff.gameId = changedEntity.gameId
        changedDiff.pluginId = changedEntity.pluginId
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
//                if (MapperUtil.hasChanged(databaseModel.pluginId, dto.pluginId)) {
//                    changedFields.add(GameFields.PLUGIN_ID)
//                }
//                generatedDiffDTO.pluginId = dto.pluginId
//                databaseModel.pluginId = dto.pluginId
//
//                if (MapperUtil.hasChanged(databaseModel.pluginName, dto.pluginName)) {
//                    changedFields.add(GameFields.PLUGIN_NAME)
//                }
//                generatedDiffDTO.pluginName = dto.pluginName
//                databaseModel.pluginName = dto.pluginName
//
//                if (MapperUtil.hasChanged(databaseModel.description, dto.description)) {
//                    generatedDiffDTO.description = dto.description
//                    databaseModel.description = dto.description
//                    changedFields.add(GameFields.DESCRIPTION)
//                }
//                if (MapperUtil.hasChanged(databaseModel.notes, dto.notes)) {
//                    generatedDiffDTO.notes = dto.notes
//                    databaseModel.notes = dto.notes
//                    changedFields.add(GameFields.NOTES)
//                }
//                if (MapperUtil.hasChanged(databaseModel.genres, dto.genres)) {
//                    generatedDiffDTO.genres = dto.genres
//                    databaseModel.genres = dto.genres
//                    changedFields.add(GameFields.GENRES)
//                }
//                if (MapperUtil.hasChanged(databaseModel.isHidden, dto.isHidden)) {
//                    generatedDiffDTO.isHidden = dto.isHidden
//                    databaseModel.isHidden = dto.isHidden
//                    changedFields.add(GameFields.HIDDEN)
//                }
//                if (MapperUtil.hasChanged(databaseModel.isFavorite, dto.isFavorite)) {
//                    generatedDiffDTO.isFavorite = dto.isFavorite
//                    databaseModel.isFavorite = dto.isFavorite
//                    changedFields.add(GameFields.FAVORITE)
//                }
//                if (MapperUtil.hasChanged(databaseModel.lastActivity, dto.lastActivity)) {
//                    generatedDiffDTO.lastActivity = dto.lastActivity
//                    databaseModel.lastActivity = dto.lastActivity
//                    databaseModel.lastActivityInMs = databaseModel.lastActivity?.toInstant()?.toEpochMilli()
//                    databaseModel.recentActivity = dto.lastActivity?.max(databaseModel.added)
//                    databaseModel.recentActivityInMs = databaseModel.recentActivity?.toInstant()?.toEpochMilli()
//                    changedFields.add(GameFields.LAST_ACTIVITY)
//                }
//                if (MapperUtil.hasChanged(databaseModel.sortingName, dto.sortingName)) {
//                    generatedDiffDTO.sortingName = dto.sortingName
//                    databaseModel.sortingName = dto.sortingName
//                    changedFields.add(GameFields.SORTING_NAME)
//                }
//                if (MapperUtil.hasChanged(databaseModel.platforms, dto.platforms)) {
//                    generatedDiffDTO.platforms = dto.platforms
//                    databaseModel.platforms = dto.platforms
//                    changedFields.add(GameFields.PLATFORMS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.publishers, dto.publishers)) {
//                    generatedDiffDTO.publishers = dto.publishers
//                    databaseModel.publishers = dto.publishers
//                    changedFields.add(GameFields.PUBLISHERS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.developers, dto.developers)) {
//                    generatedDiffDTO.developers = dto.developers
//                    databaseModel.developers = dto.developers
//                    changedFields.add(GameFields.DEVELOPERS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.releaseDate, dto.releaseDate)) {
//                    generatedDiffDTO.releaseDate = dto.releaseDate
//                    databaseModel.releaseDate = dto.releaseDate
//                    databaseModel.releaseYear = dto.releaseDate?.year
//                    changedFields.add(GameFields.RELEASE_DATE)
//                }
//                if (MapperUtil.hasChanged(databaseModel.categories, dto.categories)) {
//                    generatedDiffDTO.categories = dto.categories
//                    databaseModel.categories = dto.categories
//                    changedFields.add(GameFields.CATEGORIES)
//                }
//                if (MapperUtil.hasChanged(databaseModel.tags, dto.tags)) {
//                    generatedDiffDTO.tags = dto.tags
//                    databaseModel.tags = dto.tags
//                    changedFields.add(GameFields.TAGS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.features, dto.features)) {
//                    generatedDiffDTO.features = dto.features
//                    databaseModel.features = dto.features
//                    changedFields.add(GameFields.FEATURES)
//                }
//                if (MapperUtil.haveLinksChanged(databaseModel.links, dto.links)) {
//                    generatedDiffDTO.links = dto.links
//                    databaseModel.links = dto.links
//                    changedFields.add(GameFields.LINKS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.playtime, dto.playtime)) {
//                    generatedDiffDTO.playtime = dto.playtime
//                    databaseModel.playtime = dto.playtime
//                    databaseModel.playtimeCategory = dto.playtime.toPlaytimeCategory()
//                    changedFields.add(GameFields.PLAYTIME)
//                }
//                if (MapperUtil.hasChanged(databaseModel.added, dto.added)) {
//                    generatedDiffDTO.added = dto.added
//                    databaseModel.added = dto.added
//                    databaseModel.addedInMs = databaseModel.added?.toInstant()?.toEpochMilli()
//                    databaseModel.recentActivity = dto.lastActivity?.max(databaseModel.added)
//                    databaseModel.recentActivityInMs = databaseModel.recentActivity?.toInstant()?.toEpochMilli()
//                    changedFields.add(GameFields.ADDED)
//                }
//                if (MapperUtil.hasChanged(databaseModel.modified, dto.modified)) {
//                    generatedDiffDTO.modified = dto.modified
//                    databaseModel.modified = dto.modified
//                    databaseModel.modifiedInMs = databaseModel.modified?.toInstant()?.toEpochMilli()
//                    changedFields.add(GameFields.MODIFIED)
//                }
//                if (MapperUtil.hasChanged(databaseModel.playCount, dto.playCount)) {
//                    generatedDiffDTO.playCount = dto.playCount
//                    databaseModel.playCount = dto.playCount
//                    changedFields.add(GameFields.PLAY_COUNT)
//                }
//                if (MapperUtil.hasChanged(databaseModel.installSize, dto.installSize)) {
//                    generatedDiffDTO.installSize = dto.installSize
//                    databaseModel.installSize = dto.installSize
//                    databaseModel.installSizeGroup = dto.installSize.toInstallSizeGroup()
//                    changedFields.add(GameFields.INSTALL_SIZE)
//                }
//                if (MapperUtil.hasChanged(databaseModel.lastSizeScanDate, dto.lastSizeScanDate)) {
//                    generatedDiffDTO.lastSizeScanDate = dto.lastSizeScanDate
//                    databaseModel.lastSizeScanDate = dto.lastSizeScanDate
//                    databaseModel.lastSizeScanDateInMs = databaseModel.lastSizeScanDate?.toInstant()?.toEpochMilli()
//                    changedFields.add(GameFields.LAST_SIZE_SCAN_DATE)
//                }
//                if (MapperUtil.hasChanged(databaseModel.series, dto.series)) {
//                    generatedDiffDTO.series = dto.series
//                    databaseModel.series = dto.series
//                    changedFields.add(GameFields.SERIES)
//                }
//                if (MapperUtil.hasChanged(databaseModel.version, dto.version)) {
//                    generatedDiffDTO.version = dto.version
//                    databaseModel.version = dto.version
//                    changedFields.add(GameFields.VERSION)
//                }
//                if (MapperUtil.hasChanged(databaseModel.ageRatings, dto.ageRatings)) {
//                    generatedDiffDTO.ageRatings = dto.ageRatings
//                    databaseModel.ageRatings = dto.ageRatings
//                    changedFields.add(GameFields.AGE_RATINGS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.regions, dto.regions)) {
//                    generatedDiffDTO.regions = dto.regions
//                    databaseModel.regions = dto.regions
//                    changedFields.add(GameFields.REGIONS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.source?.id, dto.source?.id)) {
//                    generatedDiffDTO.source = dto.source
//                    databaseModel.source = dto.source
//                    changedFields.add(GameFields.SOURCE)
//                }
//                if (MapperUtil.hasChanged(
//                        databaseModel.completionStatus?.id,
//                        dto.completionStatus?.id
//                    )
//                ) {
//                    generatedDiffDTO.completionStatus = dto.completionStatus
//                    databaseModel.completionStatus = dto.completionStatus
//                    changedFields.add(GameFields.COMPLETION_STATUS)
//                }
//                if (MapperUtil.hasChanged(databaseModel.userScore, dto.userScore)) {
//                    generatedDiffDTO.userScore = dto.userScore
//                    databaseModel.userScore = dto.userScore
//                    databaseModel.userScoreGroup = dto.userScore.toScoreGroup()
//                    changedFields.add(GameFields.USER_SCORE)
//                }
//                if (MapperUtil.hasChanged(databaseModel.criticScore, dto.criticScore)) {
//                    generatedDiffDTO.criticScore = dto.criticScore
//                    databaseModel.criticScore = dto.criticScore
//                    databaseModel.criticScoreGroup = dto.criticScore.toScoreGroup()
//                    changedFields.add(GameFields.CRITIC_SCORE)
//                }
//                if (MapperUtil.hasChanged(databaseModel.communityScore, dto.communityScore)) {
//                    generatedDiffDTO.communityScore = dto.communityScore
//                    databaseModel.communityScore = dto.communityScore
//                    databaseModel.communityScoreGroup = dto.communityScore.toScoreGroup()
//                    changedFields.add(GameFields.COMMUNITY_SCORE)
//                }
//                if (MapperUtil.hasChanged(databaseModel.manual, dto.manual)) {
//                    generatedDiffDTO.manual = dto.manual
//                    databaseModel.manual = dto.manual
//                    changedFields.add(GameFields.MANUAL)
//                }
                databaseModel
            }.flatMap { it.asJson() }
            .doOnNext { entity.savedData = it }
            .thenReturn(Triple(entity, generatedDiffDTO, changedFields))
    }


    override fun fillBasicDtoFields(dto: PanelGameDTO, entity: Game): PanelGameDTO {
        val result = super.fillBasicDtoFields(dto, entity)
//        result.gameId = entity.gameId
//        result.pluginId = entity.pluginId
//        result.hasIcon = entity.iconMd5 != null
//        result.hasCoverImage = entity.coverImageMd5 != null
//        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        return result
    }

    override fun fillOtherDtoFields(entity: Game, dto: PanelGameDTO): Mono<PanelGameDTO> {
        return entity.extractDbModelOrEmpty()
            .doOnNext { databaseModel ->
//                dto.pluginId = databaseModel.pluginId
//                dto.pluginName = databaseModel.pluginName
                dto.description = databaseModel.description
//                dto.notes = databaseModel.notes
//                dto.genres = databaseModel.genres
//                dto.isHidden = databaseModel.isHidden
//                dto.isFavorite = databaseModel.isFavorite
//                dto.lastActivity = databaseModel.lastActivity
//                dto.sortingName = databaseModel.sortingName
//                dto.platforms = databaseModel.platforms
//                dto.publishers = databaseModel.publishers
//                dto.developers = databaseModel.developers
//                dto.releaseDate = databaseModel.releaseDate
//                dto.categories = databaseModel.categories
//                dto.tags = databaseModel.tags
//                dto.features = databaseModel.features
//                dto.links = databaseModel.links
//                dto.playtime = databaseModel.playtime
//                dto.added = databaseModel.added
//                dto.modified = databaseModel.modified
//                dto.playCount = databaseModel.playCount
//                dto.installSize = databaseModel.installSize
//                dto.lastSizeScanDate = databaseModel.lastSizeScanDate
//                dto.series = databaseModel.series
//                dto.version = databaseModel.version
//                dto.ageRatings = databaseModel.ageRatings
//                dto.regions = databaseModel.regions
//                dto.source = databaseModel.source
//                dto.completionStatus = databaseModel.completionStatus
//                dto.userScore = databaseModel.userScore
//                dto.criticScore = databaseModel.criticScore
//                dto.communityScore = databaseModel.communityScore
//                dto.manual = databaseModel.manual
            }
            .thenReturn(dto)
    }

    override fun createDTO(): PanelGameDTO = PanelGameDTO()

    override fun createDiffDTO(): GameDiffDTO = GameDiffDTO()
}