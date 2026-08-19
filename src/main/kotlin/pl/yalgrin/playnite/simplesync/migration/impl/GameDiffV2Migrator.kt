package pl.yalgrin.playnite.simplesync.migration.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.util.asJsonNode
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.common.util.toJson
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff
import pl.yalgrin.playnite.simplesync.library.repository.GameDiffRepository
import pl.yalgrin.playnite.simplesync.migration.GameDiffMigrator
import pl.yalgrin.playnite.simplesync.migration.operations.JsonOperation
import pl.yalgrin.playnite.simplesync.migration.operations.JsonRemoveOperation
import pl.yalgrin.playnite.simplesync.migration.operations.JsonRenameOperation
import reactor.core.publisher.Mono
import tools.jackson.databind.node.ArrayNode

@Component
class GameDiffV2Migrator(
    repository: GameDiffRepository,
    transactionManager: ReactiveTransactionManager
) : GameDiffMigrator(repository, transactionManager) {
    val operations: List<JsonOperation> = listOf(
        JsonRemoveOperation("Id"),
        JsonRemoveOperation("Name"),
        JsonRemoveOperation("GameDiffId"),
        JsonRemoveOperation("PluginId"),
        JsonRemoveOperation("Removed"),
        JsonRemoveOperation("IncludeLibraryPluginAction"),
        JsonRemoveOperation("HasIcon"),
        JsonRemoveOperation("HasCoverImage"),
        JsonRemoveOperation("HasBackgroundImage"),
        JsonRenameOperation("BaseObjectId", "baseObjectId"),
        JsonRenameOperation("ChangedFields", "changedFields"),
        JsonRenameOperation("Description", "description"),
        JsonRenameOperation("Notes", "notes"),
        JsonRenameOperation("Genres", "genres"),
        JsonRenameOperation("Hidden", "isHidden"),
        JsonRenameOperation("Favorite", "isFavorite"),
        JsonRenameOperation("LastActivity", "lastActivity"),
        JsonRenameOperation("SortingName", "sortingName"),
        JsonRenameOperation("Platforms", "platforms"),
        JsonRenameOperation("Publishers", "publishers"),
        JsonRenameOperation("Developers", "developers"),
        JsonRenameOperation("ReleaseDate", "releaseDate"),
        JsonRenameOperation("Categories", "categories"),
        JsonRenameOperation("Tags", "tags"),
        JsonRenameOperation("Features", "features"),
        JsonRenameOperation("Links", "links"),
        JsonRenameOperation("Playtime", "playtime"),
        JsonRenameOperation("Added", "added"),
        JsonRenameOperation("Modified", "modified"),
        JsonRenameOperation("PlayCount", "playCount"),
        JsonRenameOperation("InstallSize", "installSize"),
        JsonRenameOperation("LastSizeScanDate", "lastSizeScanDate"),
        JsonRenameOperation("Series", "series"),
        JsonRenameOperation("Version", "version"),
        JsonRenameOperation("AgeRatings", "ageRatings"),
        JsonRenameOperation("Regions", "regions"),
        JsonRenameOperation("Source", "source"),
        JsonRenameOperation("CompletionStatus", "completionStatus"),
        JsonRenameOperation("UserScore", "userScore"),
        JsonRenameOperation("CriticScore", "criticScore"),
        JsonRenameOperation("CommunityScore", "communityScore"),
        JsonRenameOperation("Manual", "manual")
    )

    val listFields = listOf(
        "genres",
        "platforms",
        "publishers",
        "developers",
        "categories",
        "tags",
        "features",
        "links",
        "series",
        "ageRatings",
        "regions"
    )

    val dtoFields = listOf(
        "source",
        "completionStatus"
    )

    val innerObjectOperations: List<JsonOperation> = listOf(
        JsonRenameOperation("Id", "id"),
        JsonRenameOperation("Name", "name"),
        JsonRenameOperation("Removed", "isRemoved"),
        JsonRenameOperation("SpecificationId", "specificationId"),
        JsonRenameOperation("HasIcon", "hasIcon"),
        JsonRenameOperation("HasCoverImage", "hasCoverImage"),
        JsonRenameOperation("HasBackgroundImage", "hasBackgroundImage"),
        JsonRenameOperation("Name", "name"),
        JsonRenameOperation("Url", "url"),
    )

    override fun getSourceVersion(): Long = 1

    override fun getTargetVersion(): Long = 2

    override fun migrate(entity: GameDiff): Mono<Unit> {
        return entity.diffData.asJsonNode()
            .map { nodes ->
                operations.forEach { it.perform(nodes) }
                listFields.forEach { listElement ->
                    nodes[listElement].let { listNode ->
                        if (listNode is ArrayNode) {
                            listNode.forEach { listElementNode ->
                                innerObjectOperations.forEach { it.perform(listElementNode) }
                            }
                        }
                    }
                }
                dtoFields.forEach { dtoElement ->
                    nodes[dtoElement].let { innerNode ->
                        innerObjectOperations.forEach { it.perform(innerNode) }
                    }
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

