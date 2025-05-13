package pl.yalgrin.playnite.simplesync.dto.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@SuperBuilder
public class FilterPresetSettingsDTO implements Serializable {
    @JsonProperty("UseAndFilteringStyle")
    public boolean useAndFilteringStyle;
    @JsonProperty("IsInstalled")
    public boolean installed;
    @JsonProperty("IsUnInstalled")
    public boolean uninstalled;
    @JsonProperty("Hidden")
    public boolean hidden;
    @JsonProperty("Favorite")
    public boolean favorite;
    @JsonProperty("Name")
    public String name;
    @JsonProperty("Version")
    public String version;

    @JsonProperty("ReleaseYear")
    public StringItemPropertiesDTO releaseYear;

    @JsonProperty("Genre")
    public IdItemPropertiesDTO genre;
    @JsonProperty("Platform")
    public IdItemPropertiesDTO platform;
    @JsonProperty("Publisher")
    public IdItemPropertiesDTO publisher;
    @JsonProperty("Developer")
    public IdItemPropertiesDTO developer;
    @JsonProperty("Category")
    public IdItemPropertiesDTO category;
    @JsonProperty("Tag")
    public IdItemPropertiesDTO tag;
    @JsonProperty("Series")
    public IdItemPropertiesDTO series;
    @JsonProperty("Region")
    public IdItemPropertiesDTO region;
    @JsonProperty("Source")
    public IdItemPropertiesDTO source;
    @JsonProperty("AgeRating")
    public IdItemPropertiesDTO ageRating;
    @JsonProperty("Library")
    public IdItemPropertiesDTO library;
    @JsonProperty("CompletionStatuses")
    public IdItemPropertiesDTO completionStatuses;
    @JsonProperty("Feature")
    public IdItemPropertiesDTO feature;

    @JsonProperty("UserScore")
    public IntItemPropertiesDTO userScore;
    @JsonProperty("CriticScore")
    public IntItemPropertiesDTO criticScore;
    @JsonProperty("CommunityScore")
    public IntItemPropertiesDTO communityScore;
    @JsonProperty("LastActivity")
    public IntItemPropertiesDTO lastActivity;
    @JsonProperty("RecentActivity")
    public IntItemPropertiesDTO recentActivity;
    @JsonProperty("Added")
    public IntItemPropertiesDTO added;
    @JsonProperty("Modified")
    public IntItemPropertiesDTO modified;
    @JsonProperty("PlayTime")
    public IntItemPropertiesDTO playTime;
    @JsonProperty("InstallSize")
    public IntItemPropertiesDTO installSize;
}
