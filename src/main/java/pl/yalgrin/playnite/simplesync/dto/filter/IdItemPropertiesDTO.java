package pl.yalgrin.playnite.simplesync.dto.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@SuperBuilder
public class IdItemPropertiesDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3854104084958281184L;

    @JsonProperty("Ids")
    private List<String> ids;
    @JsonProperty("Text")
    private String text;
}
