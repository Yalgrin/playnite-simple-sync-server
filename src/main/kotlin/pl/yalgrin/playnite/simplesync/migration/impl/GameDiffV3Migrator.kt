package pl.yalgrin.playnite.simplesync.migration.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.util.*
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff
import pl.yalgrin.playnite.simplesync.library.enums.toInstallSizeGroup
import pl.yalgrin.playnite.simplesync.library.enums.toPlaytimeCategory
import pl.yalgrin.playnite.simplesync.library.enums.toScoreGroup
import pl.yalgrin.playnite.simplesync.library.repository.GameDiffRepository
import pl.yalgrin.playnite.simplesync.migration.GameDiffMigrator
import reactor.core.publisher.Mono
import tools.jackson.databind.node.ObjectNode

@Component
class GameDiffV3Migrator(
    repository: GameDiffRepository,
    transactionManager: ReactiveTransactionManager
) : GameDiffMigrator(repository, transactionManager) {

    //TODO test

    override fun getSourceVersion(): Long = 2

    override fun getTargetVersion(): Long = 3

    override fun migrate(entity: GameDiff): Mono<Unit> {
        return entity.diffData.asJsonNode()
            .map { nodes ->
                if (nodes is ObjectNode) {
                    nodes.extractLocalDateTime("releaseDate")?.let { releaseDate ->
                        nodes.put("releaseYear", releaseDate.year)
                    }
                    nodes.put("pluginId", entity.pluginId)
                    nodes.put("userScoreGroup", nodes.extractInt("userScore").toScoreGroup().name)
                    nodes.put("criticScoreGroup", nodes.extractInt("criticScore").toScoreGroup().name)
                    nodes.put("communityScoreGroup", nodes.extractInt("communityScore").toScoreGroup().name)
                    nodes.extractZonedDateTime("lastActivity")
                        ?.let { nodes.put("lastActivityInMs", it.toInstant().toEpochMilli()) }
                    nodes.extractZonedDateTime("added")?.let { nodes.put("addedInMs", it.toInstant().toEpochMilli()) }
                    nodes.extractZonedDateTime("modified")
                        ?.let { nodes.put("modifiedInMs", it.toInstant().toEpochMilli()) }
                    nodes.extractZonedDateTime("lastSizeScanDate")
                        ?.let { nodes.put("lastSizeScanDateInMs", it.toInstant().toEpochMilli()) }
                    nodes.put("playtimeCategory", nodes.extractLong("playtime").toPlaytimeCategory().name)
                    nodes.put("installSizeGroup", nodes.extractLong("installSize").toInstallSizeGroup().name)
                }
                nodes
            }.flatMap { it.toJson() }
            .flatMap {
                entity.diffData = it
                entity.modelVersion = getTargetVersion()
                repository.save(entity)
            }.thenAny()
    }

}