package pl.yalgrin.playnite.simplesync.change.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux

@Service
class ChangeListenerService {
    private val sink = Sinks.many().multicast().directBestEffort<ChangeDTO>()
    private val scheduler = Schedulers.newSingle("change-emitter")

    companion object {
        private val log = LoggerFactory.getLogger(ChangeListenerService::class.java)
    }

    fun publishChange(dto: ChangeDTO): Mono<Void> {
        return Mono.fromRunnable<Unit> {
            log.debug("publishing {}", dto)
            sink.tryEmitNext(dto)
        }.subscribeOn(scheduler).then()
    }

    fun publishChanges(dtoList: List<ChangeDTO>): Mono<Void> {
        return dtoList.toFlux()
            .concatMap { dto -> publishChange(dto) }
            .then()
    }

    fun modificationFlux(): Flux<ChangeDTO> {
        return sink.asFlux()
    }
}