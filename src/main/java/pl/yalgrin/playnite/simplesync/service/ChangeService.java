package pl.yalgrin.playnite.simplesync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.yalgrin.playnite.simplesync.domain.objects.Game;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.GameChangeRequestDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.GameDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.ChangeMapper;
import pl.yalgrin.playnite.simplesync.repository.ChangeRepository;
import pl.yalgrin.playnite.simplesync.repository.objects.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChangeService {
    private final ChangeRepository repository;
    private final ChangeMapper mapper;
    private final ObjectMapper objectMapper;

    private final CategoryRepository categoryRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final CompanyRepository companyRepository;
    private final FeatureRepository featureRepository;
    private final TagRepository tagRepository;
    private final SeriesRepository seriesRepository;
    private final AgeRatingRepository ageRatingRepository;
    private final RegionRepository regionRepository;
    private final SourceRepository sourceRepository;
    private final CompletionStatusRepository completionStatusRepository;
    private final FilterPresetRepository filterPresetRepository;
    private final GameRepository gameRepository;

    @Transactional(readOnly = true)
    public Flux<ChangeDTO> findFromLastId(Long lastId) {
        return repository.findFromLastId(lastId).map(mapper::toDTO);
    }

    public Flux<ChangeDTO> generateChangesForAllObjects() {
        return repository.findMaxId().switchIfEmpty(Mono.justOrEmpty(0L)).flatMapMany(maxId -> Flux.mergeSequential(
                categoryRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Category).objectId(id).build()),
                genreRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Genre).objectId(id).build()),
                platformRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Platform).objectId(id).build()),
                companyRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Company).objectId(id).build()),
                featureRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Feature).objectId(id).build()),
                tagRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Tag).objectId(id).build()),
                seriesRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Series).objectId(id).build()),
                ageRatingRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.AgeRating).objectId(id).build()),
                regionRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Region).objectId(id).build()),
                sourceRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Source).objectId(id).build()),
                completionStatusRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.CompletionStatus).objectId(id)
                                .build()), filterPresetRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.FilterPreset).objectId(id).build()),
                gameRepository.findAllIds()
                        .map(id -> ChangeDTO.builder().id(maxId).type(ObjectType.Game).objectId(id).build())));
    }

    public Mono<ChangeDTO> saveChange(ChangeDTO changeDTO) {
        return Mono.justOrEmpty(changeDTO).doOnNext(dto -> log.debug("saveChange > START, changeDTO: {}", dto))
                .map(mapper::toEntity).flatMap(repository::save).map(mapper::toDTO)
                .doOnSuccess(dto -> log.debug("saveChange > END, changeDTO: {}", dto));
    }

    public Flux<ChangeDTO> generateChangesForGames(GameChangeRequestDTO dto) {
        return Mono.justOrEmpty(dto)
                .filter(d -> CollectionUtils.isNotEmpty(d.getIds()) || CollectionUtils.isNotEmpty(d.getGameIds()))
                .flatMapMany(d -> {
                    CollectedIds collectedIds = new CollectedIds();
                    return fetchGames(d)
                            .doOnNext(game -> {
                                GameDTO targetDto = Try.of(
                                                () -> objectMapper.readValue(game.getSavedData().asArray(), GameDTO.class))
                                        .getOrElse(GameDTO::new);
                                if (targetDto.getCategories() != null) {
                                    targetDto.getCategories().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getCategoryUuids()::add);
                                }
                                if (targetDto.getGenres() != null) {
                                    targetDto.getGenres().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getGenreUuids()::add);
                                }
                                if (targetDto.getPlatforms() != null) {
                                    targetDto.getPlatforms().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getPlatformUuids()::add);
                                }
                                if (targetDto.getDevelopers() != null) {
                                    targetDto.getDevelopers().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getCompanyUuids()::add);
                                }
                                if (targetDto.getPublishers() != null) {
                                    targetDto.getPublishers().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getCompanyUuids()::add);
                                }
                                if (targetDto.getFeatures() != null) {
                                    targetDto.getFeatures().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getFeatureUuids()::add);
                                }
                                if (targetDto.getTags() != null) {
                                    targetDto.getTags().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getTagUuids()::add);
                                }
                                if (targetDto.getSeries() != null) {
                                    targetDto.getSeries().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getSeriesUuids()::add);
                                }
                                if (targetDto.getAgeRatings() != null) {
                                    targetDto.getAgeRatings().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getAgeRatingUuids()::add);
                                }
                                if (targetDto.getRegions() != null) {
                                    targetDto.getRegions().stream().map(AbstractObjectDTO::getId)
                                            .forEach(collectedIds.getRegionUuids()::add);
                                }
                                if (targetDto.getSource() != null) {
                                    Optional.ofNullable(targetDto.getSource()).map(AbstractObjectDTO::getId)
                                            .ifPresent(id -> collectedIds.getSourceUuids().add(id));
                                }
                                if (targetDto.getCompletionStatus() != null) {
                                    Optional.ofNullable(targetDto.getCompletionStatus()).map(AbstractObjectDTO::getId)
                                            .ifPresent(id -> collectedIds.getCompletionStatusUuids().add(id));
                                }
                                collectedIds.getGameIds().add(game.getId());
                            }).then(Mono.when(findCategories(collectedIds), findGenres(collectedIds),
                                    findPlatforms(collectedIds), findCompanies(collectedIds),
                                    findFeatures(collectedIds),
                                    findTags(collectedIds), findSeries(collectedIds), findAgeRatings(collectedIds),
                                    findRegions(collectedIds), findSources(collectedIds),
                                    findCompletionStatuses(collectedIds)))
                            .thenMany(Flux.mergeSequential(
                                    Flux.fromIterable(collectedIds.getCategoryIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Category)
                                                    .objectId(id).build()),
                                    Flux.fromIterable(collectedIds.getGenreIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Genre).objectId(id)
                                                    .build()),
                                    Flux.fromIterable(collectedIds.getPlatformIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Platform)
                                                    .objectId(id).build()),
                                    Flux.fromIterable(collectedIds.getCompanyIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Company)
                                                    .objectId(id).build()),
                                    Flux.fromIterable(collectedIds.getFeatureIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Feature)
                                                    .objectId(id).build()),
                                    Flux.fromIterable(collectedIds.getTagIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Tag).objectId(id)
                                                    .build()),
                                    Flux.fromIterable(collectedIds.getSeriesIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Series).objectId(id)
                                                    .build()),
                                    Flux.fromIterable(collectedIds.getAgeRatingIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.AgeRating)
                                                    .objectId(id).build()),
                                    Flux.fromIterable(collectedIds.getRegionIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Region).objectId(id)
                                                    .build()),
                                    Flux.fromIterable(collectedIds.getSourceIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Source).objectId(id)
                                                    .build()),
                                    Flux.fromIterable(collectedIds.getCompletionStatusIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.CompletionStatus)
                                                    .objectId(id).build()),
                                    Flux.fromIterable(collectedIds.getGameIds())
                                            .sort()
                                            .map(id -> ChangeDTO.builder().id(null).type(ObjectType.Game).objectId(id)
                                                    .build())
                            ));
                });
    }

    private Flux<Game> fetchGames(GameChangeRequestDTO dto) {
        Set<String> alreadyFetchedIds = Collections.synchronizedSet(new HashSet<>());
        return getByIdPairs(dto, alreadyFetchedIds)
                .flatMapMany(list -> Flux.merge(Flux.fromIterable(list), getByIds(dto, alreadyFetchedIds)));
    }

    private Mono<List<Game>> getByIdPairs(GameChangeRequestDTO dto, Set<String> alreadyFetchedIds) {
        if (dto == null || dto.getGameIds() == null) {
            return Mono.just(new ArrayList<>());
        }
        return Flux.fromIterable(dto.getGameIds())
                .filter(i -> i != null && i.getGameId() != null && i.getPluginId() != null)
                .flatMap(i -> gameRepository.findByGameIdAndPluginId(i.getGameId(), i.getPluginId()))
                .doOnNext(g -> alreadyFetchedIds.add(g.getPlayniteId()))
                .collectList().switchIfEmpty(Mono.just(new ArrayList<>()));
    }

    private Flux<Game> getByIds(GameChangeRequestDTO dto, Set<String> alreadyFetchedIds) {
        if (dto == null || dto.getIds() == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(dto.getIds()).filter(i -> !alreadyFetchedIds.contains(i)).buffer(100)
                .flatMap(gameRepository::findByPlayniteIdIn);
    }

    private Mono<Void> findCategories(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getCategoryUuids()).buffer(100)
                .flatMap(categoryRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getCategoryIds().add(id))).then();
    }

    private Mono<Void> findGenres(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getGenreUuids()).buffer(100)
                .flatMap(genreRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getGenreIds().add(id))).then();
    }

    private Mono<Void> findPlatforms(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getPlatformUuids()).buffer(100)
                .flatMap(platformRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getPlatformIds().add(id))).then();
    }

    private Mono<Void> findCompanies(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getCompanyUuids()).buffer(100)
                .flatMap(companyRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getCompanyIds().add(id))).then();
    }

    private Mono<Void> findFeatures(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getFeatureUuids()).buffer(100)
                .flatMap(featureRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getFeatureIds().add(id))).then();
    }

    private Mono<Void> findTags(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getTagUuids()).buffer(100)
                .flatMap(tagRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getTagIds().add(id))).then();
    }

    private Mono<Void> findSeries(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getSeriesUuids()).buffer(100)
                .flatMap(seriesRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getSeriesIds().add(id))).then();
    }

    private Mono<Void> findAgeRatings(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getAgeRatingUuids()).buffer(100)
                .flatMap(ageRatingRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getAgeRatingIds().add(id))).then();
    }

    private Mono<Void> findRegions(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getRegionUuids()).buffer(100)
                .flatMap(regionRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getRegionIds().add(id))).then();
    }

    private Mono<Void> findSources(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getSourceUuids()).buffer(100)
                .flatMap(sourceRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getSourceIds().add(id))).then();
    }

    private Mono<Void> findCompletionStatuses(CollectedIds collectedIds) {
        return Mono.just(collectedIds).flatMapMany(c -> Flux.fromIterable(c.getCompletionStatusUuids()).buffer(100)
                .flatMap(completionStatusRepository::findIdsByPlayniteIdIn)
                .doOnNext(id -> collectedIds.getCompletionStatusIds().add(id))).then();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CollectedIds {
        private Set<String> categoryUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> categoryIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> genreUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> genreIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> platformUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> platformIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> companyUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> companyIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> featureUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> featureIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> tagUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> tagIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> seriesUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> seriesIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> ageRatingUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> ageRatingIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> regionUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> regionIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> sourceUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> sourceIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> completionStatusUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> completionStatusIds = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> gameIds = Collections.synchronizedSet(new HashSet<>());
    }
}
