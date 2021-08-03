package com.boswelja.autoevent.eventextractor

import android.content.Context
import androidx.datastore.core.DataStore
import com.boswelja.autoevent.common.secondOrNull
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.entityextraction.DateTimeEntity
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import java.io.Closeable
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EventExtractor(
    context: Context,
    private val settingsStore: DataStore<ExtractorSettings> = context.extractorSettingsDataStore,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : Closeable {

    private val coreTypeFilters = setOf(Entity.TYPE_DATE_TIME)
    internal val extraTypeFilters = mutableSetOf<Int>()

    internal val isReady = MutableStateFlow(false)
    internal var entityExtractor: EntityExtractor? = null
    internal var ignoreAllDayEvents: Boolean = false
    internal var defaultDuration: Long = TimeUnit.MINUTES.toMillis(30)

    init {
        coroutineScope.launch {
            settingsStore.data.collect {
                isReady.emit(false)
                entityExtractor?.close()
                initWithLanguage(it.language)

                if (it.extractEmails) {
                    extraTypeFilters.add(Entity.TYPE_EMAIL)
                } else {
                    extraTypeFilters.remove(Entity.TYPE_EMAIL)
                }
                if (it.extractLocation) {
                    extraTypeFilters.add(Entity.TYPE_ADDRESS)
                } else {
                    extraTypeFilters.remove(Entity.TYPE_ADDRESS)
                }
                ignoreAllDayEvents = it.ignoreAllDayEvents
                defaultDuration = it.defaultEventDuration
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
            .setEntityTypesFilter(coreTypeFilters)
            .build()

        val annotations = entityExtractor!!.annotate(params).await()

        val eventDates = annotations
            .sortedBy { it.start }
            .flatMap { annotation ->
                annotation.entities.filterIsInstance<DateTimeEntity>()
            }
            .filter { !ignoreAllDayEvents || !it.isAllDay() }
            .takeLast(2)

        // Return now if no dates were found
        if (eventDates.isEmpty()) return null

        // Calculate start date
        val dates = eventDates.map { Date(it.timestampMillis) }.sorted()
        val startDate = dates.first()

        // If start date is before the current date, return null
        if (startDate < Date()) return null

        // Calculate end date and whether the event is considered to be all-day
        val endDate = dates.secondOrNull() ?: Date(startDate.time + defaultDuration)
        val isAllDay = eventDates.all { it.isAllDay() }

        return Event(
            startDateTime = startDate,
            endDateTime = endDate,
            isAllDay = isAllDay,
            extras = extractExtrasFrom(text)
        )
    }

    internal suspend fun extractExtrasFrom(text: String): Extras {
        return if (extraTypeFilters.isNotEmpty()) {
            val params = EntityExtractionParams.Builder(text)
                .setEntityTypesFilter(extraTypeFilters)
                .build()
            val annotations = entityExtractor!!.annotate(params).await()
            val emails = mutableSetOf<String>()
            var address: String? = null
            annotations.forEach { annotation ->
                annotation.entities.forEach {
                    when (it.type) {
                        Entity.TYPE_EMAIL -> {
                            emails.add(annotation.annotatedText)
                        }
                        Entity.TYPE_ADDRESS -> {
                            address = annotation.annotatedText
                        }
                    }
                }
            }
            Extras(address, emails)
        } else {
            Extras()
        }
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

    internal suspend fun initWithLanguage(language: ExtractorSettings.ExtractorLanguage) {
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
