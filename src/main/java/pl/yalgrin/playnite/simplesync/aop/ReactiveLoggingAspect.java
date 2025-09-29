package pl.yalgrin.playnite.simplesync.aop;

import lombok.extern.slf4j.Slf4j;
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
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Slf4j
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
                        .publishOn(Schedulers.boundedElastic())
                        .doOnSubscribe(s -> logger.debug("{} > START, uuid: {}, args: {}", name, uuid, args))
                        .doOnSuccess(result -> logger.debug("{} > END, uuid: {}, result: {}", name, uuid, result))
                        .doOnError(th -> logger.error("{} > ERROR, uuid: {}", name, uuid, th));
            }
            if (returnValue instanceof Flux<?> flux) {
                UUID uuid = UUID.randomUUID();
                AtomicInteger counter = new AtomicInteger(0);
                return flux
                        .publishOn(Schedulers.boundedElastic())
                        .doOnSubscribe(s -> logger.debug("{} > START, uuid: {}, args: {}", name, uuid, args))
                        .doOnNext(obj -> counter.incrementAndGet())
                        .doOnComplete(
                                () -> logger.debug("{} > END, uuid: {}, result.size(): {}", name, uuid, counter.get()))
                        .doOnError(th -> logger.error("{} > ERROR, uuid: {}", name, uuid, th));
            }
        }
        return returnValue;
    }
}
