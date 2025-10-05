package pl.yalgrin.playnite.simplesync.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectMapperProvider {
    public static ObjectMapper create() {
        var builder = new Jackson2ObjectMapperBuilder();
        builder.failOnUnknownProperties(false);
        builder.serializationInclusion(JsonInclude.Include.NON_DEFAULT);
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        return builder.build();
    }
}
