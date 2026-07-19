package pl.yalgrin.playnite.simplesync.dto.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@SuperBuilder
public class FilterPresetSettingsDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1511413271560442002L;

    @JsonProperty("UseAndFilteringStyle")
    private boolean useAndFilteringStyle;
    @JsonProperty("IsInstalled")
    private boolean installed;
    @JsonProperty("IsUnInstalled")
    private boolean uninstalled;
    @JsonProperty("Hidden")
    private boolean hidden;
    @JsonProperty("Favorite")
    private boolean favorite;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Version")
    private String version;

    @JsonProperty("ReleaseYear")
    private StringItemPropertiesDTO releaseYear;

    @JsonProperty("Genre")
    private IdItemPropertiesDTO genre;
    @JsonProperty("Platform")
    private IdItemPropertiesDTO platform;
    @JsonProperty("Publisher")
    private IdItemPropertiesDTO publisher;
    @JsonProperty("Developer")
    private IdItemPropertiesDTO developer;
    @JsonProperty("Category")
    private IdItemPropertiesDTO category;
    @JsonProperty("Tag")
    private IdItemPropertiesDTO tag;
    @JsonProperty("Series")
    private IdItemPropertiesDTO series;
    @JsonProperty("Region")
    private IdItemPropertiesDTO region;
    @JsonProperty("Source")
    private IdItemPropertiesDTO source;
    @JsonProperty("AgeRating")
    private IdItemPropertiesDTO ageRating;
    @JsonProperty("Library")
    private IdItemPropertiesDTO library;
    @JsonProperty("CompletionStatuses")
    private IdItemPropertiesDTO completionStatuses;
    @JsonProperty("Feature")
    private IdItemPropertiesDTO feature;

    @JsonProperty("UserScore")
    private IntItemPropertiesDTO userScore;
    @JsonProperty("CriticScore")
    private IntItemPropertiesDTO criticScore;
    @JsonProperty("CommunityScore")
    private IntItemPropertiesDTO communityScore;
    @JsonProperty("LastActivity")
    private IntItemPropertiesDTO lastActivity;
    @JsonProperty("RecentActivity")
    private IntItemPropertiesDTO recentActivity;
    @JsonProperty("Added")
    private IntItemPropertiesDTO added;
    @JsonProperty("Modified")
    private IntItemPropertiesDTO modified;
    @JsonProperty("PlayTime")
    private IntItemPropertiesDTO playTime;
    @JsonProperty("InstallSize")
    private IntItemPropertiesDTO installSize;
}
