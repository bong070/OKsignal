package com.bbksapps.oksignal.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bbksapps.oksignal.R
import java.time.Duration
import java.time.Instant

enum class TimeAgoType {
    JUST_NOW,
    MINUTES,
    ONE_HOUR,
    HOURS,
    OVER_FOUR_HOURS,
    INVALID
}

data class TimeAgoValue(
    val type: TimeAgoType,
    val value: Int = 0
)

fun calculateTimeAgo(isoTime: String?): TimeAgoValue {
    if (isoTime.isNullOrBlank()) {
        return TimeAgoValue(TimeAgoType.INVALID)
    }

    return try {
        val activityTime = Instant.parse(isoTime)
        val minutes = Duration
            .between(activityTime, Instant.now())
            .toMinutes()
            .coerceAtLeast(0)

        when {
            minutes < 1 -> TimeAgoValue(TimeAgoType.JUST_NOW)
            minutes < 60 -> TimeAgoValue(TimeAgoType.MINUTES, minutes.toInt())
            minutes < 120 -> TimeAgoValue(TimeAgoType.ONE_HOUR)
            minutes >= 240 -> TimeAgoValue(TimeAgoType.OVER_FOUR_HOURS)
            else -> TimeAgoValue(TimeAgoType.HOURS, (minutes / 60).toInt())
        }
    } catch (e: Exception) {
        TimeAgoValue(TimeAgoType.INVALID)
    }
}

@Composable
fun TimeAgoValue.asString(): String? {
    return when (type) {
        TimeAgoType.JUST_NOW -> stringResource(R.string.time_just_now)
        TimeAgoType.MINUTES -> stringResource(R.string.time_minutes_ago, value)
        TimeAgoType.ONE_HOUR -> stringResource(R.string.time_one_hour_ago)
        TimeAgoType.HOURS -> stringResource(R.string.time_hours_ago, value)
        TimeAgoType.OVER_FOUR_HOURS -> stringResource(R.string.time_over_four_hours)
        TimeAgoType.INVALID -> null
    }
}