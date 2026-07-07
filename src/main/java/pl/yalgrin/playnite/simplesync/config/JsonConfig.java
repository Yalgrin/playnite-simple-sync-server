package pl.yalgrin.playnite.simplesync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JsonConfig {
    @Primary
    @Bean
    public JsonMapper jsonMapper() {
        JsonMapper.Builder builder = JsonMapper.builder();
        JsonMapperProvider.customizer().customize(builder);
        return builder.build();
    }
}
