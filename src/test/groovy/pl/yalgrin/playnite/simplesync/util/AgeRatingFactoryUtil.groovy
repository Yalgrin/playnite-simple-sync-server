package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.dto.AgeRatingDTO

class AgeRatingFactoryUtil {
    static AgeRatingDTO createAgeRating(String id, String name, boolean removed = false) {
        return AgeRatingDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static AgeRatingDTO randomAgeRating() {
        return AgeRatingDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static AgeRatingDTO ageRatingWithIndex(int idx) {
        return AgeRatingDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
