package pl.yalgrin.playnite.simplesync.dto.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PlatformDTO extends AbstractObjectDTO {
    @Serial
    private static final long serialVersionUID = 7372670492047597689L;

    @JsonProperty("SpecificationId")
    private String specificationId;
    @JsonProperty("HasIcon")
    private boolean hasIcon;
    @JsonProperty("HasCoverImage")
    private boolean hasCoverImage;
    @JsonProperty("HasBackgroundImage")
    private boolean hasBackgroundImage;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("removed", removed)
                .append("specificationId", specificationId)
                .append("hasIcon", hasIcon)
                .append("hasCoverImage", hasCoverImage)
                .append("hasBackgroundImage", hasBackgroundImage)
                .toString();
    }
}
