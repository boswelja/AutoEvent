package com.boswelja.autoevent.eventextractor

import java.util.Date

sealed interface Event {
    val startDateTime: Date
    val address: String?
    val emails: Array<String>
}

class DateTimeEvent(
    override val startDateTime: Date,
    val endDateTime: Date,
    override val address: String? = null,
    override val emails: Array<String> = emptyArray()
) : Event

class AllDayEvent(
    override val startDateTime: Date,
    override val address: String? = null,
    override val emails: Array<String> = emptyArray()
) : Event
