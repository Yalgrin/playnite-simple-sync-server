package pl.yalgrin.playnite.simplesync.migration.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.util.asJsonNode
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.common.util.toJson
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.library.repository.FilterPresetRepository
import pl.yalgrin.playnite.simplesync.migration.FilterPresetMigrator
import pl.yalgrin.playnite.simplesync.migration.operations.JsonOperation
import pl.yalgrin.playnite.simplesync.migration.operations.JsonRemoveOperation
import pl.yalgrin.playnite.simplesync.migration.operations.JsonRenameOperation
import reactor.core.publisher.Mono

@Component
class FilterPresetV2Migrator(
    repository: FilterPresetRepository,
    transactionManager: ReactiveTransactionManager
) : FilterPresetMigrator(repository, transactionManager) {
    val operations: List<JsonOperation> = listOf(
        JsonRemoveOperation("Id"),
        JsonRemoveOperation("Name"),
        JsonRemoveOperation("Removed"),
        JsonRenameOperation("Settings", "settings"),
        JsonRenameOperation("SortingOrder", "sortingOrder"),
        JsonRenameOperation("SortingOrderDirection", "sortingOrderDirection"),
        JsonRenameOperation("GroupingOrder", "groupingOrder"),
        JsonRenameOperation("ShowInFullscreenQuickSelection", "showInFullscreenQuickSelection")
    )

    val settingsOperations: List<JsonOperation> = listOf(
        JsonRenameOperation("UseAndFilteringStyle", "useAndFilteringStyle"),
        JsonRenameOperation("IsInstalled", "isInstalled"),
        JsonRenameOperation("IsUnInstalled", "isUninstalled"),
        JsonRenameOperation("Hidden", "isHidden"),
        JsonRenameOperation("Favorite", "isFavorite"),
        JsonRenameOperation("Name", "name"),
        JsonRenameOperation("Version", "version"),
        JsonRenameOperation("ReleaseYear", "releaseYear"),
        JsonRenameOperation("Genre", "genre"),
        JsonRenameOperation("Platform", "platform"),
        JsonRenameOperation("Publisher", "publisher"),
        JsonRenameOperation("Developer", "developer"),
        JsonRenameOperation("Category", "category"),
        JsonRenameOperation("Tag", "tag"),
        JsonRenameOperation("Series", "series"),
        JsonRenameOperation("Region", "region"),
        JsonRenameOperation("Source", "source"),
        JsonRenameOperation("AgeRating", "ageRating"),
        JsonRenameOperation("Library", "library"),
        JsonRenameOperation("CompletionStatuses", "completionStatuses"),
        JsonRenameOperation("Feature", "feature"),
        JsonRenameOperation("UserScore", "userScore"),
        JsonRenameOperation("CriticScore", "criticScore"),
        JsonRenameOperation("CommunityScore", "communityScore"),
        JsonRenameOperation("LastActivity", "lastActivity"),
        JsonRenameOperation("RecentActivity", "recentActivity"),
        JsonRenameOperation("Added", "added"),
        JsonRenameOperation("Modified", "modified"),
        JsonRenameOperation("PlayTime", "playTime"),
        JsonRenameOperation("InstallSize", "installSize")
    )

    val propertiesOperations: List<JsonOperation> = listOf(
        JsonRenameOperation("Ids", "ids"),
        JsonRenameOperation("Text", "text"),
        JsonRenameOperation("Values", "values")
    )

    override fun getSourceVersion(): Long = 1

    override fun getTargetVersion(): Long = 2

    override fun migrate(entity: FilterPreset): Mono<Unit> {
        return entity.savedData.asJsonNode()
            .map { nodes ->
                operations.forEach { it.perform(nodes) }
                nodes["settings"].let { settingsNode ->
                    settingsOperations.forEach { it.perform(settingsNode) }
                    settingsNode["releaseYear"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["genre"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["platform"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["publisher"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["developer"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["category"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["tag"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["series"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["region"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["source"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["ageRating"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["library"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["completionStatuses"].let { propNode ->
                        propertiesOperations.forEach {
                            it.perform(
                                propNode
                            )
                        }
                    }
                    settingsNode["feature"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["userScore"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["criticScore"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["communityScore"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["lastActivity"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["recentActivity"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["added"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["modified"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["playTime"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                    settingsNode["installSize"].let { propNode -> propertiesOperations.forEach { it.perform(propNode) } }
                }
                nodes
            }.flatMap { it.toJson() }
            .flatMap {
                entity.savedData = it
                entity.modelVersion = getTargetVersion()
                repository.save(entity)
            }.thenAny()
    }
}

