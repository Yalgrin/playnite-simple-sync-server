package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GameChangeRequestDTO {
    @JsonProperty("Ids")
    private List<String> ids;
    @JsonProperty("GameIds")
    private List<GameIdsDTO> gameIds;
}
