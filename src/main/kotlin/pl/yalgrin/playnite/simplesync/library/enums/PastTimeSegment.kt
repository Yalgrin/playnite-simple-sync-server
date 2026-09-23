package pl.yalgrin.playnite.simplesync.library.enums

import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


enum class PastTimeSegment(val value: Int) {
    TODAY(0),
    YESTERDAY(1),
    PAST_WEEK(2),
    PAST_MONTH(3),
    PAST_YEAR(4),
    MORE_THAN_YEAR(5),
    NEVER(6)
}

fun Int.toPastTimeSegmentById(): PastTimeSegment? {
    return PastTimeSegment.entries.firstOrNull { it.value == this }
}

fun ZonedDateTime?.toPastTimeSegment(): PastTimeSegment {
    if (this == null) {
        return PastTimeSegment.NEVER
    }
    val adjustedTime = this.withZoneSameInstant(ZoneId.systemDefault())
    val localDay = adjustedTime.toLocalDate()
    val nowDay = LocalDate.now()
    if (localDay == nowDay) {
        return PastTimeSegment.TODAY
    } else if (localDay == nowDay.minusDays(1)) {
        return PastTimeSegment.YESTERDAY
    }
    val dayDiff = Duration.between(adjustedTime, ZonedDateTime.now()).toDays()
    return when {
        dayDiff < 7 -> {
            PastTimeSegment.PAST_WEEK
        }

        dayDiff < 30 -> {
            PastTimeSegment.PAST_MONTH
        }

        dayDiff < 365 -> {
            PastTimeSegment.PAST_YEAR
        }

        else -> {
            PastTimeSegment.MORE_THAN_YEAR
        }
    }
}