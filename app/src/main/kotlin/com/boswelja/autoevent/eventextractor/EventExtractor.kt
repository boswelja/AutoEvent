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
import java.io.Closeable
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class EventExtractor(
    context: Context,
    private val settingsStore: DataStore<ExtractorSettings> = context.extractorSettingsDataStore,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : Closeable {

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

    override fun close() {
        coroutineScope.cancel()
        entityExtractor?.close()
    }

    suspend fun extractEventFrom(text: String): Event? {
        // Wait for model to be downloaded
        isReady.first { it }
        val params = EntityExtractionParams.Builder(text)
            .setEntityTypesFilter(entityTypesFilter)
            .build()

        val annotations = entityExtractor!!.annotate(params).await()

        var startDate: Date? = null
        var isAllDay = false
        var address: String? = null
        var emails = arrayOf<String>()

        annotations.forEach { annotation ->
            // TODO Handle cases where more than one event is found
            annotation.entities.forEach { entity ->
                when (entity.type) {
                    Entity.TYPE_DATE_TIME -> {
                        entity as DateTimeEntity
                        isAllDay = entity.isAllDay()
                        startDate = Date(entity.timestampMillis)
                    }
                    Entity.TYPE_ADDRESS -> {
                        address = annotation.annotatedText
                    }
                    Entity.TYPE_EMAIL -> {
                        emails += annotation.annotatedText
                    }
                }
            }
        }

        return startDate?.let { startDateTime ->
            if (isAllDay) {
                AllDayEvent(
                    startDateTime = startDateTime,
                    address = address,
                    emails = emails
                )
            } else {
                DateTimeEvent(
                    startDateTime = startDateTime,
                    endDateTime = Date(estimateEndTimeMillis(startDateTime.time)),
                    address = address,
                    emails = emails
                )
            }
        }
    }

    private fun estimateEndTimeMillis(startMillis: Long): Long {
        val durationMillis = TimeUnit.HOURS.toMillis(1)
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
