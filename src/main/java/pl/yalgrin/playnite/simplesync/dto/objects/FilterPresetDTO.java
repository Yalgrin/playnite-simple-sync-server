package pl.yalgrin.playnite.simplesync.dto.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.dto.filter.FilterPresetSettingsDTO;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class FilterPresetDTO extends AbstractObjectDTO {
    @Serial
    private static final long serialVersionUID = 1587096464739278718L;

    @JsonProperty("Settings")
    private FilterPresetSettingsDTO settings;
    @JsonProperty("SortingOrder")
    private String sortingOrder;
    @JsonProperty("SortingOrderDirection")
    private String sortingOrderDirection;
    @JsonProperty("GroupingOrder")
    private String groupingOrder;
    @JsonProperty("ShowInFullscreenQuickSelection")
    private boolean showInFullscreenQuickSelection;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("removed", removed)
                .append("settings", settings)
                .append("sortingOrder", sortingOrder)
                .append("sortingOrderDirection", sortingOrderDirection)
                .append("groupingOrder", groupingOrder)
                .append("showInFullscreenQuickSelection", showInFullscreenQuickSelection)
                .toString();
    }
}

