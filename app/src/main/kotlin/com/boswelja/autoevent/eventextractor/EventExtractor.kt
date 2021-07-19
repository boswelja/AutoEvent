package com.boswelja.autoevent.eventextractor

import android.content.Context
import androidx.datastore.core.DataStore
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.entityextraction.DateTimeEntity
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class EventExtractor(
    context: Context,
    private val settingsStore: DataStore<ExtractorSettings> = context.extractorSettingsDataStore,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val entityTypesFilter = mutableSetOf(Entity.TYPE_DATE_TIME)

    private val isReady = MutableStateFlow(false)
    private var entityExtractor: EntityExtractor? = null

    init {
        coroutineScope.launch {
            settingsStore.data.collect {
                isReady.emit(false)
                entityExtractor?.close()
                initWithLanguage(it.language)

                if (it.extractEmails) {
                    entityTypesFilter.add(Entity.TYPE_EMAIL)
                } else {
                    entityTypesFilter.remove(Entity.TYPE_EMAIL)
                }
                if (it.extractLocation) {
                    entityTypesFilter.add(Entity.TYPE_ADDRESS)
                } else {
                    entityTypesFilter.remove(Entity.TYPE_ADDRESS)
                }
            }
        }
    }

    fun close() {
        coroutineScope.cancel()
        entityExtractor?.close()
    }

    suspend fun extractEventsFrom(text: String): List<Event> {
        // Wait for model to be downloaded
        isReady.first { it }
        val params = EntityExtractionParams.Builder(text)
            .setEntityTypesFilter(entityTypesFilter)
            .build()

        val annotations = entityExtractor!!.annotate(params).await()
        return annotations.map { annotation ->
            // TODO This could throw an exception if no DateTimeEntity is found
            val dateTimeEntity = annotation.entities.first {
                it is DateTimeEntity
            } as DateTimeEntity
            val startDate = Date(dateTimeEntity.timestampMillis)
            if (dateTimeEntity.isAllDay()) {
                AllDayEvent(startDate)
            } else {
                val endDate = Date(
                    estimateEndTimeMillis(
                        dateTimeEntity.dateTimeGranularity, dateTimeEntity.timestampMillis
                    )
                )
                DateTimeEvent(startDate, endDate)
            }
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

    private fun getLanguageOption(language: ExtractorSettings.ExtractorLanguage): String {
        return when (language) {
            ExtractorSettings.ExtractorLanguage.DETECT -> {
                // Get the language for the current Locale, or default to English
                EntityExtractorOptions.fromLanguageTag(
                    Locale.getDefault().toLanguageTag()
                ) ?: EntityExtractorOptions.ENGLISH
            }
            ExtractorSettings.ExtractorLanguage.ARABIC -> EntityExtractorOptions.ARABIC
            ExtractorSettings.ExtractorLanguage.CHINESE -> EntityExtractorOptions.CHINESE
            ExtractorSettings.ExtractorLanguage.DUTCH -> EntityExtractorOptions.DUTCH
            ExtractorSettings.ExtractorLanguage.ENGLISH -> EntityExtractorOptions.ENGLISH
            ExtractorSettings.ExtractorLanguage.FRENCH -> EntityExtractorOptions.FRENCH
            ExtractorSettings.ExtractorLanguage.GERMAN -> EntityExtractorOptions.GERMAN
            ExtractorSettings.ExtractorLanguage.ITALIAN -> EntityExtractorOptions.ITALIAN
            ExtractorSettings.ExtractorLanguage.JAPANESE -> EntityExtractorOptions.JAPANESE
            ExtractorSettings.ExtractorLanguage.KOREAN -> EntityExtractorOptions.KOREAN
            ExtractorSettings.ExtractorLanguage.POLISH -> EntityExtractorOptions.POLISH
            ExtractorSettings.ExtractorLanguage.PORTUGUESE -> EntityExtractorOptions.PORTUGUESE
            ExtractorSettings.ExtractorLanguage.RUSSIAN -> EntityExtractorOptions.RUSSIAN
            ExtractorSettings.ExtractorLanguage.SPANISH -> EntityExtractorOptions.SPANISH
            ExtractorSettings.ExtractorLanguage.THAI -> EntityExtractorOptions.THAI
            ExtractorSettings.ExtractorLanguage.TURKISH -> EntityExtractorOptions.TURKISH
        }
    }

    private suspend fun initWithLanguage(language: ExtractorSettings.ExtractorLanguage) {
        val options = EntityExtractorOptions.Builder(getLanguageOption(language))
            .build()
        entityExtractor = EntityExtraction.getClient(options)

        // Download model if needed
        val modelDownloadConditions = DownloadConditions.Builder()
            .build()
        entityExtractor!!.downloadModelIfNeeded(modelDownloadConditions).await()
        isReady.emit(true)
    }
}
