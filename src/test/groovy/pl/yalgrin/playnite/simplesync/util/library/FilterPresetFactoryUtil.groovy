package pl.yalgrin.playnite.simplesync.util.library

import org.apache.commons.compress.utils.Lists
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.FilterPresetSettingsDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.StringItemPropertiesDTO

import java.util.concurrent.ThreadLocalRandom

class FilterPresetFactoryUtil {
    static FilterPresetDTO createFilterPreset(String id, String name, boolean removed = false) {
        return new FilterPresetDTO(
                null,
                id,
                name,
                removed,
                createSettings(true, createProperties()),
                "Added",
                "Descending",
                "Category",
                false, null, null, null, null
        )
    }

    private static FilterPresetSettingsDTO createSettings(boolean installed, IdItemPropertiesDTO category) {
        def obj = new FilterPresetSettingsDTO()
        obj.setInstalled(installed)
        obj.setCategory(category)
        return obj
    }

    private static IdItemPropertiesDTO createProperties() {
        return new IdItemPropertiesDTO(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()), null)
    }

    static FilterPresetDTO randomFilterPreset() {
        def random = ThreadLocalRandom.current()
        return new FilterPresetDTO(
                null,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                false,
                generateRandomSettings(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                random.nextBoolean(), null, null, null, null
        )
    }

    static FilterPresetDTO filterPresetWithIndex(int idx) {
        def random = ThreadLocalRandom.current()
        return new FilterPresetDTO(
                null,
                "id-$idx",
                "name-$idx",
                false,
                generateRandomSettings(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                random.nextBoolean(), null, null, null, null
        )
    }

    private static FilterPresetSettingsDTO generateRandomSettings() {
        def random = ThreadLocalRandom.current()
        return new FilterPresetSettingsDTO(
                random.nextBoolean(),
                random.nextBoolean(),
                random.nextBoolean(),
                random.nextBoolean(),
                random.nextBoolean(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                generateStringProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIdProperties(),
                generateIntProperties(),
                generateIntProperties(),
                generateIntProperties(),
                generateIntProperties(),
                generateIntProperties(),
                generateIntProperties(),
                generateIntProperties(),
                generateIntProperties(),
                generateIntProperties()
        )
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
        return new StringItemPropertiesDTO(values)
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
        return new IdItemPropertiesDTO(values, UUID.randomUUID().toString())
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
        return new IntItemPropertiesDTO(values)
    }
}
