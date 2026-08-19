package pl.yalgrin.playnite.simplesync.migration

import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff
import pl.yalgrin.playnite.simplesync.library.repository.FilterPresetRepository
import pl.yalgrin.playnite.simplesync.library.repository.GameDiffRepository
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.repository.PlatformDiffRepository

abstract class FilterPresetMigrator(
    repository: FilterPresetRepository,
    transactionManager: ReactiveTransactionManager
) : AbstractObjectMigration<FilterPreset>(
    repository, transactionManager
) {
    override fun getHandledType(): MigrationObjectType = MigrationObjectType.FILTER_PRESET
}

abstract class PlatformDiffMigrator(
    repository: PlatformDiffRepository,
    transactionManager: ReactiveTransactionManager
) : AbstractObjectMigration<PlatformDiff>(
    repository, transactionManager
) {
    override fun getHandledType(): MigrationObjectType = MigrationObjectType.PLATFORM_DIFF
}

abstract class GameMigrator(repository: GameRepository, transactionManager: ReactiveTransactionManager) :
    AbstractObjectMigration<Game>(
        repository, transactionManager
    ) {
    override fun getHandledType(): MigrationObjectType = MigrationObjectType.GAME
}

abstract class GameDiffMigrator(repository: GameDiffRepository, transactionManager: ReactiveTransactionManager) :
    AbstractObjectMigration<GameDiff>(
        repository, transactionManager
    ) {
    override fun getHandledType(): MigrationObjectType = MigrationObjectType.GAME_DIFF
}