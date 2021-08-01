package com.boswelja.autoevent.notificationeventextractor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.provider.CalendarContract
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.getSystemService
import com.boswelja.autoevent.R
import com.boswelja.autoevent.eventextractor.Event
import com.boswelja.autoevent.eventextractor.EventExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NotiEventExtractorService : NotificationListenerService() {

    private val handledEventMap = mutableMapOf<Int, Int>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var ignoredPackages: List<String> = emptyList()

    private lateinit var eventExtractor: EventExtractor
    private lateinit var notificationManager: NotificationManager

    override fun onListenerConnected() {
        eventExtractor = EventExtractor(this@NotiEventExtractorService)
        notificationManager = getSystemService()!!
        createNotificationChannel()
        coroutineScope.launch {
            notiExtractorSettingsStore.updateData { it.copy(running = true) }
            notiExtractorSettingsStore.data.map { it.blocklist }.collect {
                ignoredPackages = it
            }
        }
    }

    override fun onListenerDisconnected() {
        coroutineScope.launch {
            notiExtractorSettingsStore.updateData { it.copy(running = false) }
            eventExtractor.close()
            coroutineScope.cancel()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (ignoredPackages.contains(sbn.packageName)) return
        coroutineScope.launch {
            val details = sbn.getDetails(packageManager)
            val detailsHash = details.hashCode()
            eventExtractor.extractEventFrom(details.text)?.let { event ->
                val eventHash = event.hashCode()
                // Only post notification if we haven't already got the same event info
                val oldEventForNoti = handledEventMap[detailsHash]
                if (oldEventForNoti != eventHash) {
                    // Cancel previous notification, update our map and post new notification
                    oldEventForNoti?.let { notificationManager.cancel(it) }
                    handledEventMap[detailsHash] = eventHash
                    postEventNotification(event, details)
                }
            }
        }
    }

    private fun postEventNotification(
        eventDetails: Event,
        notificationDetails: NotificationDetails
    ) {
        val notificationId = eventDetails.hashCode()
        val createEventIntent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventDetails.startDateTime.time)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, eventDetails.isAllDay)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventDetails.endDateTime.time)
        val createPendingIntent = PendingIntent.getActivity(
            this, notificationId, createEventIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val ignoreAppIntent = Intent(this, NotiActionHandler::class.java)
            .setAction(NotiActionHandler.IGNORE_PACKAGE_ACTION)
            .putExtra(NotiActionHandler.EXTRA_NOTIFICATION_ID, notificationId)
            .putExtra(NotiActionHandler.EXTRA_PACKAGE_NAME, notificationDetails.packageName)
        val ignoreAppPendingIntent = PendingIntent.getBroadcast(
            this, notificationId, ignoreAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (notificationDetails.senderName != null) {
            getString(R.string.event_noti_from_summary, notificationDetails.senderName)
        } else {
            getString(R.string.event_noti_generic_summary, notificationDetails.packageLabel)
        }

        val notification = Notification.Builder(this, EventDetailsChannel)
            .setContentTitle(getString(R.string.event_noti_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.noti_ic_event_found)
            .setContentIntent(createPendingIntent)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.noti_ic_event_block),
                    getString(R.string.event_ignore_for_app),
                    ignoreAppPendingIntent
                ).build()
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        NotificationChannel(
            EventDetailsChannel,
            getString(R.string.event_noti_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            notificationManager.createNotificationChannel(it)
        }
    }

    companion object {
        private const val EventDetailsChannel = "event-details"
    }
}
