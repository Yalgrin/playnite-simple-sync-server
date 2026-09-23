package pl.yalgrin.playnite.simplesync.library.enums


enum class PlaytimeCategory(val value: Int) {
    NOT_PLAYED(0),
    LESS_THAN_HOUR(1),
    O1_10(2),
    O10_100(3),
    O100_500(4),
    O500_1000(5),
    O1000_PLUS(6)
}

fun Int.toPlaytimeCategoryById(): PlaytimeCategory? {
    return PlaytimeCategory.entries.firstOrNull { it.value == this }
}

fun Long?.toPlaytimeCategory(): PlaytimeCategory {
    if (this == null || this == 0L) {
        return PlaytimeCategory.NOT_PLAYED
    }

    val hours = this / 3600
    return when {
        hours < 1 -> {
            PlaytimeCategory.LESS_THAN_HOUR
        }

        hours in 1..10 -> {
            PlaytimeCategory.O1_10
        }

        hours in 10..100 -> {
            PlaytimeCategory.O10_100
        }

        hours in 100..500 -> {
            PlaytimeCategory.O100_500
        }

        hours in 500..1000 -> {
            PlaytimeCategory.O500_1000
        }

        else -> {
            PlaytimeCategory.O1000_PLUS
        }
    }
}