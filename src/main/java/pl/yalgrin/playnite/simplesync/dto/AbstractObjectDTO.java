package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static pl.yalgrin.playnite.simplesync.dto.AbstractObjectDTO.Fields.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class AbstractObjectDTO extends AbstractDTO {

    @JsonProperty(ID)
    protected String id;
    @JsonProperty(NAME)
    protected String name;
    @JsonProperty(REMOVED)
    protected boolean removed;

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {
        public static final String ID = "Id";
        public static final String NAME = "Name";
        public static final String REMOVED = "Removed";
    }
}
