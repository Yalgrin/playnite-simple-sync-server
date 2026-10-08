package pl.yalgrin.playnite.simplesync.common.helper

import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono

interface ITransactional {
    val reactiveTransactionManager: ReactiveTransactionManager

    fun <T : Any> Mono<T>.transactional(): Mono<T> = Mono.defer {
        val definition = DefaultTransactionAttribute()
        val operator = TransactionalOperator.create(reactiveTransactionManager, definition)
        operator.transactional(this)
    }

    fun <T : Any> Mono<T>.transactionalReadOnly(): Mono<T> = Mono.defer {
        val definition = DefaultTransactionAttribute()
        definition.isReadOnly = true
        val operator = TransactionalOperator.create(reactiveTransactionManager, definition)
        operator.transactional(this)
    }
}