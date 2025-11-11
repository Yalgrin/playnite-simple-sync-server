package pl.yalgrin.playnite.simplesync.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class ReactiveLoggingAspect {

    @Pointcut("execution(* pl.yalgrin.playnite.simplesync.service..*.*(..))")
    public void servicePointcut() {
    }

    @Pointcut("execution(* pl.yalgrin.playnite.simplesync.web..*.*(..))")
    public void resourcePointcut() {
    }

    @Around("servicePointcut() || resourcePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String name = signature.getName();
        Object[] args = joinPoint.getArgs();
        Object returnValue = joinPoint.proceed();
        Class<?> loggerClass = Optional.ofNullable(joinPoint.getTarget()).map(Object::getClass)
                .orElseGet(signature::getDeclaringType);
        Logger logger = LoggerFactory.getLogger(loggerClass);
        if (returnValue != null) {
            if (returnValue instanceof Mono<?> mono) {
                UUID uuid = UUID.randomUUID();
                return mono
                        .doOnSubscribe(
                                _ -> logger.debug("{} > START, uuid: {}, args: {}", name, uuid, formatArgs(args)))
                        .doOnSuccess(result -> logger.debug("{} > END, uuid: {}, result: {}", name, uuid,
                                formatResult(result)))
                        .doOnError(th -> logger.error("{} > ERROR, uuid: {}", name, uuid, th))
                        .doOnCancel(() -> logger.debug("{} > CANCEL, uuid: {}", name, uuid));
            }
            if (returnValue instanceof Flux<?> flux) {
                UUID uuid = UUID.randomUUID();
                AtomicInteger counter = new AtomicInteger(0);
                return flux
                        .doOnSubscribe(
                                _ -> logger.debug("{} > START, uuid: {}, args: {}", name, uuid, formatArgs(args)))
                        .doOnNext(_ -> counter.incrementAndGet())
                        .doOnComplete(
                                () -> logger.debug("{} > END, uuid: {}, result.size(): {}", name, uuid, counter.get()))
                        .doOnError(th -> logger.error("{} > ERROR, uuid: {}", name, uuid, th))
                        .doOnCancel(() -> logger.debug("{} > CANCEL, uuid: {}", name, uuid));
            }
        }
        return returnValue;
    }

    private static List<Object> formatArgs(Object[] args) {
        return Arrays.stream(args).map(ReactiveLoggingAspect::formatResult).toList();
    }

    private static Object formatResult(Object result) {
        if (result instanceof byte[] arr) {
            return "byte[" + arr.length + "]";
        }
        return result;
    }
}
