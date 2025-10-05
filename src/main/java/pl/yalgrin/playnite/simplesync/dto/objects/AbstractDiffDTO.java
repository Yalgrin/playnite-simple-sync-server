package pl.yalgrin.playnite.simplesync.dto.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.dto.AbstractDTO;

import java.io.Serial;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class AbstractDiffDTO extends AbstractDTO {
    @Serial
    private static final long serialVersionUID = 5102493534327777327L;

    @JsonProperty("Id")
    protected String id;
    @JsonProperty("Name")
    protected String name;
    @JsonProperty("BaseObjectId")
    protected Long baseObjectId;
    @JsonProperty("ChangedFields")
    protected List<String> changedFields;
    @JsonProperty("Removed")
    protected boolean removed;
}
