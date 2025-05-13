package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractDiffDTO extends AbstractDTO {
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
