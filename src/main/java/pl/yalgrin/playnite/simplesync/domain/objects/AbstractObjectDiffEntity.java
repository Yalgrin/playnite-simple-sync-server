package pl.yalgrin.playnite.simplesync.domain.objects;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractObjectDiffEntity {
    public abstract Long getId();

    public abstract void setId(Long id);

    public abstract String getPlayniteId();

    public abstract void setPlayniteId(String playniteId);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Json getDiffData();

    public abstract void setDiffData(Json data);

    public abstract boolean isRemoved();

    public abstract void setRemoved(boolean removed);
}
