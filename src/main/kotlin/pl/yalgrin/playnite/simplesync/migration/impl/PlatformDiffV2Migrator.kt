package pl.yalgrin.playnite.simplesync.migration.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.util.asJsonNode
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.common.util.toJson
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff
import pl.yalgrin.playnite.simplesync.library.repository.PlatformDiffRepository
import pl.yalgrin.playnite.simplesync.migration.PlatformDiffMigrator
import pl.yalgrin.playnite.simplesync.migration.operations.JsonOperation
import pl.yalgrin.playnite.simplesync.migration.operations.JsonRemoveOperation
import pl.yalgrin.playnite.simplesync.migration.operations.JsonRenameOperation
import reactor.core.publisher.Mono

@Component
class PlatformDiffV2Migrator(
    repository: PlatformDiffRepository,
    transactionManager: ReactiveTransactionManager
) : PlatformDiffMigrator(repository, transactionManager) {
    val operations: List<JsonOperation> = listOf(
        JsonRemoveOperation("Id"),
        JsonRemoveOperation("Name"),
        JsonRemoveOperation("Removed"),
        JsonRenameOperation("BaseObjectId", "baseObjectId"),
        JsonRenameOperation("ChangedFields", "changedFields"),
        JsonRenameOperation("SpecificationId", "specificationId")
    )


    override fun getSourceVersion(): Long = 1

    override fun getTargetVersion(): Long = 2

    override fun migrate(entity: PlatformDiff): Mono<Unit> {
        return entity.diffData.asJsonNode()
            .map { nodes ->
                operations.forEach { it.perform(nodes) }
                nodes
            }.flatMap { it.toJson() }
            .flatMap {
                entity.diffData = it
                entity.modelVersion = getTargetVersion()
                repository.save(entity)
            }.thenAny()
    }
}

