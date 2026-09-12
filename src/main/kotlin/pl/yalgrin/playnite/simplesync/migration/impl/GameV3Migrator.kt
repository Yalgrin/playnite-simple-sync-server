package pl.yalgrin.playnite.simplesync.migration.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.util.asJsonNode
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.common.util.toJson
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.migration.GameMigrator
import reactor.core.publisher.Mono
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class GameV3Migrator(
    repository: GameRepository,
    transactionManager: ReactiveTransactionManager
) : GameMigrator(repository, transactionManager) {

    override fun getSourceVersion(): Long = 2

    override fun getTargetVersion(): Long = 3

    override fun migrate(entity: Game): Mono<Unit> {
        return entity.savedData.asJsonNode()
            .map { nodes ->
                if (nodes is ObjectNode) {
                    extractReleaseDate(nodes)?.let { releaseDate ->
                        nodes.put("releaseYear", releaseDate.year)
                    }
                }
                nodes
            }.flatMap { it.toJson() }
            .flatMap {
                entity.savedData = it
                entity.modelVersion = getTargetVersion()
                repository.save(entity)
            }.thenAny()
    }

    private fun extractReleaseDate(nodes: JsonNode): LocalDateTime? {
        val releaseDateNode = nodes.get("releaseDate")
        return if (releaseDateNode != null && !releaseDateNode.isNull) {
            try {
                LocalDateTime.parse(releaseDateNode.asString(), DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}

