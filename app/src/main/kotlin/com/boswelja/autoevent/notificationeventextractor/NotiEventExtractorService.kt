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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.autoevent.R
import com.boswelja.autoevent.eventextractor.EventDetails
import com.boswelja.autoevent.eventextractor.EventExtractor
import com.boswelja.autoevent.eventextractor.extractorSettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.concurrent.atomic.AtomicInteger

class NotiEventExtractorService : NotificationListenerService() {

    private val idCounter = AtomicInteger()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var eventExtractor: EventExtractor? = null
    private lateinit var notificationManager: NotificationManager

    override fun onListenerConnected() {
        Log.i("NotiEventExtractorService", "Listener connected")
        coroutineScope.launch {
            extractorSettingsDataStore.data.map { it.language }.collect {
                eventExtractor?.close()
                eventExtractor = EventExtractor(it)
            }
        }
        notificationManager = getSystemService()!!
        createNotificationChannel()
    }

    override fun onListenerDisconnected() {
        Log.i("NotiListenerService", "Listener disconnected")
        coroutineScope.cancel()
        eventExtractor?.close()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (eventExtractor == null) return
        Log.d("NotiEventExtractorService", "Got notification")
        coroutineScope.launch {
            val messageStyle = NotificationCompat.MessagingStyle
                .extractMessagingStyleFromNotification(sbn.notification)
            val text = if (messageStyle != null) {
                Log.d("NotiEventExtractorService", "Got MessageStyle notification")
                messageStyle.messages.filter { !it.text.isNullOrBlank() }.joinToString { it.text!! }
            } else {
                Log.d("NotiEventExtractorService", "Got standard notification")
                sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
            }
            val eventDetails = eventExtractor!!.extractEventsFrom(text)
            Log.d("NotiEventExtractorService", "Got ${eventDetails.count()} event details")
            eventDetails.forEach {
                postEventNotification(it)
            }
        }
    }

    private fun postEventNotification(eventDetails: EventDetails) {
        val notificationId = idCounter.incrementAndGet()
        val createEventIntent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventDetails.start.time)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventDetails.end.time)
        val createPendingIntent = PendingIntent.getActivity(
            this, notificationId, createEventIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val dateFormatter = DateFormat.getDateTimeInstance()
        val formattedStart = dateFormatter.format(eventDetails.start)
        val formattedEnd = dateFormatter.format(eventDetails.end)

        val detailString = StringBuilder()
        detailString.appendLine(getString(R.string.event_from, formattedStart))
        detailString.appendLine(getString(R.string.event_to, formattedEnd))

        val notification = Notification.Builder(this, EventDetailsChannel)
            .setContentTitle(getString(R.string.event_noti_title))
            .setContentText(getString(R.string.event_noti_summary, formattedStart))
            .setSmallIcon(R.drawable.noti_ic_event_found)
            .setStyle(
                Notification.BigTextStyle().bigText(detailString.toString())
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.noti_ic_event_add),
                    getString(R.string.event_add_to_calendar),
                    createPendingIntent
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
