package pl.yalgrin.playnite.simplesync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ErrorDTO {
    @JsonProperty("Message")
    private String message;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("message", message)
                .toString();
    }
}
