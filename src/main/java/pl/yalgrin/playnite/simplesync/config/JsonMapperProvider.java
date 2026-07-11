package pl.yalgrin.playnite.simplesync.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonMapperProvider {
    public static JsonMapper build() {
        JsonMapper.Builder builder = JsonMapper.builder();
        customizer().customize(builder);
        return builder.build();
    }

    public static JsonMapperBuilderCustomizer customizer() {
        return builder -> {
            builder.disable(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.changeDefaultPropertyInclusion(value -> value.withValueInclusion(JsonInclude.Include.NON_NULL));
            builder.changeDefaultPropertyInclusion(
                    value -> value.withContentInclusion(JsonInclude.Include.NON_DEFAULT));
        };
    }
}
