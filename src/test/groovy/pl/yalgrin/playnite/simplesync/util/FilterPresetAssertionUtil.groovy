package pl.yalgrin.playnite.simplesync.util

import com.fasterxml.jackson.databind.ObjectMapper
import pl.yalgrin.playnite.simplesync.config.ObjectMapperProvider
import pl.yalgrin.playnite.simplesync.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.dto.FilterPresetDTO
import pl.yalgrin.playnite.simplesync.dto.filter.FilterPresetSettingsDTO
import pl.yalgrin.playnite.simplesync.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.dto.filter.StringItemPropertiesDTO

class FilterPresetAssertionUtil {
    private static final ObjectMapper objectMapper = ObjectMapperProvider.create()

    static boolean assertFilterPreset(FilterPresetDTO expectedDTO, FilterPresetDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        assert settingsMatch(resultDTO.getSettings(), expectedDTO.getSettings())
        assert resultDTO.getSortingOrder() == expectedDTO.getSortingOrder()
        assert resultDTO.getSortingOrderDirection() == expectedDTO.getSortingOrderDirection()
        assert resultDTO.getGroupingOrder() == expectedDTO.getGroupingOrder()
        assert resultDTO.isShowInFullscreenQuickSelection() == expectedDTO.isShowInFullscreenQuickSelection()
        true
    }

    static boolean assertFilterPresetEntity(FilterPresetDTO expectedDTO, FilterPreset resultEntity) {
        if (expectedDTO == null) {
            assert resultEntity == null
            return true
        }
        assert resultEntity != null
        assert resultEntity.getPlayniteId() == expectedDTO.getId()
        assert resultEntity.getName() == expectedDTO.getName()
        assert resultEntity.isRemoved() == expectedDTO.isRemoved()

        FilterPresetDTO resultDTO = objectMapper.readValue(resultEntity.getSavedData().asArray(), FilterPresetDTO.class)
        assertFilterPreset(expectedDTO, resultDTO)
    }

    static boolean settingsMatch(FilterPresetSettingsDTO resultDTO, FilterPresetSettingsDTO expectedDTO) {
        assert resultDTO?.isUseAndFilteringStyle() == expectedDTO?.isUseAndFilteringStyle()
        assert resultDTO?.isInstalled() == expectedDTO?.isInstalled()
        assert resultDTO?.isUninstalled() == expectedDTO?.isUninstalled()
        assert resultDTO?.isHidden() == expectedDTO?.isHidden()
        assert resultDTO?.isFavorite() == expectedDTO?.isFavorite()
        assert resultDTO?.getName() == expectedDTO?.getName()
        assert resultDTO?.getVersion() == expectedDTO?.getVersion()
        assert stringPropsMatch(resultDTO?.getReleaseYear(), expectedDTO?.getReleaseYear())
        assert idPropsMatch(resultDTO?.getGenre(), expectedDTO?.getGenre())
        assert idPropsMatch(resultDTO?.getPlatform(), expectedDTO?.getPlatform())
        assert idPropsMatch(resultDTO?.getPublisher(), expectedDTO?.getPublisher())
        assert idPropsMatch(resultDTO?.getDeveloper(), expectedDTO?.getDeveloper())
        assert idPropsMatch(resultDTO?.getCategory(), expectedDTO?.getCategory())
        assert idPropsMatch(resultDTO?.getTag(), expectedDTO?.getTag())
        assert idPropsMatch(resultDTO?.getSeries(), expectedDTO?.getSeries())
        assert idPropsMatch(resultDTO?.getRegion(), expectedDTO?.getRegion())
        assert idPropsMatch(resultDTO?.getSource(), expectedDTO?.getSource())
        assert idPropsMatch(resultDTO?.getAgeRating(), expectedDTO?.getAgeRating())
        assert idPropsMatch(resultDTO?.getLibrary(), expectedDTO?.getLibrary())
        assert idPropsMatch(resultDTO?.getCompletionStatuses(), expectedDTO?.getCompletionStatuses())
        assert idPropsMatch(resultDTO?.getFeature(), expectedDTO?.getFeature())
        assert intPropsMatch(resultDTO?.getUserScore(), expectedDTO?.getUserScore())
        assert intPropsMatch(resultDTO?.getCriticScore(), expectedDTO?.getCriticScore())
        assert intPropsMatch(resultDTO?.getCommunityScore(), expectedDTO?.getCommunityScore())
        assert intPropsMatch(resultDTO?.getLastActivity(), expectedDTO?.getLastActivity())
        assert intPropsMatch(resultDTO?.getRecentActivity(), expectedDTO?.getRecentActivity())
        assert intPropsMatch(resultDTO?.getAdded(), expectedDTO?.getAdded())
        assert intPropsMatch(resultDTO?.getModified(), expectedDTO?.getModified())
        assert intPropsMatch(resultDTO?.getPlayTime(), expectedDTO?.getPlayTime())
        assert intPropsMatch(resultDTO?.getInstallSize(), expectedDTO?.getInstallSize())
        true
    }

    static boolean stringPropsMatch(StringItemPropertiesDTO resultDTO, StringItemPropertiesDTO expectedDTO) {
        def expectedSize = (expectedDTO?.getValues()?.size() ?: 0)
        assert (resultDTO?.getValues()?.size() ?: 0) == expectedSize
        if (expectedSize > 0) {
            for (i in 0..<expectedSize) {
                assert resultDTO?.getValues()?.get(i) == expectedDTO?.getValues()?.get(i)
            }
        }
        true
    }

    static boolean idPropsMatch(IdItemPropertiesDTO resultDTO, IdItemPropertiesDTO expectedDTO) {
        def expectedSize = (expectedDTO?.getIds()?.size() ?: 0)
        assert (resultDTO?.getIds()?.size() ?: 0) == expectedSize
        if (expectedSize > 0) {
            for (i in 0..<expectedSize) {
                assert resultDTO?.getIds()?.get(i) == expectedDTO?.getIds()?.get(i)
            }
        }
        assert resultDTO?.getText() == expectedDTO?.getText()
        true
    }

    static boolean intPropsMatch(IntItemPropertiesDTO resultDTO, IntItemPropertiesDTO expectedDTO) {
        def expectedSize = (expectedDTO?.getValues()?.size() ?: 0)
        assert (resultDTO?.getValues()?.size() ?: 0) == expectedSize
        if (expectedSize > 0) {
            for (i in 0..<expectedSize) {
                assert resultDTO?.getValues()?.get(i) == expectedDTO?.getValues()?.get(i)
            }
        }
        true
    }
}
