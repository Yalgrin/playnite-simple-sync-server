package pl.yalgrin.playnite.simplesync.common.util

import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun <T : Any> Flux<T>.first(): Mono<T> = this.take(1).next()

fun <T : Any, U : Any> Mono<T>.pairWith(other: Mono<U>): Mono<Pair<T, U>> = this.zipWith(other).map { it.t1 to it.t2 }

fun <T : Any> Mono<*>.thenAny(): Mono<T> = this.then(Mono.empty())

fun <T : Any> Flux<*>.thenAny(): Mono<T> = this.then(Mono.empty())

fun <T : Any> Mono<T>.transactional(transactionManager: ReactiveTransactionManager): Mono<T> {
    val definition = DefaultTransactionAttribute()
    val operator = TransactionalOperator.create(transactionManager, definition)
    return operator.transactional(this)
}

fun <T : Any> Mono<T>.transactionalReadOnly(transactionManager: ReactiveTransactionManager): Mono<T> {
    val definition = DefaultTransactionAttribute()
    definition.isReadOnly = true
    val operator = TransactionalOperator.create(transactionManager, definition)
    return operator.transactional(this)
}

