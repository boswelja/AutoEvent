package com.boswelja.autoevent.eventextractor

import java.util.Date

data class Event(
    val startDateTime: Date,
    val endDateTime: Date,
    val isAllDay: Boolean,
    val extras: Extras
)

data class Extras(
    val address: String? = null,
    val emails: Set<String> = emptySet()
)
