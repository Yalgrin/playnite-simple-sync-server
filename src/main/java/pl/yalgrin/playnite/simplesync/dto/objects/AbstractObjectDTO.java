package pl.yalgrin.playnite.simplesync.dto.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.dto.AbstractDTO;

import java.io.Serial;

import static pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO.Fields.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class AbstractObjectDTO extends AbstractDTO {

    @Serial
    private static final long serialVersionUID = 9149381529424976782L;

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
