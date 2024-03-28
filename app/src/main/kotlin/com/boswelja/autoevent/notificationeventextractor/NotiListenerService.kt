package com.boswelja.autoevent.notificationeventextractor

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotiListenerService : NotificationListenerService() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private lateinit var notiExtractor: NotiEventExtractor

    override fun onListenerConnected() {
        notiExtractor = NotiEventExtractor(applicationContext)
    }

    override fun onListenerDisconnected() {
        if (::notiExtractor.isInitialized) notiExtractor.close()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        coroutineScope.launch {
            val details = sbn.getDetails(packageManager)
            notiExtractor.getEventFor(details)?.let { event ->
                notiExtractor.postEventNotification(event, details)
            }
        }
    }
}
