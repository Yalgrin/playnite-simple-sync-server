package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GameChangeRequestDTO extends AbstractDTO {
    @Serial
    private static final long serialVersionUID = -2983927955354416935L;

    @JsonProperty("Ids")
    private List<String> ids;
    @JsonProperty("GameIds")
    private List<GameIdsDTO> gameIds;
}
