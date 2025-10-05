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
public class RegionDTO extends AbstractObjectDTO {
    @Serial
    private static final long serialVersionUID = -2444990363523234197L;

    @JsonProperty("SpecificationId")
    private String specificationId;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("specificationId", specificationId)
                .append("removed", removed)
                .toString();
    }
}
