package pl.yalgrin.playnite.simplesync.util

import io.r2dbc.postgresql.codec.Json
import pl.yalgrin.playnite.simplesync.common.config.buildJsonMapper
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

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