package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class PlatformDiffDTO extends AbstractDiffDTO {
    @JsonProperty("SpecificationId")
    private String specificationId;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("baseObjectId", baseObjectId)
                .append("changedFields", changedFields)
                .append("removed", removed)
                .append("specificationId", specificationId)
                .toString();
    }
}
