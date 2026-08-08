package pl.yalgrin.playnite.simplesync.change.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.dto.GameChangeRequestDTO
import pl.yalgrin.playnite.simplesync.change.mapper.ChangeMapper
import pl.yalgrin.playnite.simplesync.change.mapper.ChangeMessageMapper
import pl.yalgrin.playnite.simplesync.change.repository.ChangeRepository
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.library.repository.*
import pl.yalgrin.playnite.simplesync.security.getSessionClientId
import pl.yalgrin.playnite.simplesync.util.asObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.ConcurrentHashMap

@Service
class ChangeService(
    val repository: ChangeRepository,
    val changeMessageMapper: ChangeMessageMapper,
    val mapper: ChangeMapper,
    categoryRepository: CategoryRepository,
    genreRepository: GenreRepository,
    platformRepository: PlatformRepository,
    companyRepository: CompanyRepository,
    featureRepository: FeatureRepository,
    tagRepository: TagRepository,
    seriesRepository: SeriesRepository,
    ageRatingRepository: AgeRatingRepository,
    regionRepository: RegionRepository,
    sourceRepository: SourceRepository,
    completionStatusRepository: CompletionStatusRepository,
    filterPresetRepository: FilterPresetRepository,
    val gameRepository: GameRepository
) {
    private val relatedObjectRepositories: Map<ObjectType, ObjectRepository<*>>

    init {

        val repositoryMap: MutableMap<ObjectType, ObjectRepository<*>> = LinkedHashMap()
        repositoryMap[ObjectType.CATEGORY] = categoryRepository
        repositoryMap[ObjectType.GENRE] = genreRepository
        repositoryMap[ObjectType.PLATFORM] = platformRepository
        repositoryMap[ObjectType.COMPANY] = companyRepository
        repositoryMap[ObjectType.FEATURE] = featureRepository
        repositoryMap[ObjectType.TAG] = tagRepository
        repositoryMap[ObjectType.SERIES] = seriesRepository
        repositoryMap[ObjectType.AGE_RATING] = ageRatingRepository
        repositoryMap[ObjectType.REGION] = regionRepository
        repositoryMap[ObjectType.SOURCE] = sourceRepository
        repositoryMap[ObjectType.COMPLETION_STATUS] = completionStatusRepository
        repositoryMap[ObjectType.FILTER_PRESET] = filterPresetRepository
        this.relatedObjectRepositories = repositoryMap
    }

    @Transactional(readOnly = true)
    fun findFromLastId(lastId: Long?): Flux<ChangeMessage> {
        return getSessionClientId()
            .flatMapMany { repository.findFromLastId(lastId, it) }
            .map { changeMessageMapper.toMessage(it) }
    }

    @Transactional(readOnly = true)
    fun generateChangesForAllObjects(): Flux<ChangeMessage> {
        return findMaxId()
            .flatMapMany { maxId ->
                Flux.mergeSequential(
                    Flux.fromIterable(relatedObjectRepositories.entries)
                        .flatMapSequential { entry ->
                            entry.value.findAllIds().collectList().flatMapMany {
                                findChangesForObjectType(
                                    it,
                                    maxId,
                                    entry.key
                                )
                            }
                        },
                    gameRepository.findAllIds().collectList().flatMapMany {
                        findChangesForObjectType(it, maxId, ObjectType.GAME)
                    }
                )
            }
    }

    private fun findMaxId(): Mono<Long> {
        return repository.findMaxId().defaultIfEmpty(0L)
    }

    @Transactional(readOnly = true)
    fun generateChangesForGames(dto: GameChangeRequestDTO): Flux<ChangeMessage> {
        return dto.toMono()
            .filter { !it.ids.isNullOrEmpty() || !it.gameIds.isNullOrEmpty() }
            .flatMapMany { d ->
                val collectedIds = CollectedIds()
                fetchGames(d)
                    .flatMap { g ->
                        g.savedData.asObject(GameDTO::class.java)
                            .doOnNext { extractObjectUuids(it, collectedIds) }
                            .thenReturn(g)
                    }
                    .doOnNext { game ->
                        game.id?.let { collectedIds.getIds(ObjectType.GAME).add(it) }
                    }.then(findIdsForUuids(collectedIds))
                    .thenMany(
                        Flux.fromIterable(relatedObjectRepositories.keys)
                            .collectList()
                            .map { it.plus(ObjectType.GAME) }
                            .flatMapMany { Flux.fromIterable(it) }
                            .flatMapSequential { objectType ->
                                Mono.fromCallable { collectedIds.getIds(objectType) }
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .flatMapMany { ids -> findChangesForObjectType(ids, null, objectType) }
                            }
                    )

            }
    }

    private fun fetchGames(dto: GameChangeRequestDTO): Flux<Game> {
        val alreadyFetchedIds = ConcurrentHashMap.newKeySet<String>()
        return getByIdPairs(dto, alreadyFetchedIds)
            .flatMapMany { list ->
                Flux.merge(
                    Flux.fromIterable(list),
                    getByIds(dto, alreadyFetchedIds)
                )
            }
    }

    private fun getByIdPairs(
        dto: GameChangeRequestDTO,
        alreadyFetchedIds: MutableSet<String>
    ): Mono<List<Game>> {
        if (dto.gameIds.isNullOrEmpty()) {
            return Mono.just(emptyList())
        }
        //TODO !!
        return Flux.fromIterable(dto.gameIds)
            .filter { i -> i?.gameId != null && i.pluginId != null }
            .flatMap { i ->
                gameRepository.findByGameIdAndPluginId(
                    i.gameId,
                    i.pluginId
                )
            }
            .doOnNext { g -> alreadyFetchedIds.add(g.playniteId!!) }
            .collectList()
            .defaultIfEmpty(emptyList<Game>())
    }

    private fun getByIds(dto: GameChangeRequestDTO, alreadyFetchedIds: Set<String>): Flux<Game> {
        if (dto.ids == null) {
            return Flux.empty()
        }
        return Flux.fromIterable(dto.ids)
            .filter { i -> !alreadyFetchedIds.contains(i) }
            .buffer(100)
            .flatMap { playniteId -> gameRepository.findByPlayniteIdIn(playniteId) }
    }

    private fun extractObjectUuids(targetDto: GameDTO, collectedIds: CollectedIds) {
        targetDto.categories?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.CATEGORY).add(it) } }
        }
        targetDto.genres?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.GENRE).add(it) } }
        }
        targetDto.platforms?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.PLATFORM).add(it) } }
        }
        targetDto.publishers?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.COMPANY).add(it) } }
        }
        targetDto.developers?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.COMPANY).add(it) } }
        }
        targetDto.features?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.FEATURE).add(it) } }
        }
        targetDto.tags?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.TAG).add(it) } }
        }
        targetDto.series?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.SERIES).add(it) } }
        }
        targetDto.ageRatings?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.AGE_RATING).add(it) } }
        }
        targetDto.regions?.let { l ->
            l.stream().map { obj -> obj.id }
                .forEach { e -> e?.let { collectedIds.getUuids(ObjectType.REGION).add(it) } }
        }
        targetDto.source?.id?.let { collectedIds.getUuids(ObjectType.SOURCE).add(it) }
        targetDto.completionStatus?.id?.let { collectedIds.getUuids(ObjectType.COMPLETION_STATUS).add(it) }
    }

    private fun findIdsForUuids(collectedIds: CollectedIds): Mono<Void> {
        return Flux.fromIterable(relatedObjectRepositories.keys)
            .flatMap { objectType ->
                Mono.fromCallable {
                    collectedIds.getUuids(objectType)
                }.subscribeOn(Schedulers.boundedElastic())
                    .flatMapMany { Flux.fromIterable(it) }
                    .buffer(100)
                    .flatMap { relatedObjectRepositories[objectType]?.findIdsByPlayniteIdIn(it) ?: Flux.empty() }
                    .doOnNext { id -> collectedIds.getIds(objectType).add(id) }
            }
            .then()
    }

    private fun findChangesForObjectType(ids: Collection<Long>, maxId: Long?, type: ObjectType): Flux<ChangeMessage> {
        return ids.toFlux().map { id ->
            ChangeMessage(
                id = maxId,
                type = type,
                objectId = id
            )
        }
    }

    fun saveChange(changeDTO: ChangeDTO): Mono<ChangeDTO> {
        return changeDTO.toMono()
            .map { dto -> mapper.toEntity(dto) }
            .flatMap { entity -> repository.save(entity) }
            .map { entity -> mapper.toDTO(entity) }
    }
}

private data class CollectedIds(
    val objectUuids: MutableMap<ObjectType, MutableSet<String>> = ConcurrentHashMap<ObjectType, MutableSet<String>>(),
    val objectIds: MutableMap<ObjectType, MutableSet<Long>> = ConcurrentHashMap<ObjectType, MutableSet<Long>>()
) {
    fun getUuids(type: ObjectType): MutableSet<String> = objectUuids.getOrPut(type) { ConcurrentHashMap.newKeySet() }

    fun getIds(type: ObjectType): MutableSet<Long> = objectIds.getOrPut(type) { ConcurrentHashMap.newKeySet() }
}