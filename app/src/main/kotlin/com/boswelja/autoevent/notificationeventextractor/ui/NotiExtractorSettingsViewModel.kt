package com.boswelja.autoevent.notificationeventextractor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.autoevent.notificationeventextractor.notiExtractorSettingsStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NotiExtractorSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStore = application.notiExtractorSettingsStore

    val serviceEnabled = dataStore.data.map { it.running }
    val blocklist = dataStore.data.map { it.blocklist }

    fun updateBlocklist(newList: List<String>) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(blocklist = newList) }
        }
    }
}
