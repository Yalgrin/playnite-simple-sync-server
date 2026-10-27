package pl.yalgrin.playnite.simplesync.common.config.json

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class AnyFormatLocalDateTimeDeserializer :
    StdDeserializer<LocalDateTime>(LocalDateTime::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
        val value = p?.string
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            ZonedDateTime.parse(value).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        } catch (_: DateTimeParseException) {
            LocalDateTime.parse(value)
        }
    }
}