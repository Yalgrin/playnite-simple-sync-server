package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ChangeDTO extends AbstractDTO {
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("Type")
    private ObjectType type;
    @JsonProperty("ClientId")
    private String clientId;
    @JsonProperty("ObjectId")
    private Long objectId;
    @JsonProperty("ForceFetch")
    private boolean forceFetch;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("clientId", clientId)
                .append("objectId", objectId)
                .append("forceFetch", forceFetch)
                .toString();
    }
}
