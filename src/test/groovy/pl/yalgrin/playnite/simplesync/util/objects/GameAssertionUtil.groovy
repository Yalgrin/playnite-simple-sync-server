package pl.yalgrin.playnite.simplesync.util.objects

import com.fasterxml.jackson.databind.ObjectMapper
import pl.yalgrin.playnite.simplesync.config.ObjectMapperProvider
import pl.yalgrin.playnite.simplesync.domain.objects.Game
import pl.yalgrin.playnite.simplesync.dto.objects.GameDTO

import java.util.function.BiPredicate

class GameAssertionUtil {
    private static final ObjectMapper objectMapper = ObjectMapperProvider.create()

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

        GameDTO resultDTO = objectMapper.readValue(resultEntity.getSavedData().asArray(), GameDTO.class)
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
