package pl.yalgrin.playnite.simplesync.util

import org.apache.commons.lang3.StringUtils
import pl.yalgrin.playnite.simplesync.client.enums.MessageType
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.message.ConnectionMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import tools.jackson.databind.json.JsonMapper

class JsonMapperUtil {
    static ConnectionMessage readConnectionMessage(JsonMapper jsonMapper, String str) {
        def type = jsonMapper.readTree(str).get("messageType")
        if (type == null) {
            throw new IllegalArgumentException("Missing message type!")
        }
        def typeStr = type.stringValue()
        if (StringUtils.isBlank(typeStr)) {
            throw new IllegalArgumentException("Missing message type!")
        }
        if (typeStr == MessageType.INITIALIZATION.name()) {
            return readValue(jsonMapper, str, InitializationMessage.class)
        } else if (typeStr == MessageType.CHANGE.name()) {
            return readValue(jsonMapper, str, ChangeMessage.class)
        } else {
            throw new IllegalArgumentException("Invalid type: " + typeStr)
        }
    }

    static <T> T readValue(JsonMapper jsonMapper, String str, Class<T> clazz) {
        return jsonMapper.readValue(str, clazz)
    }
}
