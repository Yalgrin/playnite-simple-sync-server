package pl.yalgrin.playnite.simplesync.panel.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.time.Instant
import java.util.*

@Table("panel_user")
data class PanelUser(
    @Id
    @Column("id")
    var id: UUID? = null,

    @Column("username")
    var username: String,

    @Column("password")
    var password: String,

    @Column("last_login")
    var lastLogin: Instant? = null
) {
    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("id", id)
            .append("username", username)
            .append("lastLogin", lastLogin)
            .toString()
    }
}
