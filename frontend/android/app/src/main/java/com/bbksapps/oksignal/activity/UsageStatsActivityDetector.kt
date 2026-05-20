package com.bbksapps.oksignal.activity

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

class UsageStatsActivityDetector(
    private val context: Context
) {
    fun getLastUserActivityTimeMillis(
        lookbackMillis: Long = 6 * 60 * 60 * 1000L
    ): Long? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - lookbackMillis

        val events = usageStatsManager.queryEvents(startTime, endTime)

        var lastActivityTime: Long? = null
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    lastActivityTime = maxOf(
                        lastActivityTime ?: 0L,
                        event.timeStamp
                    )
                }
            }
        }

        return lastActivityTime
    }
}