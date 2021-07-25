package com.boswelja.autoevent.notificationeventextractor

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotiActionHandler : BroadcastReceiver() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        when (intent.action) {
            IGNORE_PACKAGE_ACTION -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return
                val notiId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
                context.getSystemService<NotificationManager>()?.cancel(notiId)
                val result = goAsync()
                coroutineScope.launch {
                    context.notiExtractorSettingsStore.updateData {
                        val newList = it.blocklist + packageName
                        it.copy(blocklist = newList)
                    }
                    result.finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "noti_id"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val IGNORE_PACKAGE_ACTION = "com.boswelja.autoevent.IGNORE_PACKAGE"
    }
}
