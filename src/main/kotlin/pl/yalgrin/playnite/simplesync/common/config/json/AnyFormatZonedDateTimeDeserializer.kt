package pl.yalgrin.playnite.simplesync.common.config.json

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class AnyFormatZonedDateTimeDeserializer :
    StdDeserializer<ZonedDateTime>(ZonedDateTime::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ZonedDateTime? {
        val value = p?.string
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            ZonedDateTime.parse(value)
        } catch (_: DateTimeParseException) {
            LocalDateTime.parse(value).atZone(ZoneId.systemDefault())
        }
    }
}