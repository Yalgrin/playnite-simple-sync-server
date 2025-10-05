package pl.yalgrin.playnite.simplesync.util.objects

import org.apache.commons.compress.utils.Lists
import pl.yalgrin.playnite.simplesync.dto.filter.FilterPresetSettingsDTO
import pl.yalgrin.playnite.simplesync.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.dto.filter.StringItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.dto.objects.FilterPresetDTO

import java.util.concurrent.ThreadLocalRandom

class FilterPresetFactoryUtil {
    static FilterPresetDTO createFilterPreset(String id, String name, boolean removed = false) {
        return FilterPresetDTO.builder()
                .id(id)
                .name(name)
                .settings(FilterPresetSettingsDTO.builder()
                        .installed(true)
                        .category(IdItemPropertiesDTO.builder()
                                .ids(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                                .build())
                        .build())
                .sortingOrder("Added")
                .sortingOrderDirection("Descending")
                .groupingOrder("Category")
                .removed(removed)
                .build()
    }

    static FilterPresetDTO randomFilterPreset() {
        def random = ThreadLocalRandom.current()
        FilterPresetDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .settings(generateRandomSettings())
                .sortingOrder(UUID.randomUUID().toString())
                .sortingOrderDirection(UUID.randomUUID().toString())
                .groupingOrder(UUID.randomUUID().toString())
                .showInFullscreenQuickSelection(random.nextBoolean())
                .build()
    }

    static FilterPresetDTO filterPresetWithIndex(int idx) {
        randomFilterPreset().toBuilder()
                .id("id-" + idx)
                .name("name " + idx)
                .build()
    }

    private static FilterPresetSettingsDTO generateRandomSettings() {
        def random = ThreadLocalRandom.current()
        FilterPresetSettingsDTO.builder()
                .useAndFilteringStyle(random.nextBoolean())
                .installed(random.nextBoolean())
                .uninstalled(random.nextBoolean())
                .hidden(random.nextBoolean())
                .favorite(random.nextBoolean())
                .name(UUID.randomUUID().toString())
                .version(UUID.randomUUID().toString())
                .releaseYear(generateStringProperties())
                .genre(generateIdProperties())
                .platform(generateIdProperties())
                .publisher(generateIdProperties())
                .developer(generateIdProperties())
                .category(generateIdProperties())
                .tag(generateIdProperties())
                .series(generateIdProperties())
                .region(generateIdProperties())
                .source(generateIdProperties())
                .ageRating(generateIdProperties())
                .library(generateIdProperties())
                .completionStatuses(generateIdProperties())
                .feature(generateIdProperties())
                .userScore(generateIntProperties())
                .criticScore(generateIntProperties())
                .communityScore(generateIntProperties())
                .lastActivity(generateIntProperties())
                .recentActivity(generateIntProperties())
                .added(generateIntProperties())
                .modified(generateIntProperties())
                .playTime(generateIntProperties())
                .installSize(generateIntProperties())
                .build()
    }

    private static StringItemPropertiesDTO generateStringProperties() {
        def random = ThreadLocalRandom.current()
        if (random.nextBoolean()) {
            return null
        }
        List<String> values = Lists.newArrayList()
        for (i in 0..<random.nextInt(5)) {
            values.add(UUID.randomUUID().toString())
        }
        StringItemPropertiesDTO.builder()
                .values(values)
                .build()
    }

    private static IdItemPropertiesDTO generateIdProperties() {
        def random = ThreadLocalRandom.current()
        if (random.nextBoolean()) {
            return null
        }
        List<String> values = Lists.newArrayList()
        for (i in 0..<random.nextInt(5)) {
            values.add(UUID.randomUUID().toString())
        }
        IdItemPropertiesDTO.builder()
                .ids(values)
                .text(UUID.randomUUID().toString())
                .build()
    }

    private static IntItemPropertiesDTO generateIntProperties() {
        def random = ThreadLocalRandom.current()
        if (random.nextBoolean()) {
            return null
        }
        List<Integer> values = Lists.newArrayList()
        for (i in 0..<random.nextInt(5)) {
            values.add(random.nextInt())
        }
        IntItemPropertiesDTO.builder()
                .values(values)
                .build()
    }
}
