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

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("playnite_game")
public class Game extends AbstractObjectEntity {
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
    private Json savedData;

    @Column("icon_md5")
    private String iconMd5;

    @Column("cover_image_md5")
    private String coverImageMd5;

    @Column("background_image_md5")
    private String backgroundImageMd5;

    @Column("removed")
    private boolean removed;

    @Transient
    private boolean notifyAll;

    @Transient
    private boolean changed;
}
