package pl.yalgrin.playnite.simplesync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class SpockIntegrationTest extends Specification {
    @Autowired
    protected WebTestClient webTestClient

    def setup() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(60))
                .build()

        StepVerifier.setDefaultTimeout(Duration.ofSeconds(60))
    }
}
