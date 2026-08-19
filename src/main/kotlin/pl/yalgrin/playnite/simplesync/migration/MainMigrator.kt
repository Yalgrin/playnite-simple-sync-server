package pl.yalgrin.playnite.simplesync.migration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class MainMigrator(
    private val migrators: List<ObjectMigrator>
) : ApplicationRunner {
    protected val log: Logger by lazy { LoggerFactory.getLogger(AopUtils.getTargetClass(this)) }

    override fun run(args: ApplicationArguments) {
        log.info("Going to migrate database using {} migrators", migrators.size)
        migrators.sortedWith(Comparator.comparingLong<ObjectMigrator> { it.getSourceVersion() }
            .thenComparing { it.getHandledType() })
            .forEach { it.migrateAll().block() }
        log.info("Database migration finished")
    }
}