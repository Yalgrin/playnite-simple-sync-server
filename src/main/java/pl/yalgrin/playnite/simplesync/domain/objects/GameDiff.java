package pl.yalgrin.playnite.simplesync.domain.objects;

import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pl.yalgrin.playnite.simplesync.dto.objects.PlatformDiffDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("playnite_game_diff")
public class GameDiff extends AbstractObjectDiffEntity {
    @Id
    @Column("id")
    private Long id;

    @Column("playnite_id")
    private String playniteId;

    @Column("game_id")
    private String gameId;

    @Column("plugin_id")
    private String pluginId;

    @Column("name")
    private String name;

    @Column("contents")
    private Json diffData;

    @Column("removed")
    private boolean removed;

    @Transient
    private boolean notifyAll;

    @Transient
    private boolean changed;

    @Transient
    private PlatformDiffDTO diffDTO;
}
