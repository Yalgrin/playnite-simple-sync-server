package pl.yalgrin.playnite.simplesync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JsonConfig {
    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapperProvider.create();
    }
}
