package pl.yalgrin.playnite.simplesync.library.service

import io.vavr.control.Try
import jakarta.annotation.PostConstruct
import org.apache.commons.collections4.ListUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.stereotype.Service
import pl.yalgrin.playnite.simplesync.common.config.ALLOWED_FILE_NAMES
import pl.yalgrin.playnite.simplesync.common.config.ALLOWED_FOLDERS
import pl.yalgrin.playnite.simplesync.common.config.GAME
import pl.yalgrin.playnite.simplesync.common.config.PLATFORM
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.repository.PlatformRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.function.Tuple2
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.streams.asSequence

@Service
@DependsOnDatabaseInitialization
class MetadataService(
    private val gameRepository: GameRepository,
    private val platformRepository: PlatformRepository
) {
    @Value($$"${application.metadata-folder}")
    val metadataFolder: String = ""

    companion object {
        private val log = LoggerFactory.getLogger(MetadataService::class.java)
        const val PARTITION_SIZE = 990
    }

    @PostConstruct
    fun init() {
        require(metadataFolder.isNotBlank()) { "No metadata folder set!" }

        val metadataPath = Path.of(metadataFolder)
        if (Files.exists(metadataPath)) {
            Files.list(metadataPath).use { files ->
                files.forEach { path -> checkFolderAndRemoveInvalidFolders(path) }
            }
        }
    }

    private fun checkFolderAndRemoveInvalidFolders(path: Path) {
        when (path.fileName.toString()) {
            in ALLOWED_FOLDERS -> {
                val idsToCheck = getIdsToCheck(path)
                if (idsToCheck.isNotEmpty()) {
                    val existingIds = when (path.fileName.toString()) {
                        GAME -> getExistingIds(idsToCheck, gameRepository::findIdsByIds)
                        PLATFORM -> getExistingIds(idsToCheck, platformRepository::findIdsByIds)
                        else -> emptySet()
                    }
                    removeInvalidSubdirectories(path, existingIds, path.fileName.toString().lowercase())
                }
            }

            else -> {
                log.debug("checkFolderAndRemoveInvalidFolders > deleting main folder due to unsupported type: {}", path)
                deleteDirectory(path)
            }
        }
    }

    private fun getIdsToCheck(path: Path): Set<Long> {
        return Files.list(path).use { files ->
            files.asSequence().mapNotNull { pathToLong(it) }.toSet()
        }
    }

    private fun getExistingIds(idsToCheck: Set<Long>, checkingMethod: (Iterable<Long>) -> Flux<Long>): Set<Long> {
        return Mono.fromSupplier { ListUtils.partition(ArrayList(idsToCheck), PARTITION_SIZE) }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap(checkingMethod)
            .collectList()
            .map { it.toSet() }
            .block() ?: emptySet()
    }

    private fun removeInvalidSubdirectories(path: Path, existingIds: Set<Long>, objectName: String) {
        Files.list(path).use { files ->
            files.forEach { gamePath ->
                val id = pathToLong(gamePath)
                if (id == null || id !in existingIds) {
                    log.debug(
                        "checkFolderAndRemoveInvalidFolders > deleting {} folder due to {} {}: {}",
                        objectName,
                        if (id == null) "invalid" else "missing",
                        objectName,
                        gamePath
                    )
                    deleteDirectory(gamePath)
                }
            }
        }
    }

    private fun deleteDirectory(path: Path) {
        FileUtils.deleteDirectory(path.toFile())
    }

    fun saveMetadata(
        folder: String,
        idPart: String,
        filename: String,
        bytes: ByteArray,
        fieldName: String
    ): Mono<Boolean> {
        return Mono.fromCallable {
            require(metadataFolder.isNotBlank()) { "No metadata folder set!" }
            require(fieldName in ALLOWED_FILE_NAMES) { "Invalid file name!" }

            val dir = Path.of(metadataFolder, folder, idPart)
            Files.createDirectories(dir)
            deleteExistingMetadata(fieldName, dir)
            val path = dir.resolve(filename)
            deleteIfExists(path)
            log.debug("saveMetadata > Writing file: {}", path)
            Files.write(path, bytes)
            true
        }.subscribeOn(Schedulers.boundedElastic())
    }

    private fun deleteExistingMetadata(fieldName: String, dir: Path) {
        Files.list(dir).use { files ->
            files.forEach { path ->
                val baseName = FilenameUtils.getBaseName(path.toString())
                if (baseName == fieldName || FilenameUtils.getExtension(path.toString()).equals("tmp", true)) {
                    log.debug("deleteExistingMetadata > found same existing metadata, will try to remove: {}", path)
                    deleteIfExists(path)
                }
            }
        }
    }

    fun deleteMetadata(folder: String, idPart: String, fieldName: String): Mono<Boolean> {
        return Mono.fromCallable {
            require(metadataFolder.isNotBlank()) { "No metadata folder set!" }
            require(fieldName in ALLOWED_FILE_NAMES) { "Invalid file name!" }

            val dir = Path.of(metadataFolder, folder, idPart)
            Files.exists(dir) && deleteExisting(fieldName, dir)
        }.subscribeOn(Schedulers.boundedElastic())
    }

    private fun deleteExisting(fieldName: String, dir: Path): Boolean {
        val deletedAnything = AtomicBoolean(false)
        Files.list(dir).use { files ->
            files.forEach { path ->
                val baseName = FilenameUtils.getBaseName(path.toString())
                if (baseName == fieldName || FilenameUtils.getExtension(path.toString()).equals("tmp", true)) {
                    if (deleteIfExists(path)) {
                        deletedAnything.set(true)
                    }
                }
            }
        }
        return deletedAnything.get()
    }

    private fun deleteIfExists(path: Path): Boolean {
        log.debug("Deleting file: {}", path)
        return Files.deleteIfExists(path)
    }

    fun deleteExcessiveMetadata(folder: String, idPart: String, filename: String, fieldName: String): Mono<Boolean> {
        return Mono.fromCallable {
            require(metadataFolder.isNotBlank()) { "No metadata folder set!" }
            require(fieldName in ALLOWED_FILE_NAMES) { "Invalid file name!" }

            val dir = Path.of(metadataFolder, folder, idPart)
            if (Files.exists(dir)) {
                Files.list(dir).use { files ->
                    files.forEach { path ->
                        val baseName = FilenameUtils.getBaseName(path.toString())
                        val existingFileName = FilenameUtils.getName(path.toString())
                        if ((baseName == fieldName && existingFileName != filename) || FilenameUtils.getExtension(path.toString())
                                .equals("tmp", true)
                        ) {
                            log.debug(
                                "deleteExcessiveMetadata > found excessive metadata, will try to remove: {}",
                                path
                            )
                            deleteIfExists(path)
                        }
                    }
                }
            }
            true
        }.subscribeOn(Schedulers.boundedElastic())
    }

    fun getMetadata(folder: String, id: String, filename: String): Mono<Tuple2<String, Flux<DataBuffer>>> {
        return Mono.fromCallable { findMetadataPath(folder, id, filename) }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { path ->
                getFileName(path).zipWith(readFile(path))
            }
    }

    private fun findMetadataPath(folder: String, id: String, filename: String): Path? {
        val directory = Path.of(metadataFolder, folder, id)
        return Files.list(directory).use { list ->
            val matchingFiles = list.filter { path ->
                FilenameUtils.getBaseName(path.toString()) == filename
            }.toList()
            if (matchingFiles.size > 1) {
                log.warn(
                    "findMetadataPath > found multiple matching files for directory {} - {}!",
                    directory,
                    matchingFiles
                )
            }
            matchingFiles.firstOrNull()
        }
    }

    private fun getFileName(path: Path): Mono<String> {
        return Mono.just(path.fileName.toString())
    }

    private fun readFile(path: Path): Mono<Flux<DataBuffer>> {
        return Mono.fromCallable { DataBufferUtils.read(path, DefaultDataBufferFactory(), 4096) }
            .subscribeOn(Schedulers.boundedElastic())
    }

    private fun pathToLong(gamePath: Path): Long? {
        return Try.of { gamePath.fileName.toString().toLong() }.getOrNull()
    }


    fun fileDoesNotExist(metadataFolder: String, idPart: String, fieldName: String): Mono<Boolean> {
        return Mono.fromCallable {
            val folderPath = Path.of(this.metadataFolder, metadataFolder, idPart)
            !Files.exists(folderPath) || !Files.isDirectory(folderPath) || Files.list(folderPath).use { stream ->
                stream.noneMatch { path: Path? ->
                    val fileName = path!!.fileName.toString()
                    val extensionIndex = fileName.lastIndexOf(".")
                    val baseName = fileName.substring(0, extensionIndex)
                    baseName == fieldName
                }
            }
        }.subscribeOn(Schedulers.boundedElastic())
    }
}