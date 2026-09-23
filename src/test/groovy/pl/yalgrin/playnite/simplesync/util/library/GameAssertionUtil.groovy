package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.common.config.JsonMapperProviderKt
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.library.dto.GameDatabaseModel
import pl.yalgrin.playnite.simplesync.library.enums.InstallSizeGroup
import pl.yalgrin.playnite.simplesync.library.enums.PlaytimeCategory
import pl.yalgrin.playnite.simplesync.library.enums.ScoreGroupKt
import tools.jackson.databind.json.JsonMapper

import java.util.function.BiPredicate

class GameAssertionUtil {
    private static final JsonMapper objectMapper = JsonMapperProviderKt.buildJsonMapper()

    static boolean assertGame(GameDTO expectedDTO, GameDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.getDescription() == expectedDTO.getDescription()
        assert resultDTO.getNotes() == expectedDTO.getNotes()
        assert assertListMatches(expectedDTO.getGenres(), resultDTO.getGenres(), { expected, result ->
            GenreAssertionUtil.assertGenre(expected, result)
        })
        assert resultDTO.isHidden() == expectedDTO.isHidden()
        assert resultDTO.isFavorite() == expectedDTO.isFavorite()
        assert resultDTO.getLastActivity()?.toEpochSecond() == expectedDTO.getLastActivity()?.toEpochSecond()
        assert resultDTO.getSortingName() == expectedDTO.getSortingName()
        assert resultDTO.getGameId() == expectedDTO.getGameId()
        assert resultDTO.getPluginId() == expectedDTO.getPluginId()
        assert resultDTO.getPluginName() == expectedDTO.getPluginName()
        assert assertListMatches(expectedDTO.getPlatforms(), resultDTO.getPlatforms(), { expected, result ->
            PlatformAssertionUtil.assertPlatform(expected, result)
        })
        assert assertListMatches(expectedDTO.getPublishers(), resultDTO.getPublishers(), { expected, result ->
            CompanyAssertionUtil.assertCompany(expected, result)
        })
        assert assertListMatches(expectedDTO.getDevelopers(), resultDTO.getDevelopers(), { expected, result ->
            CompanyAssertionUtil.assertCompany(expected, result)
        })
        assert resultDTO.getReleaseDate() == expectedDTO.getReleaseDate()
        assert assertListMatches(expectedDTO.getCategories(), resultDTO.getCategories(), { expected, result ->
            CategoryAssertionUtil.assertCategory(expected, result)
        })
        assert assertListMatches(expectedDTO.getTags(), resultDTO.getTags(), { expected, result ->
            TagAssertionUtil.assertTag(expected, result)
        })
        assert assertListMatches(expectedDTO.getFeatures(), resultDTO.getFeatures(), { expected, result ->
            FeatureAssertionUtil.assertFeature(expected, result)
        })
        assert assertListMatches(expectedDTO.getLinks(), resultDTO.getLinks(), { expected, result ->
            assert expected.getName() == result.getName()
            assert expected.getUrl() == result.getUrl()
            true
        })
        assert resultDTO.getPlaytime() == expectedDTO.getPlaytime()
        assert resultDTO.getAdded()?.toEpochSecond() == expectedDTO.getAdded()?.toEpochSecond()
        assert resultDTO.getModified()?.toEpochSecond() == expectedDTO.getModified()?.toEpochSecond()
        assert resultDTO.getPlayCount() == expectedDTO.getPlayCount()
        assert resultDTO.getInstallSize() == expectedDTO.getInstallSize()
        assert resultDTO.getLastSizeScanDate()?.toEpochSecond() == expectedDTO.getLastSizeScanDate()?.toEpochSecond()
        assert assertListMatches(expectedDTO.getSeries(), resultDTO.getSeries(), { expected, result ->
            SeriesAssertionUtil.assertSeries(expected, result)
        })
        assert resultDTO.getVersion() == expectedDTO.getVersion()
        assert assertListMatches(expectedDTO.getAgeRatings(), resultDTO.getAgeRatings(), { expected, result ->
            AgeRatingAssertionUtil.assertAgeRating(expected, result)
        })
        assert assertListMatches(expectedDTO.getRegions(), resultDTO.getRegions(), { expected, result ->
            RegionAssertionUtil.assertRegion(expected, result)
        })
        assert SourceAssertionUtil.assertSource(expectedDTO.getSource(), resultDTO.getSource())
        assert CompletionStatusAssertionUtil.assertCompletionStatus(expectedDTO.getCompletionStatus(), resultDTO.getCompletionStatus())
        assert resultDTO.getUserScore() == expectedDTO.getUserScore()
        assert resultDTO.getCriticScore() == expectedDTO.getCriticScore()
        assert resultDTO.getCommunityScore() == expectedDTO.getCommunityScore()
        assert resultDTO.getManual() == expectedDTO.getManual()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    static boolean assertGame(GameDTO expectedDTO, GameDatabaseModel resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getPluginId() == expectedDTO.getPluginId()
        assert resultDTO.getPluginName() == expectedDTO.getPluginName()
        assert resultDTO.getDescription() == expectedDTO.getDescription()
        assert resultDTO.getNotes() == expectedDTO.getNotes()
        assert assertListMatches(expectedDTO.getGenres(), resultDTO.getGenres(), { expected, result ->
            GenreAssertionUtil.assertGenre(expected, result)
        })
        assert resultDTO.isHidden() == expectedDTO.isHidden()
        assert resultDTO.isFavorite() == expectedDTO.isFavorite()

        def expectedLastActivitySeconds = expectedDTO.getLastActivity()?.toEpochSecond()
        def expectedAddedSeconds = expectedDTO.getAdded()?.toEpochSecond()

        assert resultDTO.getLastActivity()?.toEpochSecond() == expectedLastActivitySeconds
        if (expectedAddedSeconds != null) {
            if (expectedLastActivitySeconds != null) {
                assert resultDTO.getRecentActivity()?.toEpochSecond() == Math.max(expectedAddedSeconds, expectedLastActivitySeconds)
            } else {
                assert resultDTO.getRecentActivity()?.toEpochSecond() == expectedAddedSeconds
            }
        } else {
            if (expectedLastActivitySeconds != null) {
                assert resultDTO.getRecentActivity()?.toEpochSecond() == expectedLastActivitySeconds
            } else {
                assert resultDTO.getRecentActivity()?.toEpochSecond() == null
            }
        }
        assert resultDTO.getSortingName() == expectedDTO.getSortingName()
        assert assertListMatches(expectedDTO.getPlatforms(), resultDTO.getPlatforms(), { expected, result ->
            PlatformAssertionUtil.assertPlatform(expected, result)
        })
        assert assertListMatches(expectedDTO.getPublishers(), resultDTO.getPublishers(), { expected, result ->
            CompanyAssertionUtil.assertCompany(expected, result)
        })
        assert assertListMatches(expectedDTO.getDevelopers(), resultDTO.getDevelopers(), { expected, result ->
            CompanyAssertionUtil.assertCompany(expected, result)
        })
        assert resultDTO.getReleaseDate() == expectedDTO.getReleaseDate()
        assert resultDTO.getReleaseYear() == expectedDTO.getReleaseDate()?.year
        assert assertListMatches(expectedDTO.getCategories(), resultDTO.getCategories(), { expected, result ->
            CategoryAssertionUtil.assertCategory(expected, result)
        })
        assert assertListMatches(expectedDTO.getTags(), resultDTO.getTags(), { expected, result ->
            TagAssertionUtil.assertTag(expected, result)
        })
        assert assertListMatches(expectedDTO.getFeatures(), resultDTO.getFeatures(), { expected, result ->
            FeatureAssertionUtil.assertFeature(expected, result)
        })
        assert assertListMatches(expectedDTO.getLinks(), resultDTO.getLinks(), { expected, result ->
            assert expected.getName() == result.getName()
            assert expected.getUrl() == result.getUrl()
            true
        })

        def expectedPlaytime = expectedDTO.getPlaytime()
        assert resultDTO.getPlaytime() == expectedPlaytime
        if (expectedPlaytime == null || expectedPlaytime == 0) {
            assert resultDTO.getPlaytimeCategory() == PlaytimeCategory.NOT_PLAYED
        } else if (expectedPlaytime < 3600) {
            assert resultDTO.getPlaytimeCategory() == PlaytimeCategory.LESS_THAN_HOUR
        } else if (expectedPlaytime < (3600 * 10)) {
            assert resultDTO.getPlaytimeCategory() == PlaytimeCategory.O1_10
        } else if (expectedPlaytime < (3600 * 100)) {
            assert resultDTO.getPlaytimeCategory() == PlaytimeCategory.O10_100
        } else if (expectedPlaytime < (3600 * 500)) {
            assert resultDTO.getPlaytimeCategory() == PlaytimeCategory.O100_500
        } else if (expectedPlaytime < (3600 * 1000)) {
            assert resultDTO.getPlaytimeCategory() == PlaytimeCategory.O500_1000
        } else {
            assert resultDTO.getPlaytimeCategory() == PlaytimeCategory.O1000_PLUS
        }

        assert resultDTO.getAdded()?.toEpochSecond() == expectedAddedSeconds
        assert resultDTO.getModified()?.toEpochSecond() == expectedDTO.getModified()?.toEpochSecond()
        assert resultDTO.getPlayCount() == expectedDTO.getPlayCount()

        def installSize = expectedDTO.getInstallSize()
        assert resultDTO.getInstallSize() == installSize
        if (installSize == null || installSize == 0L) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.NONE
        } else if (installSize <= (100L * 1024 * 1024)) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S0_0MB_100MB
        } else if (installSize <= (1024L * 1024 * 1024)) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S1_100MB_1GB
        } else if (installSize <= (5L * 1024 * 1024 * 1024)) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S2_1GB_5GB
        } else if (installSize <= (10L * 1024 * 1024 * 1024)) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S3_5GB_10GB
        } else if (installSize <= (20L * 1024 * 1024 * 1024)) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S4_10GB_20GB
        } else if (installSize <= (40L * 1024 * 1024 * 1024)) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S5_20GB_40GB
        } else if (installSize <= (100L * 1024 * 1024 * 1024)) {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S6_40GB_100GB
        } else {
            assert resultDTO.getInstallSizeGroup() == InstallSizeGroup.S7_100GB_PLUS
        }

        assert resultDTO.getLastSizeScanDate()?.toEpochSecond() == expectedDTO.getLastSizeScanDate()?.toEpochSecond()
        assert assertListMatches(expectedDTO.getSeries(), resultDTO.getSeries(), { expected, result ->
            SeriesAssertionUtil.assertSeries(expected, result)
        })
        assert resultDTO.getVersion() == expectedDTO.getVersion()
        assert assertListMatches(expectedDTO.getAgeRatings(), resultDTO.getAgeRatings(), { expected, result ->
            AgeRatingAssertionUtil.assertAgeRating(expected, result)
        })
        assert assertListMatches(expectedDTO.getRegions(), resultDTO.getRegions(), { expected, result ->
            RegionAssertionUtil.assertRegion(expected, result)
        })
        assert SourceAssertionUtil.assertSource(expectedDTO.getSource(), resultDTO.getSource())
        assert CompletionStatusAssertionUtil.assertCompletionStatus(expectedDTO.getCompletionStatus(), resultDTO.getCompletionStatus())
        assert resultDTO.getUserScore() == expectedDTO.getUserScore()
        assert resultDTO.getUserScoreGroup() == ScoreGroupKt.toScoreGroup(expectedDTO.getUserScore())
        assert resultDTO.getCriticScore() == expectedDTO.getCriticScore()
        assert resultDTO.getCriticScoreGroup() == ScoreGroupKt.toScoreGroup(expectedDTO.getCriticScore())
        assert resultDTO.getCommunityScore() == expectedDTO.getCommunityScore()
        assert resultDTO.getCommunityScoreGroup() == ScoreGroupKt.toScoreGroup(expectedDTO.getCommunityScore())
        assert resultDTO.getManual() == expectedDTO.getManual()
        true
    }

    static boolean assertGameEntity(GameDTO expectedDTO, Game resultEntity) {
        if (expectedDTO == null) {
            assert resultEntity == null
            return true
        }
        assert resultEntity != null
        assert resultEntity.getPlayniteId() == expectedDTO.getId()
        assert resultEntity.getName() == expectedDTO.getName()
        assert resultEntity.getGameId() == expectedDTO.getGameId()
        assert resultEntity.getPluginId() == expectedDTO.getPluginId()
        assert resultEntity.isRemoved() == expectedDTO.isRemoved()

        GameDatabaseModel resultDTO = objectMapper.readValue(resultEntity.getSavedData().asArray(), GameDatabaseModel.class)
        assertGame(expectedDTO, resultDTO)
    }

    static <T> boolean assertListMatches(List<T> expectedList, List<T> resultList,
                                         BiPredicate<T, T> predicate = { expected, result ->
                                             expected == result
                                         }) {
        def expectedSize = expectedList?.size() ?: 0
        assert (resultList?.size() ?: 0) == expectedSize
        if (expectedSize > 0) {
            for (i in 0..<expectedSize) {
                assert predicate(expectedList?.get(i), resultList?.get(i))
            }
        }
        true
    }
}
