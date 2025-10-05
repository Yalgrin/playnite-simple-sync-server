package pl.yalgrin.playnite.simplesync.domain.objects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractObjectEntity {
    public abstract Long getId();

    public abstract void setId(Long id);

    public abstract String getPlayniteId();

    public abstract void setPlayniteId(String playniteId);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract boolean isRemoved();

    public abstract void setRemoved(boolean removed);

    public abstract boolean isNotifyAll();

    public abstract void setNotifyAll(boolean notifyAll);

    public abstract boolean isChanged();

    public abstract void setChanged(boolean changed);
}
