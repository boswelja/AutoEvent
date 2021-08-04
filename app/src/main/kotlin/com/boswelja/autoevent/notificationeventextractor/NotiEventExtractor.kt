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

class NotiEventExtractor(
    private val context: Context,
    private val eventExtractor: EventExtractor = EventExtractor(context),
    private val notificationManager: NotificationManager = context.getSystemService()!!,
    private val settingsStore: DataStore<NotiExtractorSettings> = context.notiExtractorSettingsStore
) : Closeable {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var ignoredPackages: List<String> = emptyList()

    init {
        createNotificationChannel()
        coroutineScope.launch {
            settingsStore.updateData { it.copy(running = true) }
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

    suspend fun getNewEventFor(details: NotificationDetails): Event? {
        if (!ignoredPackages.contains(details.packageName)) {
            return eventExtractor.extractEventFrom(details.text)
        }
        return null
    }

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

    private fun getIgnoreAppIntent(id: Int, packageName: String): PendingIntent {
        val ignoreAppIntent = Intent(context, NotiActionReceiver::class.java)
            .setAction(NotiActionReceiver.IGNORE_PACKAGE_ACTION)
            .putExtra(NotiActionReceiver.EXTRA_NOTIFICATION_ID, id)
            .putExtra(NotiActionReceiver.EXTRA_PACKAGE_NAME, packageName)
        return PendingIntent.getBroadcast(
            context, id, ignoreAppIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

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
