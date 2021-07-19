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
import com.boswelja.autoevent.eventextractor.AllDayEvent
import com.boswelja.autoevent.eventextractor.DateTimeEvent
import com.boswelja.autoevent.eventextractor.Event
import com.boswelja.autoevent.eventextractor.EventExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.concurrent.atomic.AtomicInteger

class NotiEventExtractorService : NotificationListenerService() {

    private val idCounter = AtomicInteger()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private lateinit var eventExtractor: EventExtractor
    private lateinit var notificationManager: NotificationManager

    override fun onListenerConnected() {
        Log.i("NotiEventExtractorService", "Listener connected")
        eventExtractor = EventExtractor(this@NotiEventExtractorService)
        notificationManager = getSystemService()!!
        createNotificationChannel()
    }

    override fun onListenerDisconnected() {
        Log.i("NotiListenerService", "Listener disconnected")
        coroutineScope.cancel()
        eventExtractor.close()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
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
            eventExtractor.extractEventFrom(text)?.let {
                postEventNotification(it)
            }
        }
    }

    private fun postEventNotification(eventDetails: Event) {
        val notificationId = idCounter.incrementAndGet()
        val createEventIntent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventDetails.startDateTime.time)
        when (eventDetails) {
            is AllDayEvent -> {
                createEventIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            }
            is DateTimeEvent -> {
                createEventIntent.putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME, eventDetails.endDateTime.time
                )
            }
        }
        val createPendingIntent = PendingIntent.getActivity(
            this, notificationId, createEventIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val dateFormatter = DateFormat.getDateTimeInstance()
        val formattedStart = dateFormatter.format(eventDetails.startDateTime.time)

        val detailString = StringBuilder()
        detailString.appendLine(getString(R.string.event_from, formattedStart))

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
