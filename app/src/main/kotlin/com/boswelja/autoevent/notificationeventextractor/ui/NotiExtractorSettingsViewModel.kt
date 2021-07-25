package com.boswelja.autoevent.notificationeventextractor.ui

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.autoevent.notificationeventextractor.notiExtractorSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NotiExtractorSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStore = application.notiExtractorSettingsStore

    val serviceEnabled = MutableStateFlow(false)
    val blocklist = dataStore.data.map { it.blocklist }

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

    fun updateBlocklist(newList: List<String>) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(blocklist = newList) }
        }
    }
}
