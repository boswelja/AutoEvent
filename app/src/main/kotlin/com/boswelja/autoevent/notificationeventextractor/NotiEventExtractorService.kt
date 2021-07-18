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
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.entityextraction.DateTimeEntity
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class NotiEventExtractorService : NotificationListenerService() {

    private val idCounter = AtomicInteger()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val modelDownloaded = MutableStateFlow(false)

    private lateinit var notificationManager: NotificationManager
    private lateinit var eventExtractor: EntityExtractor

    override fun onListenerConnected() {
        Log.i("NotiListenerService", "Listener connected")
        notificationManager = getSystemService()!!

        createNotificationChannel()

        val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
            .build()
        eventExtractor = EntityExtraction.getClient(options)

        // Download model if needed
        val modelDownloadConditions = DownloadConditions.Builder()
            .build()
        eventExtractor.downloadModelIfNeeded(modelDownloadConditions)
            .addOnSuccessListener {
                Log.i("NotiListenerService", "Model downloaded")
                modelDownloaded.tryEmit(true)
            }
            .addOnFailureListener {
                throw it
            }
    }

    override fun onListenerDisconnected() {
        Log.i("NotiListenerService", "Listener disconnected")
        coroutineScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        Log.d("NotiEventExtractorService", "Got notification")
        coroutineScope.launch {
            Log.d("NotiEventExtractorService", "Checking model is downloaded")
            modelDownloaded.first { it }
            Log.d("NotiEventExtractorService", "Model downloaded")
            val messageStyle = NotificationCompat.MessagingStyle
                .extractMessagingStyleFromNotification(sbn.notification)
            val text = if (messageStyle != null) {
                Log.i("NotiListenerService", "Got MessageStyle notification")
                messageStyle.messages.filter { !it.text.isNullOrBlank() }.joinToString { it.text!! }
            } else {
                Log.d("NotiEventExtractorService", "Got standard notification")
                sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
            }
            val params = EntityExtractionParams.Builder(text)
                .setEntityTypesFilter(
                    setOf(
                        Entity.TYPE_DATE_TIME,
                        Entity.TYPE_ADDRESS
                    )
                )
                .build()
            eventExtractor.annotate(params)
                .addOnSuccessListener {
                    parseAnnotations(it)
                }
        }
    }

    private fun parseAnnotations(annotations: List<EntityAnnotation>) {
        Log.d("NotiEventExtractorService", "Got ${annotations.count()} annotations")
        annotations.forEach { annotation ->
            val entities = annotation.entities
            Log.d("NotiEventExtractorService", "Got ${entities.count()} entities")
            entities.forEach { entity ->
                when (entity) {
                    is DateTimeEntity -> {
                        Log.d("NotiEventExtractorService", "${entity.dateTimeGranularity}")
                        Log.d("NotiEventExtractorService", "${entity.timestampMillis}")
                        val endMillis = estimateEndTimeMillis(
                            entity.dateTimeGranularity, entity.timestampMillis
                        )
                        postEventNotification(
                            entity.timestampMillis,
                            endMillis
                        )
                    }
                    else -> {
                        Log.d("NotiEventExtractorService", "$entity")
                    }
                }
            }
        }
    }

    private fun estimateEndTimeMillis(granularity: Int, startMillis: Long): Long {
        val durationMillis = when (granularity) {
            DateTimeEntity.GRANULARITY_DAY -> TimeUnit.DAYS.toMillis(1)
            DateTimeEntity.GRANULARITY_MONTH -> TimeUnit.DAYS.toMillis(30)
            DateTimeEntity.GRANULARITY_WEEK -> TimeUnit.DAYS.toMillis(7)
            DateTimeEntity.GRANULARITY_YEAR -> TimeUnit.DAYS.toMillis(365)
            else -> TimeUnit.HOURS.toMillis(1)
        }
        return startMillis + durationMillis
    }

    private fun postEventNotification(
        startMillis: Long,
        endMillis: Long,
        location: String? = null
    ) {
        val notificationId = idCounter.incrementAndGet()
        val createEventIntent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        val createPendingIntent = PendingIntent.getActivity(
            this, notificationId, createEventIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val dateFormatter = DateFormat.getDateTimeInstance()

        val startDateTime = dateFormatter.format(Date(startMillis))
        val detailString = StringBuilder()
        detailString.appendLine(getString(R.string.event_from, startDateTime))
        detailString.appendLine(getString(R.string.event_to, dateFormatter.format(Date(endMillis))))
        location?.let {
            detailString.append(getString(R.string.event_at, location))
        }

        val notification = Notification.Builder(this, EventDetailsChannel)
            .setContentTitle(getString(R.string.event_noti_title))
            .setContentText(getString(R.string.event_noti_summary, startDateTime))
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
