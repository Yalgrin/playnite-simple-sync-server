package pl.yalgrin.playnite.simplesync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlayniteSimpleSyncServerApplication {
    fun main(args: Array<String>) {
        runApplication<PlayniteSimpleSyncServerApplication>(*args)
    }
}