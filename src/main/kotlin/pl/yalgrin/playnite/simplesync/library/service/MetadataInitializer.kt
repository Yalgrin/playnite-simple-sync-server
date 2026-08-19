package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component


@Component
@Order(2)
class MetadataInitializer(
    private val metadataService: MetadataService
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        metadataService.init()
    }

}