package com.boswelja.autoevent.eventextractor

import java.util.Date

sealed interface Event {
    val startDateTime: Date
    val address: String?
    val emails: Array<String>
}

data class DateTimeEvent(
    override val startDateTime: Date,
    val endDateTime: Date,
    override val address: String? = null,
    override val emails: Array<String> = emptyArray()
) : Event {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DateTimeEvent

        if (startDateTime != other.startDateTime) return false
        if (endDateTime != other.endDateTime) return false
        if (address != other.address) return false
        if (!emails.contentEquals(other.emails)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startDateTime.hashCode()
        result = 31 * result + endDateTime.hashCode()
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + emails.contentHashCode()
        return result
    }
}

data class AllDayEvent(
    override val startDateTime: Date,
    override val address: String? = null,
    override val emails: Array<String> = emptyArray()
) : Event {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AllDayEvent

        if (startDateTime != other.startDateTime) return false
        if (address != other.address) return false
        if (!emails.contentEquals(other.emails)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startDateTime.hashCode()
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + emails.contentHashCode()
        return result
    }
}
