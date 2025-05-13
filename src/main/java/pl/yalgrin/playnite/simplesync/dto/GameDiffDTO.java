package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static pl.yalgrin.playnite.simplesync.dto.GameDiffDTO.Fields.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class GameDiffDTO extends AbstractDiffDTO {

    @JsonProperty(DESCRIPTION)
    private String description;

    @JsonProperty(NOTES)
    private String notes;

    @JsonProperty(GENRES)
    private List<GenreDTO> genres;

    @JsonProperty(HIDDEN)
    private boolean hidden;

    @JsonProperty(FAVORITE)
    private boolean favorite;

    @JsonProperty(LAST_ACTIVITY)
    private ZonedDateTime lastActivity;

    @JsonProperty(SORTING_NAME)
    private String sortingName;

    @JsonProperty(GAME_ID)
    private String gameId;

    @JsonProperty(PLUGIN_ID)
    private String pluginId;

    @JsonProperty(INCLUDE_LIBRARY_PLUGIN_ACTION)
    private boolean includeLibraryPluginAction;

    @JsonProperty(PLATFORMS)
    private List<PlatformDTO> platforms;

    @JsonProperty(PUBLISHERS)
    private List<CompanyDTO> publishers;

    @JsonProperty(DEVELOPERS)
    private List<CompanyDTO> developers;

    @JsonProperty(RELEASE_DATE)
    private LocalDateTime releaseDate;

    @JsonProperty(CATEGORIES)
    private List<CategoryDTO> categories;

    @JsonProperty(TAGS)
    private List<TagDTO> tags;

    @JsonProperty(FEATURES)
    private List<FeatureDTO> features;

    @JsonProperty(LINKS)
    private List<LinkDTO> links;

    @JsonProperty(PLAYTIME)
    private long playtime;

    @JsonProperty(PLAYTIME_DIFF)
    private Long playtimeDiff;

    @JsonProperty(ADDED)
    private ZonedDateTime added;

    @JsonProperty(MODIFIED)
    private ZonedDateTime modified;

    @JsonProperty(PLAY_COUNT)
    private long playCount;

    @JsonProperty(PLAY_COUNT_DIFF)
    private Long playCountDiff;

    @JsonProperty(INSTALL_SIZE)
    private Long installSize;

    @JsonProperty(LAST_SIZE_SCAN_DATE)
    private ZonedDateTime lastSizeScanDate;

    @JsonProperty(SERIES)
    private List<SeriesDTO> series;

    @JsonProperty(VERSION)
    private String version;

    @JsonProperty(AGE_RATINGS)
    private List<AgeRatingDTO> ageRatings;

    @JsonProperty(REGIONS)
    private List<RegionDTO> regions;

    @JsonProperty(SOURCE)
    private SourceDTO source;

    @JsonProperty(COMPLETION_STATUS)
    private CompletionStatusDTO completionStatus;

    @JsonProperty(USER_SCORE)
    private Integer userScore;

    @JsonProperty(CRITIC_SCORE)
    private Integer criticScore;

    @JsonProperty(COMMUNITY_SCORE)
    private Integer communityScore;

    @JsonProperty(MANUAL)
    private String manual;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("removed", removed)
                .toString();
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields extends AbstractObjectDTO.Fields {
        public static final String DESCRIPTION = "Description";
        public static final String NOTES = "Notes";
        public static final String GENRES = "Genres";
        public static final String HIDDEN = "Hidden";
        public static final String FAVORITE = "Favorite";
        public static final String LAST_ACTIVITY = "LastActivity";
        public static final String SORTING_NAME = "SortingName";
        public static final String GAME_ID = "GameId";
        public static final String PLUGIN_ID = "PluginId";
        public static final String INCLUDE_LIBRARY_PLUGIN_ACTION = "IncludeLibraryPluginAction";
        public static final String PLATFORMS = "Platforms";
        public static final String PUBLISHERS = "Publishers";
        public static final String DEVELOPERS = "Developers";
        public static final String RELEASE_DATE = "ReleaseDate";
        public static final String CATEGORIES = "Categories";
        public static final String TAGS = "Tags";
        public static final String FEATURES = "Features";
        public static final String LINKS = "Links";
        public static final String PLAYTIME = "Playtime";
        public static final String PLAYTIME_DIFF = "PlaytimeDiff";
        public static final String ADDED = "Added";
        public static final String MODIFIED = "Modified";
        public static final String PLAY_COUNT = "PlayCount";
        public static final String PLAY_COUNT_DIFF = "PlayCountDiff";
        public static final String INSTALL_SIZE = "InstallSize";
        public static final String LAST_SIZE_SCAN_DATE = "LastSizeScanDate";
        public static final String SERIES = "Series";
        public static final String VERSION = "Version";
        public static final String AGE_RATINGS = "AgeRatings";
        public static final String REGIONS = "Regions";
        public static final String SOURCE = "Source";
        public static final String COMPLETION_STATUS = "CompletionStatus";
        public static final String USER_SCORE = "UserScore";
        public static final String CRITIC_SCORE = "CriticScore";
        public static final String COMMUNITY_SCORE = "CommunityScore";
        public static final String MANUAL = "Manual";
    }
}
