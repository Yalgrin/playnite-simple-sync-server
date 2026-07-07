package pl.yalgrin.playnite.simplesync.web.client

import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.client.dto.RegisteredClientDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.message.ConnectionMessage
import pl.yalgrin.playnite.simplesync.client.service.ConnectionService
import pl.yalgrin.playnite.simplesync.client.service.RegisteredClientService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/client")
class ClientResource(
    val registeredClientService: RegisteredClientService,
    val connectionService: ConnectionService
) {

    @PostMapping("/register")
    fun register(@RequestBody info: RegistrationRequestDTO): Mono<RegisteredClientDTO> {
        return registeredClientService.register(info)
    }

    @PostMapping
    fun check(): Mono<Void> {
        return Mono.empty()
    }

    @PostMapping("/change-name")
    fun changeName(@RequestParam newName: String): Mono<*> {
        return registeredClientService.changeName(newName)
    }

    @PostMapping("/connect", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connect(): Flux<ServerSentEvent<ConnectionMessage>> {
        return connectionService.connect()
    }
}