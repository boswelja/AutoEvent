package com.boswelja.autoevent.notificationeventextractor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.provider.CalendarContract
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import com.boswelja.autoevent.R
import com.boswelja.autoevent.eventextractor.Event
import com.boswelja.autoevent.eventextractor.EventExtractor
import java.io.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A class for extracting [Event]s from [NotificationDetails], and handling event notifications.
 * @param context [Context].
 * @param eventExtractor The [EventExtractor] instance to use.
 * @param notificationManager [NotificationManager].
 * @param settingsStore The [DataStore] to read [NotiExtractorSettings] from.
 */
class NotiEventExtractor(
    private val context: Context,
    private val eventExtractor: EventExtractor = EventExtractor(context),
    private val notificationManager: NotificationManager = context.getSystemService()!!,
    private val settingsStore: DataStore<NotiExtractorSettings> = context.notiExtractorSettingsStore
) : Closeable {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    internal var ignoredPackages: List<String> = emptyList()

    init {
        createNotificationChannel()
        coroutineScope.launch {
            // Mark extractor as running
            settingsStore.updateData { it.copy(running = true) }
            // Collect blocklist changes
            settingsStore.data.map { it.blocklist }.collect {
                ignoredPackages = it
            }
        }
    }

    override fun close() {
        coroutineScope.launch {
            settingsStore.updateData { it.copy(running = false) }
            eventExtractor.close()
            coroutineScope.cancel()
        }
    }

    /**
     * Gets an [Event] from the details provided by the given [NotificationDetails]. Can be null if
     * no event is found, or the details are for a notification from a package on the blocklist.
     * @param details The [NotificationDetails] to use for creating an event.
     * @return The [Event] found. Can be null if no event is found, or the details are for a
     * notification from a package on the blocklist.
     */
    suspend fun getEventFor(details: NotificationDetails): Event? {
        if (!ignoredPackages.contains(details.packageName)) {
            return eventExtractor.extractEventFrom(details.text)
        }
        return null
    }

    /**
     * Post a notification for the given event and notification details.
     * @param event The [Event] to post a notification for.
     * @param details The [NotificationDetails] that generated the [Event].
     */
    fun postEventNotification(event: Event, details: NotificationDetails) {
        val notificationId = event.hashCode()
        val createPendingIntent = getCreateIntentForEvent(notificationId, event)
        val ignoreAppPendingIntent = getIgnoreAppIntent(notificationId, details.packageName)

        val contentText = if (details.senderName != null) {
            context.getString(R.string.event_noti_from_summary, details.senderName)
        } else {
            context.getString(R.string.event_noti_generic_summary, details.packageLabel)
        }

        val notification = Notification.Builder(context, EventDetailsChannel)
            .setContentTitle(context.getString(R.string.event_noti_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.noti_ic_event_found)
            .setContentIntent(createPendingIntent)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.noti_ic_event_block),
                    context.getString(R.string.event_ignore_for_app),
                    ignoreAppPendingIntent
                ).build()
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * Get a [PendingIntent] that can be used to launch the system calendar "add event" activity for
     * a given [Event].
     * @param id The [PendingIntent] requestCode. This should be a non-zero, unique value.
     * @param event The [Event] to build an intent for.
     * @return The created [PendingIntent].
     */
    private fun getCreateIntentForEvent(id: Int, event: Event): PendingIntent {
        val createEventIntent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startDateTime.time)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.isAllDay)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endDateTime.time)
        return PendingIntent.getActivity(
            context, id, createEventIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Get a [PendingIntent] that can be used to add a package to the blocklist.
     * @param id The [PendingIntent] requestCode. This should be a non-zero, unique value.
     * @param packageName The [NotificationDetails.packageName] to add to the blocklist.
     * @return The created [PendingIntent].
     */
    private fun getIgnoreAppIntent(id: Int, packageName: String): PendingIntent {
        val ignoreAppIntent = Intent(context, NotiActionReceiver::class.java)
            .setAction(NotiActionReceiver.IGNORE_PACKAGE_ACTION)
            .putExtra(NotiActionReceiver.EXTRA_NOTIFICATION_ID, id)
            .putExtra(NotiActionReceiver.EXTRA_PACKAGE_NAME, packageName)
        return PendingIntent.getBroadcast(
            context, id, ignoreAppIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Create a [NotificationChannel] for discovered events. The created channel has an ID of
     * [EventDetailsChannel].
     */
    private fun createNotificationChannel() {
        NotificationChannel(
            EventDetailsChannel,
            context.getString(R.string.event_noti_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            notificationManager.createNotificationChannel(it)
        }
    }

    companion object {
        private const val EventDetailsChannel = "event-details"
    }
}
