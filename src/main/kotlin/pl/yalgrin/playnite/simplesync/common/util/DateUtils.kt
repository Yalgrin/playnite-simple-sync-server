package pl.yalgrin.playnite.simplesync.common.util

import java.time.ZoneId
import java.time.ZonedDateTime

fun ZonedDateTime?.max(other: ZonedDateTime?): ZonedDateTime? {
    if (this == null) {
        return other
    }
    if (other == null) {
        return this
    }
    return maxOf(this, other)
}

private val UTC_ZONE = ZoneId.of("UTC")

fun currentZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.now().normalize()
}

fun ZonedDateTime.normalize(): ZonedDateTime {
    return this.withNano((this.nano / 1_000_000) * 1_000_000).withZoneSameInstant(UTC_ZONE)
}