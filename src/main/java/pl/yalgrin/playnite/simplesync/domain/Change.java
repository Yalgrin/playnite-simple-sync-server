package pl.yalgrin.playnite.simplesync.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("playnite_change")
public class Change {
    @Id
    @Column("id")
    private Long id;

    @Column("type")
    private ObjectType type;

    @Column("client_id")
    private String clientId;

    @Column("object_id")
    private Long objectId;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean notifyAll;
}
