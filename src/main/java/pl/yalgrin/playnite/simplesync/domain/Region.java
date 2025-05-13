package pl.yalgrin.playnite.simplesync.domain;

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
@Table("playnite_region")
public class Region extends AbstractObjectEntity {
    @Id
    @Column("id")
    private Long id;

    @Column("playnite_id")
    private String playniteId;

    @Column("name")
    private String name;

    @Column("specification_id")
    private String specificationId;

    @Column("removed")
    private boolean removed;

    @Transient
    private boolean notifyAll;

    @Transient
    private boolean changed;
}
