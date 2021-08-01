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
import androidx.core.app.NotificationCompat
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

    private val notiIdMap = mutableMapOf<Int, Int>()
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
            val text = sbn.getDetails()
            eventExtractor.extractEventFrom(text.text)?.let { event ->
                val eventHash = event.hashCode()
                // Only post notification if we haven't already got the same event info
                val oldEventForNoti = notiIdMap[sbn.id]
                if (oldEventForNoti != eventHash) {
                    // Cancel previous notification, update our map and post new notification
                    oldEventForNoti?.let { notificationManager.cancel(it) }
                    notiIdMap[sbn.id] = eventHash
                    postEventNotification(event, sbn.packageName)
                }
            }
        }
    }

    /**
     * Extract some details about the [StatusBarNotification].
     * @return A [NotificationDetails] containing data about this notification.
     */
    private fun StatusBarNotification.getDetails(): NotificationDetails {
        // Load message style if it exists
        val messageStyle = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(notification)

        if (messageStyle != null) {
            // Message style found, get details
            val text = messageStyle.messages
                .filter { !it.text.isNullOrBlank() }
                .joinToString { it.text!! }
            val sender = messageStyle.conversationTitle
                ?: messageStyle.messages.firstOrNull { it.person != null }?.person?.name
            val packageName = packageManager.getApplicationInfo(this.packageName, 0)
                .loadLabel(packageManager)
            return NotificationDetails(text, sender?.toString(), packageName.toString())
        } else {
            // No message style found
            val text = notification.extras.getString(Notification.EXTRA_TEXT, "")
            val packageName = packageManager.getApplicationInfo(this.packageName, 0)
                .loadLabel(packageManager)
            return NotificationDetails(text, null, packageName.toString())
        }
    }

    private fun postEventNotification(eventDetails: Event, packageName: String) {
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
            .putExtra(NotiActionHandler.EXTRA_PACKAGE_NAME, packageName)
        val ignoreAppPendingIntent = PendingIntent.getBroadcast(
            this, notificationId, ignoreAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, EventDetailsChannel)
            .setContentTitle(getString(R.string.event_noti_title))
            .setContentText("Haha text go brrrr")
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
