package pl.yalgrin.playnite.simplesync.dto.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class LinkDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5001751673402015078L;

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Url")
    private String url;
}
