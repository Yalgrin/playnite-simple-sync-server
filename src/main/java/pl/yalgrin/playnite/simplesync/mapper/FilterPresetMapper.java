package pl.yalgrin.playnite.simplesync.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.FilterPreset;
import pl.yalgrin.playnite.simplesync.dto.FilterPresetDTO;
import pl.yalgrin.playnite.simplesync.dto.filter.FilterPresetSettingsDTO;
import pl.yalgrin.playnite.simplesync.dto.filter.IdItemPropertiesDTO;
import pl.yalgrin.playnite.simplesync.dto.filter.IntItemPropertiesDTO;
import pl.yalgrin.playnite.simplesync.dto.filter.StringItemPropertiesDTO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilterPresetMapper extends AbstractObjectMapper<FilterPreset, FilterPresetDTO> {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public FilterPreset fillEntity(FilterPresetDTO dto, FilterPreset target) {
        dto.setRemoved(false);
        byte[] newDataToSave = objectMapper.writeValueAsBytes(dto);

        target.setChanged(
                !StringUtils.equals(target.getName(), dto.getName()) || target.isRemoved() || isPreviousDataDifferent(
                        target, dto));
        target.setName(dto.getName());
        target.setSavedData(Json.of(newDataToSave));
        target.setRemoved(false);
        return target;
    }

    private boolean isPreviousDataDifferent(FilterPreset target, FilterPresetDTO dto) {
        byte[] previousSavedData = Optional.ofNullable(target.getSavedData()).map(Json::asArray).orElse(null);
        FilterPresetDTO previousDTO = Try.of(() -> objectMapper.readValue(previousSavedData, FilterPresetDTO.class))
                .getOrNull();
        return previousDTO == null
                || !StringUtils.equals(previousDTO.getId(), dto.getId())
                || !StringUtils.equals(previousDTO.getName(), dto.getName())
                || !Objects.equals(previousDTO.isRemoved(), dto.isRemoved())
                || !StringUtils.equals(previousDTO.getSortingOrder(), dto.getSortingOrder())
                || !StringUtils.equals(previousDTO.getSortingOrderDirection(), dto.getSortingOrderDirection())
                || !StringUtils.equals(previousDTO.getGroupingOrder(), dto.getGroupingOrder())
                || !Objects.equals(previousDTO.isShowInFullscreenQuickSelection(),
                dto.isShowInFullscreenQuickSelection())
                || areSettingsDifferent(previousDTO.getSettings(), dto.getSettings());
    }

    private boolean areSettingsDifferent(FilterPresetSettingsDTO previousDTO, FilterPresetSettingsDTO newDTO) {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && (
                !Objects.equals(previousDTO.isUseAndFilteringStyle(), newDTO.isUseAndFilteringStyle())
                        || !Objects.equals(previousDTO.isInstalled(), newDTO.isInstalled())
                        || !Objects.equals(previousDTO.isUninstalled(), newDTO.isUninstalled())
                        || !Objects.equals(previousDTO.isHidden(), newDTO.isHidden())
                        || !Objects.equals(previousDTO.isFavorite(), newDTO.isFavorite())
                        || !StringUtils.equals(previousDTO.getName(), newDTO.getName())
                        || !StringUtils.equals(previousDTO.getVersion(), newDTO.getVersion())
                        || areItemPropertiesDifferent(previousDTO.getReleaseYear(), newDTO.getReleaseYear())
                        || areItemPropertiesDifferent(previousDTO.getGenre(), newDTO.getGenre())
                        || areItemPropertiesDifferent(previousDTO.getPlatform(), newDTO.getPlatform())
                        || areItemPropertiesDifferent(previousDTO.getDeveloper(), newDTO.getDeveloper())
                        || areItemPropertiesDifferent(previousDTO.getCategory(), newDTO.getCategory())
                        || areItemPropertiesDifferent(previousDTO.getTag(), newDTO.getTag())
                        || areItemPropertiesDifferent(previousDTO.getSeries(), newDTO.getSeries())
                        || areItemPropertiesDifferent(previousDTO.getRegion(), newDTO.getRegion())
                        || areItemPropertiesDifferent(previousDTO.getSource(), newDTO.getSource())
                        || areItemPropertiesDifferent(previousDTO.getAgeRating(), newDTO.getAgeRating())
                        || areItemPropertiesDifferent(previousDTO.getLibrary(), newDTO.getLibrary())
                        || areItemPropertiesDifferent(previousDTO.getCompletionStatuses(),
                        newDTO.getCompletionStatuses())
                        || areItemPropertiesDifferent(previousDTO.getFeature(), newDTO.getFeature())
                        || areItemPropertiesDifferent(previousDTO.getUserScore(), newDTO.getUserScore())
                        || areItemPropertiesDifferent(previousDTO.getCriticScore(), newDTO.getCriticScore())
                        || areItemPropertiesDifferent(previousDTO.getCommunityScore(), newDTO.getCommunityScore())
                        || areItemPropertiesDifferent(previousDTO.getLastActivity(), newDTO.getLastActivity())
                        || areItemPropertiesDifferent(previousDTO.getRecentActivity(), newDTO.getRecentActivity())
                        || areItemPropertiesDifferent(previousDTO.getAdded(), newDTO.getAdded())
                        || areItemPropertiesDifferent(previousDTO.getModified(), newDTO.getModified())
                        || areItemPropertiesDifferent(previousDTO.getPlayTime(), newDTO.getPlayTime())
                        || areItemPropertiesDifferent(previousDTO.getInstallSize(), newDTO.getInstallSize())
        ));
    }

    private boolean areItemPropertiesDifferent(StringItemPropertiesDTO previousDTO, StringItemPropertiesDTO newDTO) {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && areListsDifferent(previousDTO.getValues(), newDTO.getValues()));
    }

    private boolean areItemPropertiesDifferent(IdItemPropertiesDTO previousDTO, IdItemPropertiesDTO newDTO) {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && (areListsDifferent(previousDTO.getIds(), newDTO.getIds())
                || !StringUtils.equals(previousDTO.getText(), newDTO.getText())));
    }

    private boolean areItemPropertiesDifferent(IntItemPropertiesDTO previousDTO, IntItemPropertiesDTO newDTO) {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && areListsDifferent(previousDTO.getValues(), newDTO.getValues()));
    }

    private <T> boolean areListsDifferent(List<T> previousList, List<T> newList) {
        Integer previousCount = Optional.ofNullable(previousList).map(List::size).orElse(0);
        Integer newCount = Optional.ofNullable(newList).map(List::size).orElse(0);
        return !Objects.equals(previousCount, newCount)
                || previousList != null && newList != null && previousList.stream()
                .anyMatch(str -> !newList.contains(str));
    }

    @Override
    protected FilterPresetDTO createDTO() {
        return new FilterPresetDTO();
    }

    @SneakyThrows
    @Override
    public FilterPresetDTO toDTO(FilterPreset entity) {
        FilterPresetDTO dto = objectMapper.readValue(entity.getSavedData()
                .asArray(), FilterPresetDTO.class);
        dto.setId(entity.getPlayniteId());
        dto.setName(entity.getName());
        dto.setRemoved(entity.isRemoved());
        fillDtoFields(dto, entity);
        return dto;
    }
}
