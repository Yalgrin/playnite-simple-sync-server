package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.yalgrin.playnite.simplesync.client.message.ConnectionMessage
import pl.yalgrin.playnite.simplesync.client.service.ConnectionService
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/panel/change")
class PanelChangeResource(
    val connectionService: ConnectionService
) {

    @GetMapping("/connect", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connect(): Flux<ServerSentEvent<ConnectionMessage>> {
        return connectionService.connect(true)
    }
}