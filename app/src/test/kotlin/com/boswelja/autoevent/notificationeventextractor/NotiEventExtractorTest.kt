package com.boswelja.autoevent.notificationeventextractor

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.autoevent.InMemoryDataStore
import com.boswelja.autoevent.eventextractor.Event
import com.boswelja.autoevent.eventextractor.EventExtractor
import com.boswelja.autoevent.eventextractor.Extras
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class NotiEventExtractorTest {

    private val settingsStore = InMemoryDataStore(NotiExtractorSettings())

    private lateinit var notiExtractor: NotiEventExtractor

    private lateinit var context: Context
    private lateinit var eventExtractor: EventExtractor
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp(): Unit = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        eventExtractor = mockk()
        notificationManager = mockk()

        every { eventExtractor.close() } just Runs
        every { notificationManager.createNotificationChannel(any()) } just Runs

        // Reset settings
        settingsStore.updateData { NotiExtractorSettings() }

        notiExtractor = NotiEventExtractor(
            context, eventExtractor, notificationManager, settingsStore
        )
    }

    @Test
    fun `changing blocklist updates the blocklist properly`(): Unit = runBlocking {
        // Set a new value
        val blocklist = listOf(
            "com.boswelja.autoevent", "com.boswelja.smartwatchextensions"
        )
        settingsStore.updateData { it.copy(blocklist = blocklist) }
        settingsStore.data.first { it.blocklist.containsAll(blocklist) }

        withTimeout(2000) {
            while (notiExtractor.ignoredPackages.isEmpty()) {
                delay(50)
            }
        }

        // Check result
        expectThat(notiExtractor.ignoredPackages).containsExactlyInAnyOrder(blocklist)
    }

    @Test
    fun `running state is updated correctly`(): Unit = runBlocking {
        val timeout = 2000L

        // Check running = true on start
        withTimeout(timeout) { settingsStore.data.map { it.running }.first { it } }

        // Close the extractor and make sure running = false
        notiExtractor.close()
        withTimeout(timeout) { settingsStore.data.map { it.running }.first { !it } }
    }

    @Test
    fun `settings aren't updated after closing`(): Unit = runBlocking {
        // Reset internal blocklist and close
        notiExtractor.ignoredPackages = emptyList()
        notiExtractor.close()

        // Set a new value
        val blocklist = listOf(
            "com.boswelja.autoevent", "com.boswelja.smartwatchextensions"
        )
        settingsStore.updateData { it.copy(blocklist = blocklist) }
        settingsStore.data.first { it.blocklist.containsAll(blocklist) }

        // Check result
        expectThat(notiExtractor.ignoredPackages).isEmpty()
    }

    @Test
    fun `getEventFor returns result from EventExtractor`(): Unit = runBlocking {
        val details = NotificationDetails("text", "", "", "")
        // Mock no event
        coEvery { eventExtractor.extractEventFrom(any()) } returns null

        // Make the call and check result
        expectThat(notiExtractor.getEventFor(details)).isNull()
        coVerify { eventExtractor.extractEventFrom(details.text) }

        // Mock event
        coEvery {
            eventExtractor.extractEventFrom(any())
        } returns Event(Date(), Date(), true, Extras())

        // Make the call and check result
        expectThat(notiExtractor.getEventFor(details)).isNotNull()
        coVerify { eventExtractor.extractEventFrom(details.text) }
    }

    @Test
    fun `getEventFor returns null if package is on blocklist`(): Unit = runBlocking {
        // Set up details for blocked package
        val packageName = "packageName"
        val details = NotificationDetails("text", "", "", packageName)
        notiExtractor.ignoredPackages = listOf(packageName)

        // Make the call and check result
        expectThat(notiExtractor.getEventFor(details)).isNull()
        coVerify(inverse = true) { eventExtractor.extractEventFrom(details.text) }
    }
}
