package pl.yalgrin.playnite.simplesync.dto.objects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.yalgrin.playnite.simplesync.utils.ToStringUtils;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CompletionStatusDTO extends AbstractObjectDTO {
    @Serial
    private static final long serialVersionUID = -2262088203554756129L;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("removed", removed)
                .toString();
    }
}
