package com.boswelja.autoevent.eventextractor

import com.boswelja.autoevent.InMemoryDataStore
import com.google.android.gms.internal.mlkit_entity_extraction.zzafl
import com.google.mlkit.nl.entityextraction.DateTimeEntity
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import io.mockk.every
import io.mockk.mockkStatic
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.count
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

class EventExtractorTest {

    private val settingsStore = InMemoryDataStore(ExtractorSettingsSerializer.defaultValue)
    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)

    private lateinit var entityExtractor: DummyEntityExtractor
    private lateinit var eventExtractor: EventExtractor

    @Before
    fun setUp(): Unit = runBlocking {
        // Reset settings store
        settingsStore.updateData { ExtractorSettingsSerializer.defaultValue }

        // Mock entity extractor
        entityExtractor = DummyEntityExtractor()
        mockkStatic(EntityExtraction::getClient)
        every { EntityExtraction.getClient(any()) } returns entityExtractor

        // Init event extractor
        eventExtractor = EventExtractor(
            settingsStore,
            coroutineScope
        )
        eventExtractor.extractEventFrom("") // Effectively block until ready
        entityExtractor.reset()
    }

    @After
    fun tearDown() {
        eventExtractor.close()
    }

    @Test
    fun `close shuts down EntityExtractor`() {
        eventExtractor.close()
        expectThat(entityExtractor.isClosed).isTrue()
    }

    @Test
    fun `settings are not loaded after closing`(): Unit = runBlocking {
        // Set everything to defaults
        eventExtractor.extraTypeFilters.clear()
        eventExtractor.defaultDuration = 0
        eventExtractor.ignoreAllDayEvents = false

        // Close extractor
        eventExtractor.close()

        // Update settings
        settingsStore.updateData {
            ExtractorSettings(
                extractLocation = true,
                extractEmails = true,
                ignoreAllDayEvents = true,
                defaultEventDuration = TimeUnit.MINUTES.toMillis(30)
            )
        }

        // Wait for event extractor to be ready again
        withTimeout(2000) { eventExtractor.isReady.first { it } }

        // Check settings
        expect {
            that(eventExtractor.extraTypeFilters).isEmpty()
            that(eventExtractor.defaultDuration).isEqualTo(0)
            that(eventExtractor.ignoreAllDayEvents).isFalse()
        }
    }

    @Test
    fun `initWithLanguage ensured model is downloaded`(): Unit = runBlocking {
        eventExtractor.initWithLanguage(ExtractorSettings.ExtractorLanguage.ENGLISH)
        expectThat(entityExtractor.isModelDownloaded.await()).isTrue()
    }

    @Test
    fun `extractExtrasFrom does nothing if all extras are disabled`(): Unit = runBlocking {
        // Ensure there are no extras set
        eventExtractor.extraTypeFilters.clear()

        // Make the call and check the result
        eventExtractor.extractExtrasFrom("")
        expectThat(entityExtractor.lastAnnotateParams).isNull()
    }

    @Test
    fun `extractExtrasFrom passes the correct types to EntityExtractor`(): Unit = runBlocking {
        // Check with single type
        eventExtractor.extraTypeFilters.clear()
        eventExtractor.extraTypeFilters.add(Entity.TYPE_EMAIL)

        // Make the call and check the result
        eventExtractor.extractExtrasFrom("")
        expectThat(entityExtractor.lastAnnotateParams?.zzd())
            .isNotNull()
            .containsExactly(eventExtractor.extraTypeFilters)

        // Check with multiple types
        eventExtractor.extraTypeFilters.clear()
        eventExtractor.extraTypeFilters.add(Entity.TYPE_EMAIL)
        eventExtractor.extraTypeFilters.add(Entity.TYPE_ADDRESS)

        // Make the call and check the result
        eventExtractor.extractExtrasFrom("")
        expectThat(entityExtractor.lastAnnotateParams?.zzd())
            .isNotNull()
            .containsExactly(eventExtractor.extraTypeFilters)
    }

    @Test
    fun `extractExtrasFrom contains all extracted emails`(): Unit = runBlocking {
        // Add type so tests can run
        eventExtractor.extraTypeFilters.add(Entity.TYPE_EMAIL)

        val correctAddress = "address"
        // Check with single email being returned
        entityExtractor.annotationResults.clear()
        entityExtractor.annotationResults
            .add(createAnnotationFor(correctAddress, Entity.TYPE_ADDRESS))
        expectThat(eventExtractor.extractExtrasFrom("").address).isEqualTo(correctAddress)

        // Check with multiple emails being returned
        entityExtractor.annotationResults.clear()
        entityExtractor.annotationResults
            .add(createAnnotationFor("dummy address", Entity.TYPE_ADDRESS))
        entityExtractor.annotationResults
            .add(createAnnotationFor(correctAddress, Entity.TYPE_ADDRESS))
        expectThat(eventExtractor.extractExtrasFrom("").address).isEqualTo(correctAddress)
    }

    @Test
    fun `extractExtrasFrom contains the last extracted address`(): Unit = runBlocking {
        // Add type so tests can run
        eventExtractor.extraTypeFilters.add(Entity.TYPE_ADDRESS)

        // Check with single email being returned
        entityExtractor.annotationResults.clear()
        entityExtractor.annotationResults.add(createAnnotationFor("email", Entity.TYPE_EMAIL))
        expectThat(eventExtractor.extractExtrasFrom("").emails).count().isEqualTo(1)

        // Check with multiple emails being returned
        entityExtractor.annotationResults.clear()
        entityExtractor.annotationResults.add(createAnnotationFor("email 1", Entity.TYPE_EMAIL))
        entityExtractor.annotationResults.add(createAnnotationFor("email 2", Entity.TYPE_EMAIL))
        expectThat(eventExtractor.extractExtrasFrom("").emails).count().isEqualTo(2)
    }

    @Test
    fun `extractEventFrom returns allDay event when annotation is all day`(): Unit = runBlocking {
        // Create a list of cases where the returned event should be considered all day
        val dummyDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        val testAnnotations = listOf(
            createDateTimeAnnotation("date", dummyDate, DateTimeEntity.GRANULARITY_DAY),
            createDateTimeAnnotation("date", dummyDate, DateTimeEntity.GRANULARITY_WEEK),
            createDateTimeAnnotation("date", dummyDate, DateTimeEntity.GRANULARITY_MONTH),
            createDateTimeAnnotation("date", dummyDate, DateTimeEntity.GRANULARITY_YEAR)
        )

        testAnnotations.forEach {
            entityExtractor.annotationResults.clear()
            entityExtractor.annotationResults.add(it)

            expectThat(eventExtractor.extractEventFrom("")?.isAllDay).isTrue()
        }
    }

    @Test
    fun `extractEventFrom returns !allDay event when annotation is not all day`(): Unit =
        runBlocking {
            // Create a list of cases where the returned event should be considered all day
            val dummyDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
            val testAnnotations = listOf(
                createDateTimeAnnotation("datetime", dummyDate, DateTimeEntity.GRANULARITY_HOUR),
                createDateTimeAnnotation("datetime", dummyDate, DateTimeEntity.GRANULARITY_MINUTE),
                createDateTimeAnnotation("datetime", dummyDate, DateTimeEntity.GRANULARITY_SECOND)
            )

            testAnnotations.forEach {
                entityExtractor.annotationResults.clear()
                entityExtractor.annotationResults.add(it)

                expectThat(eventExtractor.extractEventFrom("")?.isAllDay).isFalse()
            }
        }

    @Test
    fun `extractEventFrom returns event with the default duration as needed`(): Unit = runBlocking {
        // Set up a single annotation
        val dummyDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))
        val testAnnotations = createDateTimeAnnotation(
            "datetime", dummyDate.time, DateTimeEntity.GRANULARITY_HOUR
        )
        entityExtractor.annotationResults.clear()
        entityExtractor.annotationResults.add(testAnnotations)

        // Create a list of default durations to try
        val defaultDurations = listOf(
            TimeUnit.MINUTES.toMillis(30),
            TimeUnit.HOURS.toMillis(1)
        )

        // extract events and check result
        defaultDurations.forEach { durationMillis ->
            eventExtractor.defaultDuration = durationMillis
            expectThat(eventExtractor.extractEventFrom("")?.endDateTime?.time)
                .isEqualTo(dummyDate.time + durationMillis)
        }
    }

    @Test
    fun `extractEventFrom combines two event annotations`(): Unit = runBlocking {
        // Set up a single annotation
        val dummyStartDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))
        val dummyEndDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2))
        val testAnnotations = listOf( // These are backwards intentionally
            createDateTimeAnnotation(
                "datetime", dummyEndDate.time, DateTimeEntity.GRANULARITY_HOUR
            ),
            createDateTimeAnnotation(
                "datetime", dummyStartDate.time, DateTimeEntity.GRANULARITY_HOUR
            )
        )
        entityExtractor.annotationResults.clear()
        entityExtractor.annotationResults.addAll(testAnnotations)

        // extract events and check result
        val event = eventExtractor.extractEventFrom("")
        expect {
            that(event?.startDateTime).isEqualTo(dummyStartDate)
            that(event?.endDateTime).isEqualTo(dummyEndDate)
        }
    }

    @Test
    fun `extractEventFrom takes the two most recent datetime entities`(): Unit = runBlocking {
        // Set up annotations
        val initialMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        val text = "date1 date2 date3 date4 date5"
        val annotations = text.split(" ").mapIndexed { index, s ->
            createDateTimeAnnotation(
                text = s,
                initialMillis + TimeUnit.DAYS.toMillis(index.toLong()),
                granularity = DateTimeEntity.GRANULARITY_HOUR,
                startIndex = index,
                endIndex = index + 1
            )
        }
        entityExtractor.annotationResults.clear()
        entityExtractor.annotationResults.addAll(annotations)
        val expectedAnnotations = annotations
            .sortedBy { it.start }
            .takeLast(2)
            .map { it.entities.first() as DateTimeEntity }
            .sortedBy { it.timestampMillis }

        // Extract event and check result
        val event = eventExtractor.extractEventFrom(text)
        expect {
            that(event?.startDateTime?.time).isEqualTo(expectedAnnotations[0].timestampMillis)
            that(event?.endDateTime?.time).isEqualTo(expectedAnnotations[1].timestampMillis)
        }
    }

    @Test
    fun `settings are loaded from datastore`(): Unit = runBlocking {
        // Set everything to defaults
        eventExtractor.extraTypeFilters.clear()
        eventExtractor.defaultDuration = 0
        eventExtractor.ignoreAllDayEvents = false

        // Update settings
        settingsStore.updateData {
            ExtractorSettings(
                extractLocation = true,
                extractEmails = true,
                ignoreAllDayEvents = true,
                defaultEventDuration = TimeUnit.MINUTES.toMillis(30)
            )
        }

        // Wait for event extractor to be ready again
        withTimeout(2000) { eventExtractor.isReady.first { it } }

        // Check settings
        expect {
            that(eventExtractor.extraTypeFilters)
                .containsExactlyInAnyOrder(Entity.TYPE_ADDRESS, Entity.TYPE_EMAIL)
            that(eventExtractor.defaultDuration).isEqualTo(TimeUnit.MINUTES.toMillis(30))
            that(eventExtractor.ignoreAllDayEvents).isTrue()
        }
    }

    private fun createAnnotationFor(
        text: String,
        type: Int,
        startIndex: Int = 0,
        endIndex: Int = startIndex + text.length
    ): EntityAnnotation {
        return EntityAnnotation(
            text,
            startIndex,
            endIndex,
            zzafl.zzk(Entity(type))
        )
    }

    private fun createDateTimeAnnotation(
        text: String,
        timestamp: Long,
        granularity: Int,
        startIndex: Int = 0,
        endIndex: Int = text.length
    ): EntityAnnotation {
        return EntityAnnotation(
            text,
            startIndex,
            endIndex,
            zzafl.zzk(DateTimeEntity(timestamp, granularity))
        )
    }
}
