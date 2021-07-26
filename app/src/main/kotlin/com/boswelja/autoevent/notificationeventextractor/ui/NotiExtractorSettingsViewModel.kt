package com.boswelja.autoevent.notificationeventextractor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.autoevent.notificationeventextractor.notiExtractorSettingsStore
import kotlinx.coroutines.flow.map

class NotiExtractorSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStore = application.notiExtractorSettingsStore

    val serviceEnabled = dataStore.data.map { it.running }
    val blocklistCount = dataStore.data.map { it.blocklist.count() }
}
