package pl.yalgrin.playnite.simplesync.util.panel


import pl.yalgrin.playnite.simplesync.panel.dto.PanelAgeRatingDTO

class PanelAgeRatingFactoryUtil {
    static PanelAgeRatingDTO createAgeRating(Long id, String name) {
        return new PanelAgeRatingDTO(id, null, name)
    }

    static PanelAgeRatingDTO createAgeRating(String name) {
        return new PanelAgeRatingDTO(null, null, name)
    }

    static PanelAgeRatingDTO randomAgeRating() {
        return createAgeRating(UUID.randomUUID().toString())
    }

    static PanelAgeRatingDTO ageRatingWithIndex(int idx) {
        return createAgeRating("name-$idx")
    }
}
