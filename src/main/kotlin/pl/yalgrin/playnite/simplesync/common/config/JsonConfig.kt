package pl.yalgrin.playnite.simplesync.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import tools.jackson.databind.json.JsonMapper

@Configuration
class JsonConfig {
    @Primary
    @Bean
    fun jsonMapper(): JsonMapper {
        return buildJsonMapper()
    }
}