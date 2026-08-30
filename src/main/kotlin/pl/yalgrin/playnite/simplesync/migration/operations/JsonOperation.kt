package pl.yalgrin.playnite.simplesync.migration.operations

import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode

fun interface JsonOperation {
    fun perform(node: JsonNode?)
}

class JsonRenameOperation(val oldName: String, val newName: String) : JsonOperation {
    override fun perform(node: JsonNode?) {
        if (node != null && node is ObjectNode) {
            val oldNameNode = node.remove(oldName)
            if (oldNameNode != null) {
                node[newName] = oldNameNode
            }
        }
    }
}

class JsonRemoveOperation(val propertyName: String) : JsonOperation {
    override fun perform(node: JsonNode?) {
        if (node != null && node is ObjectNode) {
            node.remove(propertyName)
        }
    }
}