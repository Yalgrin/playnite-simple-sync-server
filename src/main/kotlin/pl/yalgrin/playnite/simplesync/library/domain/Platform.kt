package pl.yalgrin.playnite.simplesync.library.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("playnite_platform")
data class Platform(
    @Id
    @Column("id")
    override var id: Long? = null,

    @Column("playnite_id")
    override var playniteId: String? = null,

    @Column("name")
    override var name: String? = null,

    @Column("specification_id")
    var specificationId: String? = null,

    @Column("icon_md5")
    var iconMd5: String? = null,

    @Column("cover_image_md5")
    var coverImageMd5: String? = null,

    @Column("background_image_md5")
    var backgroundImageMd5: String? = null,

    @Column("removed")
    override var isRemoved: Boolean = false,

    @Transient
    override var isNotifyAll: Boolean = false,

    @Transient
    override var isChanged: Boolean = false
) : LibraryObjectEntity