package com.boswelja.autoevent.notificationeventextractor

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotiListenerService : NotificationListenerService() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private lateinit var notiExtractor: NotiEventExtractor

    override fun onBind(intent: Intent?): IBinder = Binder()

    override fun onListenerConnected() {
        notiExtractor = NotiEventExtractor(applicationContext)
    }

    override fun onListenerDisconnected() {
        notiExtractor.close()
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
