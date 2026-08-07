package pl.yalgrin.playnite.simplesync.helper

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.util.context.ContextView
import reactor.util.retry.Retry
import java.time.Duration


@Component
class SingleExecutorHelper {
    private val sink: Sinks.Many<RequestTask> = Sinks.many().unicast().onBackpressureBuffer<RequestTask>()
    private val sinkScheduler = Schedulers.newSingle("sink-scheduler")

    private var subscription: Disposable? = null

    @PostConstruct
    fun init() {
        subscription = sink.asFlux()
            .concatMap { task ->
                Mono.defer {
                    task.request
                        .contextWrite { ctx ->
                            ctx.putAll(task.contextView)
                        }
                }
                    .doOnSuccess {
                        task.resultSink.emitValue(it, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(500)))
                    }
                    .doOnError {
                        task.resultSink.emitError(it, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(500)))
                    }
                    .onErrorResume { Mono.empty() }
                    .then(Mono.empty())
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    @PreDestroy
    fun shutdown() {
        subscription?.dispose()
        sink.tryEmitComplete()
        sinkScheduler.dispose()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> runOnExecutor(mono: Mono<T>): Mono<T> {
        return Mono.deferContextual { contextView ->
            val resultSink = Sinks.one<Any>()
            val task = RequestTask(mono, contextView, resultSink)

            val taskSubscription = emitToSink(task)
                .doOnError { resultSink.emitError(it, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(500))) }
                .subscribe()
            resultSink.asMono()
                .map { it as T }
                .doOnTerminate { taskSubscription.dispose() }
                .doOnCancel { taskSubscription.dispose() }
        }
    }

    fun <T : Any> runOnExecutor(flux: Flux<T>): Flux<T> {
        return runOnExecutor(flux.collectList()).flatMapIterable { it }
    }

    private fun emitToSink(task: RequestTask): Mono<Any> {
        return Mono.fromCallable {
            sink.tryEmitNext(task)
        }.filter {
            it == Sinks.EmitResult.FAIL_OVERFLOW || it == Sinks.EmitResult.FAIL_NON_SERIALIZED || it == Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER
        }.flatMap { Mono.error { IllegalStateException("Failed to enqueue request: $it") } }
            .retryWhen(Retry.backoff(10, Duration.ofMillis(100)).filter { it is IllegalStateException })
            .subscribeOn(sinkScheduler)
    }


    private data class RequestTask(val request: Mono<*>, val contextView: ContextView, val resultSink: Sinks.One<Any>)
}