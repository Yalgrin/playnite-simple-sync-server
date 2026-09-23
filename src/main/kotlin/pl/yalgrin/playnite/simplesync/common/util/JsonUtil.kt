package pl.yalgrin.playnite.simplesync.common.util

import io.r2dbc.postgresql.codec.Json
import pl.yalgrin.playnite.simplesync.common.config.buildJsonMapper
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import tools.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val jsonMapper = buildJsonMapper()

fun <T : Any> Json?.asObject(clazz: Class<T>): Mono<T> = this?.let { json ->
    Mono.fromCallable {
        jsonMapper.readValue<T>(json.asArray(), clazz)
    }.subscribeOn(Schedulers.parallel())
} ?: Mono.empty()

fun <T : Any> T?.asJson(): Mono<Json> = this?.let { obj ->
    Mono.fromCallable {
        jsonMapper.writeValueAsString(obj)
    }.subscribeOn(Schedulers.parallel()).map { obj -> Json.of(obj) }
} ?: Mono.empty()

fun Json?.asJsonNode(): Mono<JsonNode> = Mono.defer {
    this?.let { json ->
        Mono.fromCallable {
            jsonMapper.readTree(json.asArray())
        }.subscribeOn(Schedulers.parallel())
    } ?: Mono.empty()
}

fun JsonNode.toJson(): Mono<Json> = Mono.defer {
    Mono.fromCallable {
        Json.of(jsonMapper.writeValueAsString(this))
    }.subscribeOn(Schedulers.parallel())
}

fun JsonNode?.extractLocalDateTime(paramName: String): LocalDateTime? {
    if (this == null) {
        return null
    }
    val node = this.get(paramName)
    return if (node != null && !node.isNull) {
        try {
            LocalDateTime.parse(node.asString(), DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}

fun JsonNode?.extractInt(name: String): Int? {
    if (this == null) return null
    val node = get(name)
    return if (node != null && !node.isNull) {
        try {
            node.asInt()
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}

fun JsonNode?.extractLong(name: String): Long? {
    if (this == null) return null
    val node = get(name)
    return if (node != null && !node.isNull) {
        try {
            node.asLong()
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }
}

fun JsonNode?.extractZonedDateTime(name: String): ZonedDateTime? {
    if (this == null) return null
    val node = get(name)
    return if (node != null && !node.isNull) {
        try {
            ZonedDateTime.parse(node.asString())
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }
}

fun ZonedDateTime?.toIsoString(): String? {
    if (this == null) return null
    return DateTimeFormatter.ISO_INSTANT.format(this)
}