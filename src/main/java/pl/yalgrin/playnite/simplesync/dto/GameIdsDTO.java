package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GameIdsDTO extends AbstractDTO {
    @Serial
    private static final long serialVersionUID = 3080905186398003975L;

    @JsonProperty("GameId")
    private String gameId;
    @JsonProperty("PluginId")
    private String pluginId;
}
