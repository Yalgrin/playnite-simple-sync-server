package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ErrorDTO extends AbstractDTO {
    @Serial
    private static final long serialVersionUID = -1744517363772567506L;

    @JsonProperty("Message")
    private String message;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("message", message)
                .toString();
    }
}
