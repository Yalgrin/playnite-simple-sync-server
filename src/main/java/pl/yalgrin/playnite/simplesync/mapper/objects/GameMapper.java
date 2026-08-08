package pl.yalgrin.playnite.simplesync.mapper.objects;

import io.r2dbc.postgresql.codec.Json;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.library.domain.Game;
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff;
import pl.yalgrin.playnite.simplesync.library.dto.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static pl.yalgrin.playnite.simplesync.utils.MapperUtil.hasChanged;
import static pl.yalgrin.playnite.simplesync.utils.MapperUtil.haveLinksChanged;

@Component
public class GameMapper extends AbstractObjectWithMetadataMapper<Game, GameDiff, GameDTO, GameDiffDTO> {

    @Autowired
    public GameMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @SneakyThrows
    @Override
    protected void fillEntityAndGenerateDiffAdditionalFields(GameDTO dto, Game game, GameDiffDTO diffDTO) {
        diffDTO.setGameId(dto.getGameId());
        diffDTO.setPluginId(dto.getPluginId());

        GameDTO targetDto = Try.of(() -> objectMapper.readValue(game.getSavedData().asArray(), GameDTO.class))
                .getOrElse(GameDTO::new);

        if (hasChanged(targetDto.getDescription(), dto.getDescription())) {
            diffDTO.setDescription(dto.getDescription());
            diffDTO.getChangedFields().add(GameFields.DESCRIPTION);
        }
        if (hasChanged(targetDto.getNotes(), dto.getNotes())) {
            diffDTO.setNotes(dto.getNotes());
            diffDTO.getChangedFields().add(GameFields.NOTES);
        }
        if (hasChanged(targetDto.getGenres(), dto.getGenres())) {
            diffDTO.setGenres(dto.getGenres());
            diffDTO.getChangedFields().add(GameFields.GENRES);
        }
        if (hasChanged(targetDto.isHidden(), dto.isHidden())) {
            diffDTO.setHidden(dto.isHidden());
            diffDTO.getChangedFields().add(GameFields.HIDDEN);
        }
        if (hasChanged(targetDto.isFavorite(), dto.isFavorite())) {
            diffDTO.setFavorite(dto.isFavorite());
            diffDTO.getChangedFields().add(GameFields.FAVORITE);
        }
        if (hasChanged(targetDto.getLastActivity(), dto.getLastActivity())) {
            diffDTO.setLastActivity(dto.getLastActivity());
            diffDTO.getChangedFields().add(GameFields.LAST_ACTIVITY);
        }
        if (hasChanged(targetDto.getSortingName(), dto.getSortingName())) {
            diffDTO.setSortingName(dto.getSortingName());
            diffDTO.getChangedFields().add(GameFields.SORTING_NAME);
        }
        if (hasChanged(targetDto.getGameId(), dto.getGameId())) {
            game.setGameId(dto.getGameId());
            diffDTO.getChangedFields().add(GameFields.GAME_ID);
        }
        if (hasChanged(targetDto.getPluginId(), dto.getPluginId())) {
            game.setPluginId(dto.getPluginId());
            diffDTO.getChangedFields().add(GameFields.PLUGIN_ID);
        }
        if (hasChanged(targetDto.getPlatforms(), dto.getPlatforms())) {
            diffDTO.setPlatforms(dto.getPlatforms());
            diffDTO.getChangedFields().add(GameFields.PLATFORMS);
        }
        if (hasChanged(targetDto.getPublishers(), dto.getPublishers())) {
            diffDTO.setPublishers(dto.getPublishers());
            diffDTO.getChangedFields().add(GameFields.PUBLISHERS);
        }
        if (hasChanged(targetDto.getDevelopers(), dto.getDevelopers())) {
            diffDTO.setDevelopers(dto.getDevelopers());
            diffDTO.getChangedFields().add(GameFields.DEVELOPERS);
        }
        if (hasChanged(targetDto.getReleaseDate(), dto.getReleaseDate())) {
            diffDTO.setReleaseDate(dto.getReleaseDate());
            diffDTO.getChangedFields().add(GameFields.RELEASE_DATE);
        }
        if (hasChanged(targetDto.getCategories(), dto.getCategories())) {
            diffDTO.setCategories(dto.getCategories());
            diffDTO.getChangedFields().add(GameFields.CATEGORIES);
        }
        if (hasChanged(targetDto.getTags(), dto.getTags())) {
            diffDTO.setTags(dto.getTags());
            diffDTO.getChangedFields().add(GameFields.TAGS);
        }
        if (hasChanged(targetDto.getFeatures(), dto.getFeatures())) {
            diffDTO.setFeatures(dto.getFeatures());
            diffDTO.getChangedFields().add(GameFields.FEATURES);
        }
        if (haveLinksChanged(targetDto.getLinks(), dto.getLinks())) {
            diffDTO.setLinks(dto.getLinks());
            diffDTO.getChangedFields().add(GameFields.LINKS);
        }
        if (hasChanged(targetDto.getPlaytime(), dto.getPlaytime())) {
            diffDTO.setPlaytime(dto.getPlaytime());
            diffDTO.getChangedFields().add(GameFields.PLAYTIME);
        }
        if (hasChanged(targetDto.getAdded(), dto.getAdded())) {
            diffDTO.setAdded(dto.getAdded());
            diffDTO.getChangedFields().add(GameFields.ADDED);
        }
        if (hasChanged(targetDto.getModified(), dto.getModified())) {
            diffDTO.setModified(dto.getModified());
            diffDTO.getChangedFields().add(GameFields.MODIFIED);
        }
        if (hasChanged(targetDto.getPlayCount(), dto.getPlayCount())) {
            diffDTO.setPlayCount(dto.getPlayCount());
            diffDTO.getChangedFields().add(GameFields.PLAY_COUNT);
        }
        if (hasChanged(targetDto.getInstallSize(), dto.getInstallSize())) {
            diffDTO.setInstallSize(dto.getInstallSize());
            diffDTO.getChangedFields().add(GameFields.INSTALL_SIZE);
        }
        if (hasChanged(targetDto.getLastSizeScanDate(), dto.getLastSizeScanDate())) {
            diffDTO.setLastSizeScanDate(dto.getLastSizeScanDate());
            diffDTO.getChangedFields().add(GameFields.LAST_SIZE_SCAN_DATE);
        }
        if (hasChanged(targetDto.getSeries(), dto.getSeries())) {
            diffDTO.setSeries(dto.getSeries());
            diffDTO.getChangedFields().add(GameFields.SERIES);
        }
        if (hasChanged(targetDto.getVersion(), dto.getVersion())) {
            diffDTO.setVersion(dto.getVersion());
            diffDTO.getChangedFields().add(GameFields.VERSION);
        }
        if (hasChanged(targetDto.getAgeRatings(), dto.getAgeRatings())) {
            diffDTO.setAgeRatings(dto.getAgeRatings());
            diffDTO.getChangedFields().add(GameFields.AGE_RATINGS);
        }
        if (hasChanged(targetDto.getRegions(), dto.getRegions())) {
            diffDTO.setRegions(dto.getRegions());
            diffDTO.getChangedFields().add(GameFields.REGIONS);
        }
        if (hasChanged(getId(targetDto.getSource()), getId(dto.getSource()))) {
            diffDTO.setSource(dto.getSource());
            diffDTO.getChangedFields().add(GameFields.SOURCE);
        }
        if (hasChanged(getId(targetDto.getCompletionStatus()), getId(dto.getCompletionStatus()))) {
            diffDTO.setCompletionStatus(dto.getCompletionStatus());
            diffDTO.getChangedFields().add(GameFields.COMPLETION_STATUS);
        }
        if (hasChanged(targetDto.getUserScore(), dto.getUserScore())) {
            diffDTO.setUserScore(dto.getUserScore());
            diffDTO.getChangedFields().add(GameFields.USER_SCORE);
        }
        if (hasChanged(targetDto.getCriticScore(), dto.getCriticScore())) {
            diffDTO.setCriticScore(dto.getCriticScore());
            diffDTO.getChangedFields().add(GameFields.CRITIC_SCORE);
        }
        if (hasChanged(targetDto.getCommunityScore(), dto.getCommunityScore())) {
            diffDTO.setCommunityScore(dto.getCommunityScore());
            diffDTO.getChangedFields().add(GameFields.COMMUNITY_SCORE);
        }
        if (hasChanged(targetDto.getManual(), dto.getManual())) {
            diffDTO.setManual(dto.getManual());
            diffDTO.getChangedFields().add(GameFields.MANUAL);
        }
        game.setSavedData(Json.of(objectMapper.writeValueAsBytes(dto)));
    }


    private static String getId(LibraryObjectDTO source) {
        return Optional.ofNullable(source).map(LibraryObjectDTO::getId).orElse(null);
    }

    @Override
    protected void fillDiffDtoAdditionalFields(GameDiffDTO dto, GameDiffDTO diffDTO, Game target) {
        diffDTO.setGameId(dto.getGameId());
        diffDTO.setPluginId(dto.getPluginId());
    }

    @SneakyThrows
    @Override
    protected void fillEntityAndGenerateDiffAdditionalFields(GameDiffDTO dto, Game game, GameDiffDTO diffDTO) {
        List<String> changedFields = dto.getChangedFields();
        if (changedFields == null) {
            return;
        }
        GameDTO targetDto = Try.of(() -> objectMapper.readValue(game.getSavedData().asArray(), GameDTO.class))
                .getOrElse(GameDTO::new);

        if (diffDTO.getChangedFields().contains(LibraryObjectFields.NAME)) {
            targetDto.setName(game.getName());
        }
        if (diffDTO.getChangedFields().contains(LibraryObjectFields.REMOVED)) {
            targetDto.setRemoved(game.isRemoved());
        }
        if (changedFields.contains(GameFields.DESCRIPTION) && hasChanged(targetDto.getDescription(),
                dto.getDescription())) {
            targetDto.setDescription(dto.getDescription());
            diffDTO.setDescription(dto.getDescription());
            diffDTO.getChangedFields().add(GameFields.DESCRIPTION);
        }
        if (changedFields.contains(GameFields.NOTES) && hasChanged(targetDto.getNotes(), dto.getNotes())) {
            targetDto.setNotes(dto.getNotes());
            diffDTO.setNotes(dto.getNotes());
            diffDTO.getChangedFields().add(GameFields.NOTES);
        }
        if (changedFields.contains(GameFields.GENRES) && hasChanged(targetDto.getGenres(), dto.getGenres())) {
            targetDto.setGenres(dto.getGenres());
            diffDTO.setGenres(dto.getGenres());
            diffDTO.getChangedFields().add(GameFields.GENRES);
        }
        if (changedFields.contains(GameFields.HIDDEN) && hasChanged(targetDto.isHidden(), dto.isHidden())) {
            targetDto.setHidden(dto.isHidden());
            diffDTO.setHidden(dto.isHidden());
            diffDTO.getChangedFields().add(GameFields.HIDDEN);
        }
        if (changedFields.contains(GameFields.FAVORITE) && hasChanged(targetDto.isFavorite(), dto.isFavorite())) {
            targetDto.setFavorite(dto.isFavorite());
            diffDTO.setFavorite(dto.isFavorite());
            diffDTO.getChangedFields().add(GameFields.FAVORITE);
        }
        if (changedFields.contains(GameFields.LAST_ACTIVITY) && hasChanged(targetDto.getLastActivity(),
                dto.getLastActivity())) {
            targetDto.setLastActivity(dto.getLastActivity());
            diffDTO.setLastActivity(dto.getLastActivity());
            diffDTO.getChangedFields().add(GameFields.LAST_ACTIVITY);
        }
        if (changedFields.contains(GameFields.SORTING_NAME) && hasChanged(targetDto.getSortingName(),
                dto.getSortingName())) {
            targetDto.setSortingName(dto.getSortingName());
            diffDTO.setSortingName(dto.getSortingName());
            diffDTO.getChangedFields().add(GameFields.SORTING_NAME);
        }
        if (changedFields.contains(GameFields.PLATFORMS) && hasChanged(targetDto.getPlatforms(),
                dto.getPlatforms())) {
            targetDto.setPlatforms(dto.getPlatforms());
            diffDTO.setPlatforms(dto.getPlatforms());
            diffDTO.getChangedFields().add(GameFields.PLATFORMS);
        }
        if (changedFields.contains(GameFields.PUBLISHERS) && hasChanged(targetDto.getPublishers(),
                dto.getPublishers())) {
            targetDto.setPublishers(dto.getPublishers());
            diffDTO.setPublishers(dto.getPublishers());
            diffDTO.getChangedFields().add(GameFields.PUBLISHERS);
        }
        if (changedFields.contains(GameFields.DEVELOPERS) && hasChanged(targetDto.getDevelopers(),
                dto.getDevelopers())) {
            targetDto.setDevelopers(dto.getDevelopers());
            diffDTO.setDevelopers(dto.getDevelopers());
            diffDTO.getChangedFields().add(GameFields.DEVELOPERS);
        }
        if (changedFields.contains(GameFields.RELEASE_DATE) && hasChanged(targetDto.getReleaseDate(),
                dto.getReleaseDate())) {
            targetDto.setReleaseDate(dto.getReleaseDate());
            diffDTO.setReleaseDate(dto.getReleaseDate());
            diffDTO.getChangedFields().add(GameFields.RELEASE_DATE);
        }
        if (changedFields.contains(GameFields.CATEGORIES) && hasChanged(targetDto.getCategories(),
                dto.getCategories())) {
            targetDto.setCategories(dto.getCategories());
            diffDTO.setCategories(dto.getCategories());
            diffDTO.getChangedFields().add(GameFields.CATEGORIES);
        }
        if (changedFields.contains(GameFields.TAGS) && hasChanged(targetDto.getTags(), dto.getTags())) {
            targetDto.setTags(dto.getTags());
            diffDTO.setTags(dto.getTags());
            diffDTO.getChangedFields().add(GameFields.TAGS);
        }
        if (changedFields.contains(GameFields.FEATURES) && hasChanged(targetDto.getFeatures(), dto.getFeatures())) {
            targetDto.setFeatures(dto.getFeatures());
            diffDTO.setFeatures(dto.getFeatures());
            diffDTO.getChangedFields().add(GameFields.FEATURES);
        }
        if (changedFields.contains(GameFields.LINKS) && haveLinksChanged(targetDto.getLinks(), dto.getLinks())) {
            targetDto.setLinks(dto.getLinks());
            diffDTO.setLinks(dto.getLinks());
            diffDTO.getChangedFields().add(GameFields.LINKS);
        }
        if (changedFields.contains(GameFields.PLAYTIME) && (hasChanged(targetDto.getPlaytime(),
                dto.getPlaytime()) || (dto.getPlaytimeDiff() != null && dto.getPlaytimeDiff() > 0))) {
            if (dto.getPlaytimeDiff() != null && dto.getPlaytimeDiff() > 0) {
                targetDto.setPlaytime(Stream.of(targetDto.getPlaytime(), dto.getPlaytimeDiff()).reduce(0L, Long::sum));
                diffDTO.setPlaytimeDiff(dto.getPlaytimeDiff());
                diffDTO.setPlaytime(targetDto.getPlaytime());
            } else {
                targetDto.setPlaytime(dto.getPlaytime());
                diffDTO.setPlaytime(dto.getPlaytime());
            }
            diffDTO.getChangedFields().add(GameFields.PLAYTIME);
        }
        if (changedFields.contains(GameFields.ADDED) && dto.getAdded() != null && hasChanged(targetDto.getAdded(),
                dto.getAdded())) {
            targetDto.setAdded(dto.getAdded());
            diffDTO.setAdded(dto.getAdded());
            diffDTO.getChangedFields().add(GameFields.ADDED);
        }
        if (changedFields.contains(GameFields.MODIFIED) && dto.getModified() != null && hasChanged(
                targetDto.getModified(), dto.getModified())) {
            targetDto.setModified(dto.getModified());
            diffDTO.setModified(dto.getModified());
            diffDTO.getChangedFields().add(GameFields.MODIFIED);
        }
        if (changedFields.contains(GameFields.PLAY_COUNT) && (hasChanged(targetDto.getPlayCount(),
                dto.getPlayCount()) || (dto.getPlayCountDiff() != null && dto.getPlayCountDiff() > 0))) {
            if (dto.getPlayCountDiff() != null && dto.getPlayCountDiff() > 0) {
                targetDto.setPlayCount(
                        Stream.of(targetDto.getPlayCount(), dto.getPlayCountDiff()).reduce(0L, Long::sum));
                diffDTO.setPlayCountDiff(dto.getPlayCountDiff());
                diffDTO.setPlayCount(targetDto.getPlayCount());
            } else {
                targetDto.setPlayCount(dto.getPlayCount());
                diffDTO.setPlayCount(dto.getPlayCount());
            }
            diffDTO.getChangedFields().add(GameFields.PLAY_COUNT);
        }
        if (changedFields.contains(GameFields.INSTALL_SIZE) && hasChanged(targetDto.getInstallSize(),
                dto.getInstallSize())) {
            targetDto.setInstallSize(dto.getInstallSize());
            diffDTO.setInstallSize(dto.getInstallSize());
            diffDTO.getChangedFields().add(GameFields.INSTALL_SIZE);
        }
        if (changedFields.contains(GameFields.LAST_SIZE_SCAN_DATE) && hasChanged(targetDto.getLastSizeScanDate(),
                dto.getLastSizeScanDate())) {
            targetDto.setLastSizeScanDate(dto.getLastSizeScanDate());
            diffDTO.setLastSizeScanDate(dto.getLastSizeScanDate());
            diffDTO.getChangedFields().add(GameFields.LAST_SIZE_SCAN_DATE);
        }
        if (changedFields.contains(GameFields.SERIES) && hasChanged(targetDto.getSeries(), dto.getSeries())) {
            targetDto.setSeries(dto.getSeries());
            diffDTO.setSeries(dto.getSeries());
            diffDTO.getChangedFields().add(GameFields.SERIES);
        }
        if (changedFields.contains(GameFields.VERSION) && hasChanged(targetDto.getVersion(), dto.getVersion())) {
            targetDto.setVersion(dto.getVersion());
            diffDTO.setVersion(dto.getVersion());
            diffDTO.getChangedFields().add(GameFields.VERSION);
        }
        if (changedFields.contains(GameFields.AGE_RATINGS) && hasChanged(targetDto.getAgeRatings(),
                dto.getAgeRatings())) {
            targetDto.setAgeRatings(dto.getAgeRatings());
            diffDTO.setAgeRatings(dto.getAgeRatings());
            diffDTO.getChangedFields().add(GameFields.AGE_RATINGS);
        }
        if (changedFields.contains(GameFields.REGIONS) && hasChanged(targetDto.getRegions(), dto.getRegions())) {
            targetDto.setRegions(dto.getRegions());
            diffDTO.setRegions(dto.getRegions());
            diffDTO.getChangedFields().add(GameFields.REGIONS);
        }
        if (changedFields.contains(GameFields.SOURCE) && hasChanged(getId(targetDto.getSource()),
                getId(dto.getSource()))) {
            targetDto.setSource(dto.getSource());
            diffDTO.setSource(dto.getSource());
            diffDTO.getChangedFields().add(GameFields.SOURCE);
        }
        if (changedFields.contains(GameFields.COMPLETION_STATUS) && hasChanged(
                getId(targetDto.getCompletionStatus()), getId(dto.getCompletionStatus()))) {
            targetDto.setCompletionStatus(dto.getCompletionStatus());
            diffDTO.setCompletionStatus(dto.getCompletionStatus());
            diffDTO.getChangedFields().add(GameFields.COMPLETION_STATUS);
        }
        if (changedFields.contains(GameFields.USER_SCORE) && hasChanged(targetDto.getUserScore(),
                dto.getUserScore())) {
            targetDto.setUserScore(dto.getUserScore());
            diffDTO.setUserScore(dto.getUserScore());
            diffDTO.getChangedFields().add(GameFields.USER_SCORE);
        }
        if (changedFields.contains(GameFields.CRITIC_SCORE) && hasChanged(targetDto.getCriticScore(),
                dto.getCriticScore())) {
            targetDto.setCriticScore(dto.getCriticScore());
            diffDTO.setCriticScore(dto.getCriticScore());
            diffDTO.getChangedFields().add(GameFields.CRITIC_SCORE);
        }
        if (changedFields.contains(GameFields.COMMUNITY_SCORE) && hasChanged(targetDto.getCommunityScore(),
                dto.getCommunityScore())) {
            targetDto.setCommunityScore(dto.getCommunityScore());
            diffDTO.setCommunityScore(dto.getCommunityScore());
            diffDTO.getChangedFields().add(GameFields.COMMUNITY_SCORE);
        }
        if (changedFields.contains(GameFields.MANUAL) && hasChanged(targetDto.getManual(), dto.getManual())) {
            targetDto.setManual(dto.getManual());
            diffDTO.setManual(dto.getManual());
            diffDTO.getChangedFields().add(GameFields.MANUAL);
        }

        game.setSavedData(Json.of(objectMapper.writeValueAsBytes(targetDto)));
    }

    @Override
    protected void fillOtherDtoFields(Game game, GameDTO dto) {
        dto.setGameId(game.getGameId());
        dto.setPluginId(game.getPluginId());

        GameDTO targetDto = Try.of(() -> objectMapper.readValue(game.getSavedData().asArray(), GameDTO.class))
                .getOrElse(GameDTO::new);

        dto.setDescription(targetDto.getDescription());
        dto.setNotes(targetDto.getNotes());
        dto.setGenres(targetDto.getGenres());
        dto.setHidden(targetDto.isHidden());
        dto.setFavorite(targetDto.isFavorite());
        dto.setLastActivity(targetDto.getLastActivity());
        dto.setSortingName(targetDto.getSortingName());
        dto.setPlatforms(targetDto.getPlatforms());
        dto.setPublishers(targetDto.getPublishers());
        dto.setDevelopers(targetDto.getDevelopers());
        dto.setReleaseDate(targetDto.getReleaseDate());
        dto.setCategories(targetDto.getCategories());
        dto.setTags(targetDto.getTags());
        dto.setFeatures(targetDto.getFeatures());
        dto.setLinks(targetDto.getLinks());
        dto.setPlaytime(targetDto.getPlaytime());
        dto.setAdded(targetDto.getAdded());
        dto.setModified(targetDto.getModified());
        dto.setPlayCount(targetDto.getPlayCount());
        dto.setInstallSize(targetDto.getInstallSize());
        dto.setLastSizeScanDate(targetDto.getLastSizeScanDate());
        dto.setSeries(targetDto.getSeries());
        dto.setVersion(targetDto.getVersion());
        dto.setAgeRatings(targetDto.getAgeRatings());
        dto.setRegions(targetDto.getRegions());
        dto.setSource(targetDto.getSource());
        dto.setCompletionStatus(targetDto.getCompletionStatus());
        dto.setUserScore(targetDto.getUserScore());
        dto.setCriticScore(targetDto.getCriticScore());
        dto.setCommunityScore(targetDto.getCommunityScore());
        dto.setManual(targetDto.getManual());

        dto.setHasIcon(game.getIconMd5() != null);
        dto.setHasCoverImage(game.getCoverImageMd5() != null);
        dto.setHasBackgroundImage(game.getBackgroundImageMd5() != null);
    }

    @Override
    protected void fillOtherFieldsFromDiffEntity(GameDiffDTO diffDTO, List<String> changedFields, Game entity,
                                                 GameDiff diffEntity) {
        if (changedFields.contains(GameFields.GAME_ID)) {
            diffDTO.setGameId(entity.getGameId());
        }
        if (changedFields.contains(GameFields.PLUGIN_ID)) {
            diffDTO.setPluginId(entity.getPluginId());
        }

        GameDTO targetDto = Try.of(() -> objectMapper.readValue(entity.getSavedData().asArray(), GameDTO.class))
                .getOrElse(GameDTO::new);
        if (changedFields.contains(GameFields.DESCRIPTION)) {
            diffDTO.setDescription(targetDto.getDescription());
        }
        if (changedFields.contains(GameFields.NOTES)) {
            diffDTO.setNotes(targetDto.getNotes());
        }
        if (changedFields.contains(GameFields.GENRES)) {
            diffDTO.setGenres(targetDto.getGenres());
        }
        if (changedFields.contains(GameFields.HIDDEN)) {
            diffDTO.setHidden(targetDto.isHidden());
        }
        if (changedFields.contains(GameFields.FAVORITE)) {
            diffDTO.setFavorite(targetDto.isFavorite());
        }
        if (changedFields.contains(GameFields.LAST_ACTIVITY)) {
            diffDTO.setLastActivity(targetDto.getLastActivity());
        }
        if (changedFields.contains(GameFields.SORTING_NAME)) {
            diffDTO.setSortingName(targetDto.getSortingName());
        }
        if (changedFields.contains(GameFields.PLATFORMS)) {
            diffDTO.setPlatforms(targetDto.getPlatforms());
        }
        if (changedFields.contains(GameFields.PUBLISHERS)) {
            diffDTO.setPublishers(targetDto.getPublishers());
        }
        if (changedFields.contains(GameFields.DEVELOPERS)) {
            diffDTO.setDevelopers(targetDto.getDevelopers());
        }
        if (changedFields.contains(GameFields.RELEASE_DATE)) {
            diffDTO.setReleaseDate(targetDto.getReleaseDate());
        }
        if (changedFields.contains(GameFields.CATEGORIES)) {
            diffDTO.setCategories(targetDto.getCategories());
        }
        if (changedFields.contains(GameFields.TAGS)) {
            diffDTO.setTags(targetDto.getTags());
        }
        if (changedFields.contains(GameFields.FEATURES)) {
            diffDTO.setFeatures(targetDto.getFeatures());
        }
        if (changedFields.contains(GameFields.LINKS)) {
            diffDTO.setLinks(targetDto.getLinks());
        }
        if (changedFields.contains(GameFields.PLAYTIME)) {
            diffDTO.setPlaytime(targetDto.getPlaytime());
        }
        if (changedFields.contains(GameFields.ADDED)) {
            diffDTO.setAdded(targetDto.getAdded());
        }
        if (changedFields.contains(GameFields.MODIFIED)) {
            diffDTO.setModified(targetDto.getModified());
        }
        if (changedFields.contains(GameFields.PLAY_COUNT)) {
            diffDTO.setPlayCount(targetDto.getPlayCount());
        }
        if (changedFields.contains(GameFields.INSTALL_SIZE)) {
            diffDTO.setInstallSize(targetDto.getInstallSize());
        }
        if (changedFields.contains(GameFields.LAST_SIZE_SCAN_DATE)) {
            diffDTO.setLastSizeScanDate(targetDto.getLastSizeScanDate());
        }
        if (changedFields.contains(GameFields.SERIES)) {
            diffDTO.setSeries(targetDto.getSeries());
        }
        if (changedFields.contains(GameFields.VERSION)) {
            diffDTO.setVersion(targetDto.getVersion());
        }
        if (changedFields.contains(GameFields.AGE_RATINGS)) {
            diffDTO.setAgeRatings(targetDto.getAgeRatings());
        }
        if (changedFields.contains(GameFields.REGIONS)) {
            diffDTO.setRegions(targetDto.getRegions());
        }
        if (changedFields.contains(GameFields.SOURCE)) {
            diffDTO.setSource(targetDto.getSource());
        }
        if (changedFields.contains(GameFields.COMPLETION_STATUS)) {
            diffDTO.setCompletionStatus(targetDto.getCompletionStatus());
        }
        if (changedFields.contains(GameFields.USER_SCORE)) {
            diffDTO.setUserScore(targetDto.getUserScore());
        }
        if (changedFields.contains(GameFields.CRITIC_SCORE)) {
            diffDTO.setCriticScore(targetDto.getCriticScore());
        }
        if (changedFields.contains(GameFields.COMMUNITY_SCORE)) {
            diffDTO.setCommunityScore(targetDto.getCommunityScore());
        }
        if (changedFields.contains(GameFields.MANUAL)) {
            diffDTO.setManual(targetDto.getManual());
        }
    }

    @Override
    protected GameDTO createDTO() {
        return new GameDTO();
    }

    @Override
    protected GameDiffDTO createDiffDTO() {
        return new GameDiffDTO();
    }

    @Override
    protected GameDiff createDiffEntity() {
        return new GameDiff();
    }

    @Override
    protected Class<GameDiffDTO> getDiffClass() {
        return GameDiffDTO.class;
    }

    @Override
    public GameDiff toEntity(GameDiffDTO dto) {
        GameDiff entity = super.toEntity(dto);
        entity.setGameId(dto.getGameId());
        entity.setPluginId(dto.getPluginId());
        return entity;
    }
}
