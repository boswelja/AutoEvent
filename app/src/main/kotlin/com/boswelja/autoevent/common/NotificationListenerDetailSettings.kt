package com.boswelja.autoevent.common

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract

class NotificationListenerDetailSettings : ActivityResultContract<ComponentName, Unit>() {

    override fun createIntent(context: Context, input: ComponentName?): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && input != null) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, input.flattenToString()
                )
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Unit = Unit
}
