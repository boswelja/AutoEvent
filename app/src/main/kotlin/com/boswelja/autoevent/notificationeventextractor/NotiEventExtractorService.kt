package com.boswelja.autoevent.notificationeventextractor

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.entityextraction.Entity
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

class NotiEventExtractorService : NotificationListenerService() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val modelDownloaded = MutableStateFlow(false)

    private lateinit var eventExtractor: EntityExtractor

    override fun onListenerConnected() {
        Log.i("NotiListenerService", "Listener connected")
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
        coroutineScope.launch {
            modelDownloaded.first { it }
            val text = if (sbn.notification.extras.containsKey(Notification.EXTRA_MESSAGES)) {
                Log.i("NotiListenerService", "Got MessageStyle notification")
                val messageStyle = NotificationCompat.MessagingStyle
                    .extractMessagingStyleFromNotification(sbn.notification)
                messageStyle?.messages?.filter {
                    !it.text.isNullOrBlank()
                }?.joinToString { it.text!! }
            } else {
                sbn.notification.extras.getString(Notification.EXTRA_TEXT)
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
                    Log.i("NotiListenerService", it.toString())
                }
        }
    }
}
