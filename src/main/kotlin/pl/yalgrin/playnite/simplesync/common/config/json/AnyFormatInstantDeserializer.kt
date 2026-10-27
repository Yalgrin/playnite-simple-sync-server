package pl.yalgrin.playnite.simplesync.common.config.json

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

class AnyFormatInstantDeserializer : StdDeserializer<Instant>(Instant::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Instant? {
        val value = p?.string
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            OffsetDateTime.parse(value).toInstant()
        } catch (_: DateTimeParseException) {
            LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant()
        }
    }
}