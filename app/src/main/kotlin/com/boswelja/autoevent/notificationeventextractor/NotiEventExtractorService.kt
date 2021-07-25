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
import java.text.DateFormat

class NotiEventExtractorService : NotificationListenerService() {

    private val notiIdMap = mutableMapOf<Int, Event>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var ignoredPackages: List<String> = emptyList()

    private lateinit var eventExtractor: EventExtractor
    private lateinit var notificationManager: NotificationManager

    override fun onListenerConnected() {
        eventExtractor = EventExtractor(this@NotiEventExtractorService)
        notificationManager = getSystemService()!!
        createNotificationChannel()
        coroutineScope.launch {
            notiExtractorSettingsStore.data.map { it.blocklist }.collect {
                ignoredPackages = it
            }
        }
    }

    override fun onListenerDisconnected() {
        coroutineScope.cancel()
        eventExtractor.close()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (ignoredPackages.contains(sbn.packageName)) return
        coroutineScope.launch {
            val text = sbn.notification.allText()
            eventExtractor.extractEventFrom(text)?.let { event ->
                // Only post notification if we haven't already got the same event info
                val oldEventForNoti = notiIdMap[sbn.id]
                if (oldEventForNoti != event) {
                    // Cancel previous notification, update our map and post new notification
                    oldEventForNoti?.let { notificationManager.cancel(it.hashCode()) }
                    notiIdMap[sbn.id] = event
                    postEventNotification(event, sbn.packageName)
                }
            }
        }
    }

    private fun Notification.allText(): String {
        val messageStyle = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(this)
        val messageStyleText = messageStyle?.messages
            ?.filter { !it.text.isNullOrBlank() }
            ?.joinToString { it.text!! }
        return messageStyleText ?: extras.getString(Notification.EXTRA_TEXT, "")
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

        val dateFormatter = DateFormat.getDateTimeInstance()
        val formattedStart = dateFormatter.format(eventDetails.startDateTime.time)

        val notification = Notification.Builder(this, EventDetailsChannel)
            .setContentTitle(getString(R.string.event_noti_title))
            .setContentText(getString(R.string.event_noti_summary, formattedStart))
            .setSmallIcon(R.drawable.noti_ic_event_found)
            .setStyle(createNotificationStyleForEvent(eventDetails))
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.noti_ic_event_add),
                    getString(R.string.event_add_to_calendar),
                    createPendingIntent
                ).build()
            )
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

    private fun createNotificationStyleForEvent(event: Event): Notification.Style {
        val formatter =
            if (event.isAllDay) DateFormat.getDateInstance() else DateFormat.getDateTimeInstance()
        val formattedStart = formatter.format(event.startDateTime)
        val formattedEnd = formatter.format(event.endDateTime)
        var text = getString(R.string.event_from, formattedStart)
        text += "\n${getString(R.string.event_to, formattedEnd)}"
        event.extras.address?.let { address ->
            text += "\n${getString(R.string.event_at, address)}"
        }
        return Notification.BigTextStyle().bigText(text)
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
