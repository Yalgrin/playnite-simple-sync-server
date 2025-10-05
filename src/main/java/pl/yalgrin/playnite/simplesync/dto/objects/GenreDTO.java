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
public class GenreDTO extends AbstractObjectDTO {
    @Serial
    private static final long serialVersionUID = 2567585936441014917L;

    @Override
    public String toString() {
        return ToStringUtils.createBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("removed", removed)
                .toString();
    }
}
