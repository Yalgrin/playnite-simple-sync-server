package pl.yalgrin.playnite.simplesync.helper

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.util.context.ContextView
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean


@Component
class SingleExecutorHelper {
    private val sink: Sinks.Many<RequestTask> = Sinks.many().unicast().onBackpressureBuffer()
    private val sinkScheduler = Schedulers.newSingle("sink-scheduler")

    protected val log: Logger by lazy { LoggerFactory.getLogger(AopUtils.getTargetClass(this)) }

    private var subscription: Disposable? = null

    @PostConstruct
    fun init() {
        subscription = sink.asFlux().concatMap { task ->
                Mono.defer {
                    task.request.contextWrite { ctx ->
                            ctx.putAll(task.contextView)
                    }.takeUntilOther(task.cancelSignal.asMono())
                }.doOnSuccess { value ->
                    if (!task.cancelled.get()) {
                        task.resultSink.emitValue(
                            value, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(500))
                        )
                    }
                }.doOnError { error ->
                    if (!task.cancelled.get()) {
                        task.resultSink.emitError(
                            error, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(500))
                        )
                    }
                }.doFinally { signalType ->
                    if (signalType == SignalType.CANCEL || task.cancelled.get()) {
                        task.resultSink.tryEmitError(CancellationException("Task cancelled"))
                    }
                }.onErrorResume { Mono.empty() }.then(Mono.empty())
        }.subscribeOn(Schedulers.boundedElastic()).subscribe()
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
            val cancelSignal = Sinks.one<Unit>()
            val cancelled = AtomicBoolean(false)
            val task = RequestTask(mono, contextView, resultSink, cancelSignal, cancelled)

            val taskSubscription = emitToSink(task).doOnError {
                resultSink.emitError(it, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(500)))
            }.subscribe()

            resultSink.asMono()
                .map { it as T }
                .doOnTerminate { taskSubscription.dispose() }
                .doOnCancel {
                    cancelled.set(true)
                    cancelSignal.tryEmitValue(Unit)
                    taskSubscription.dispose()
                }
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


    private data class RequestTask(
        val request: Mono<*>,
        val contextView: ContextView,
        val resultSink: Sinks.One<Any>,
        val cancelSignal: Sinks.One<Unit>,
        val cancelled: AtomicBoolean
    )
}