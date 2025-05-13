package pl.yalgrin.playnite.simplesync.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractDTO implements Serializable {
}
