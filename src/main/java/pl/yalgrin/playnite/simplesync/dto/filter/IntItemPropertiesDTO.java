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
public class IntItemPropertiesDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6747197307373352936L;

    @JsonProperty("Values")
    private List<Integer> values;
}
