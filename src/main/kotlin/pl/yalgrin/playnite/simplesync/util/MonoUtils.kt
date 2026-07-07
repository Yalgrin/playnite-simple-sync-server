package pl.yalgrin.playnite.simplesync.util

import reactor.core.publisher.Mono

fun <T : Any> Mono<*>.thenAny(): Mono<T> = this.then(Mono.empty())