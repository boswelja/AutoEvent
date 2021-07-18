package com.boswelja.autoevent.eventextractor

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.entityextraction.DateTimeEntity
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class EventExtractor {
    private val modelDownloaded = MutableStateFlow(false)
    private val eventExtractor: EntityExtractor

    init {
        // TODO Load language from settings
        val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
            .build()
        eventExtractor = EntityExtraction.getClient(options)

        // Download model if needed
        val modelDownloadConditions = DownloadConditions.Builder()
            .build()
        eventExtractor.downloadModelIfNeeded(modelDownloadConditions)
            .addOnSuccessListener {
                Log.i("NotiListenerService", "Model downloaded")
                modelDownloaded.tryEmit(true)
            }
            .addOnFailureListener {
                throw it
            }
    }

    suspend fun extractEventsFrom(text: String): List<EventDetails> {
        // Wait for model to be downloaded
        modelDownloaded.first { it }
        val params = EntityExtractionParams.Builder(text)
            .setEntityTypesFilter(
                setOf(
                    Entity.TYPE_DATE_TIME
                )
            )
            .build()

        val annotations = eventExtractor.annotate(params).await()
        return annotations.map { annotation ->
            // We can only have annotations with DateTimeEntities due to our params
            val dateTimeEntity = annotation.entities.first {
                it is DateTimeEntity
            } as DateTimeEntity
            val startDate = Date(dateTimeEntity.timestampMillis)
            val endDate = Date(
                estimateEndTimeMillis(
                    dateTimeEntity.dateTimeGranularity, dateTimeEntity.timestampMillis
                )
            )
            EventDetails(startDate, endDate)
        }
    }

    private fun estimateEndTimeMillis(granularity: Int, startMillis: Long): Long {
        val durationMillis = when (granularity) {
            DateTimeEntity.GRANULARITY_DAY -> TimeUnit.DAYS.toMillis(1)
            DateTimeEntity.GRANULARITY_MONTH -> TimeUnit.DAYS.toMillis(30)
            DateTimeEntity.GRANULARITY_WEEK -> TimeUnit.DAYS.toMillis(7)
            DateTimeEntity.GRANULARITY_YEAR -> TimeUnit.DAYS.toMillis(365)
            else -> TimeUnit.HOURS.toMillis(1)
        }
        return startMillis + durationMillis
    }
}
