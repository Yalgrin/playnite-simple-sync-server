package pl.yalgrin.playnite.simplesync.common.aop

import io.vavr.Tuple
import io.vavr.Tuple2
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import pl.yalgrin.playnite.simplesync.security.getSessionClientId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Aspect
@Component
class ReactiveLoggingAspect {
    @Pointcut("execution(* pl.yalgrin.playnite.simplesync.service..*.*(..)) || execution(* pl.yalgrin.playnite.simplesync.*.service..*.*(..))")
    fun servicePointcut() = Unit

    @Pointcut("execution(* pl.yalgrin.playnite.simplesync.web..*.*(..))")
    fun resourcePointcut() = Unit

    @Around("servicePointcut() || resourcePointcut()")
    @Throws(Throwable::class)
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val signature: Signature = joinPoint.signature
        val name: String = signature.name
        val args: Array<Any?> = joinPoint.args
        val returnValue: Any? = joinPoint.proceed()
        val loggerClass: Class<*> = joinPoint.target?.javaClass ?: signature.declaringType
        val logger: Logger = LoggerFactory.getLogger(loggerClass)
        if (returnValue != null) {
            if (returnValue is Mono<*>) {
                val uuid: UUID = UUID.randomUUID()
                return getSessionClientId().defaultIfEmpty("(none)").flatMap { clientId ->
                    returnValue.doOnSubscribe {
                        logger.debug(
                            "{} > START, uuid: {}, clientId: {}, args: {}",
                            name,
                            uuid,
                            clientId,
                            formatArgs(args)
                        )
                    }.doOnSuccess { result ->
                        logger.debug(
                            "{} > END, uuid: {}, clientId: {}, result: {}",
                            name,
                            uuid,
                            clientId,
                            formatResult(result)
                        )
                    }.doOnError { th ->
                        logger.error(
                            "{} > ERROR, uuid: {}, clientId: {}", name, uuid, clientId, th
                        )
                    }.doOnCancel {
                        logger.debug("{} > CANCEL, uuid: {}, clientId: {}", name, uuid, clientId)
                    }
                }
            }
            if (returnValue is Flux<*>) {
                val uuid: UUID = UUID.randomUUID()
                val counter = AtomicInteger(0)
                return getSessionClientId().defaultIfEmpty("(none)").flatMapMany { clientId ->
                    returnValue.doOnSubscribe {
                        logger.debug(
                            "{} > START, uuid: {}, clientId: {}, args: {}",
                            name,
                            uuid,
                            clientId,
                            formatArgs(args)
                        )
                    }.doOnNext { counter.incrementAndGet() }.doOnComplete {
                        logger.debug(
                            "{} > END, uuid: {}, clientId: {}, result.size(): {}",
                            name,
                            uuid,
                            clientId,
                            counter.get()
                        )
                    }.doOnError { th ->
                        logger.error("{} > ERROR, uuid: {}, clientId: {}", name, uuid, clientId, th)
                    }.doOnCancel {
                        logger.debug("{} > CANCEL, uuid: {}, clientId: {}", name, uuid, clientId)
                    }
                }
            }
        }
        return returnValue
    }

    private fun formatArgs(args: Array<Any?>): List<Any?> {
        return args.filter { shouldIncludeArg(it) }.map { formatResult(it) }
    }

    private fun shouldIncludeArg(arg: Any?): Boolean {
        if (arg is ServerWebExchange) {
            return false
        }
        return true
    }

    private fun formatResult(result: Any?): Any? {
        return if (result is ByteArray) {
            "byte[${result.size}]"
        } else if (result is Flux<*>) {
            "Flux"
        } else if (result is Tuple2<*, *>) {
            Tuple.of(formatResult(result._1), formatResult(result._2))
        } else if (result is Collection<*>) {
            "Collection[${result.size}]: ${truncateResult(result)}"
        } else {
            truncateResult(result)
        }
    }

    private fun truncateResult(result: Any?): String? {
        result?.let { r ->
            val str = r.toString()
            if (str.length > 200) {
                return str.substring(0, 200) + "..."
            }
            return str
        }
        return null
    }
}
