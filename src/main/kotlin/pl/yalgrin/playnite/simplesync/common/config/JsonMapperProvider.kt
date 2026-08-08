package pl.yalgrin.playnite.simplesync.common.config

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper

fun buildJsonMapper(): JsonMapper {
    return JsonMapper.builder().customize().build()
}

fun JsonMapper.Builder.customize(): JsonMapper.Builder {
    JsonMapperBuilderCustomizer { builder ->
        builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        builder.changeDefaultPropertyInclusion { value ->
            value.withValueInclusion(
                JsonInclude.Include.NON_NULL
            )
        }
        builder.changeDefaultPropertyInclusion { value -> value.withContentInclusion(JsonInclude.Include.NON_DEFAULT) }
    }.customize(this)

    return this
}