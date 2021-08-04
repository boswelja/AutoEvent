package com.boswelja.autoevent.eventextractor

import com.google.mlkit.nl.entityextraction.DateTimeEntity
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class AnnotationEntityExtensionsKtTest {

    @Test
    fun `isAllDay returns true when an event doesn't specify a time`() {
        val granularities = setOf(
            DateTimeEntity.GRANULARITY_DAY,
            DateTimeEntity.GRANULARITY_WEEK,
            DateTimeEntity.GRANULARITY_MONTH,
            DateTimeEntity.GRANULARITY_YEAR
        )

        granularities.forEach {
            val data = createEntity(it)
            expectThat(data.isAllDay()).isTrue()
        }
    }

    @Test
    fun `isAllDay returns false when an event specifies a time`() {
        val granularities = setOf(
            DateTimeEntity.GRANULARITY_HOUR,
            DateTimeEntity.GRANULARITY_MINUTE,
            DateTimeEntity.GRANULARITY_SECOND
        )

        granularities.forEach {
            val data = createEntity(it)
            expectThat(data.isAllDay()).isFalse()
        }
    }

    private fun createEntity(
        @DateTimeEntity.DateTimeGranularity granularity: Int
    ): DateTimeEntity {
        return DateTimeEntity(0, granularity)
    }
}
