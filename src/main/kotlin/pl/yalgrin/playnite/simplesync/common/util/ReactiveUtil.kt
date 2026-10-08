package pl.yalgrin.playnite.simplesync.common.util

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

fun <T : Any> Flux<T>.first(): Mono<T> = this.take(1).next()

fun <T : Any, U : Any> Mono<T>.pairWith(other: Mono<U>): Mono<Pair<T, U>> = this.zipWith(other).map { it.t1 to it.t2 }

fun <T : Any> Mono<*>.thenAny(): Mono<T> = this.then(Mono.empty())

fun <T : Any> Flux<*>.thenAny(): Mono<T> = this.then(Mono.empty())

fun <T : Any> Mono<T>.defaultIfEmpty(supplier: () -> T): Mono<T> =
    this.switchIfEmpty { Mono.fromSupplier { supplier() } }