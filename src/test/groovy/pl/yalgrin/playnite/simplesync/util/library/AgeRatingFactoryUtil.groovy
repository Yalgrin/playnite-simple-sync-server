package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.AgeRatingDTO

class AgeRatingFactoryUtil {
    static AgeRatingDTO createAgeRating(String id, String name, boolean removed = false) {
        return new AgeRatingDTO(id, name, removed)
    }

    static AgeRatingDTO randomAgeRating() {
        return createAgeRating(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static AgeRatingDTO ageRatingWithIndex(int idx) {
        return createAgeRating("id-$idx", "name-$idx")
    }
}
