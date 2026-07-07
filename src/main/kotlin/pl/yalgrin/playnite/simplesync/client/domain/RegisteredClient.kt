package pl.yalgrin.playnite.simplesync.client.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("registered_client")
data class RegisteredClient(
    @Id
    @Column("id")
    var clientId: String,

    @Column("display_name")
    var displayName: String,

    @Column("client_token")
    var clientToken: String,

    @Column("last_connected")
    var lastConnected: Instant? = null,

    @Transient
    var newEntity: Boolean = false
) : Persistable<String> {
    override fun getId(): String = clientId

    override fun isNew(): Boolean = newEntity

    override fun toString(): String {
        return "RegisteredClient(displayName='$displayName', lastConnected=$lastConnected)"
    }


}
