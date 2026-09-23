package pl.yalgrin.playnite.simplesync.library.enums

enum class ScoreGroup(val value: Int) {
    O0X(0),
    O1X(1),
    O2X(2),
    O3X(3),
    O4X(4),
    O5X(5),
    O6X(6),
    O7X(7),
    O8X(8),
    O9X(9),
    NONE(10)
}

fun Int.toScoreGroupById(): ScoreGroup? {
    return ScoreGroup.entries.firstOrNull { it.value == this }
}

fun Int?.toScoreGroup(): ScoreGroup {
    if (this == null) {
        return ScoreGroup.NONE
    }
    return when {
        this in 0..<10 -> {
            ScoreGroup.O0X
        }

        this in 10..<20 -> {
            ScoreGroup.O1X
        }

        this in 20..<30 -> {
            ScoreGroup.O2X
        }

        this in 30..<40 -> {
            ScoreGroup.O3X
        }

        this in 40..<50 -> {
            ScoreGroup.O4X
        }

        this in 50..<60 -> {
            ScoreGroup.O5X
        }

        this in 60..<70 -> {
            ScoreGroup.O6X
        }

        this in 70..<80 -> {
            ScoreGroup.O7X
        }

        this in 80..<90 -> {
            ScoreGroup.O8X
        }

        this >= 90 -> {
            ScoreGroup.O9X
        }

        else -> {
            ScoreGroup.NONE
        }
    }
}