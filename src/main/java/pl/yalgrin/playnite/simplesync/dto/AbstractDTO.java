package pl.yalgrin.playnite.simplesync.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class AbstractDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6859933206281608604L;
}
