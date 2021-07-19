package com.boswelja.autoevent.eventextractor

import com.google.mlkit.nl.entityextraction.DateTimeEntity

fun DateTimeEntity.isAllDay(): Boolean {
    return when (dateTimeGranularity) {
        DateTimeEntity.GRANULARITY_HOUR,
        DateTimeEntity.GRANULARITY_MINUTE,
        DateTimeEntity.GRANULARITY_SECOND -> false
        else -> true
    }
}
