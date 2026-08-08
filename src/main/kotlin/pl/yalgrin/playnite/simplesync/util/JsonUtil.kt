package pl.yalgrin.playnite.simplesync.util

import io.r2dbc.postgresql.codec.Json
import pl.yalgrin.playnite.simplesync.common.config.buildJsonMapper
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

private val jsonMapper = buildJsonMapper()

fun <T : Any> Json?.asObject(clazz: Class<T>): Mono<T> {
    return this?.let { json ->
        Mono.fromCallable {
            jsonMapper.readValue<T>(json.asArray(), clazz)
        }.subscribeOn(Schedulers.parallel())
    } ?: Mono.empty()
}