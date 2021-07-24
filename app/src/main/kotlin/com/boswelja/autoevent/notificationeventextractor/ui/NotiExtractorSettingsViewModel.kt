package com.boswelja.autoevent.notificationeventextractor.ui

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class NotiExtractorSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    val serviceEnabled = MutableStateFlow(false)

    init {
        updateServiceStatus()
    }

    fun updateServiceStatus() {
        val context = getApplication<Application>()
        val isServiceEnabled = NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)
        serviceEnabled.tryEmit(isServiceEnabled)
    }
}
