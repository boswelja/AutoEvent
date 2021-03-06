package com.boswelja.autoevent.eventextractor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.autoevent.eventextractor.ExtractorSettings
import com.boswelja.autoevent.eventextractor.extractorSettingsDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ExtractorSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val extractorSettingsDataStore = application.extractorSettingsDataStore

    val extractorLanguage = extractorSettingsDataStore.data.map { it.language }
    val extractEmails = extractorSettingsDataStore.data.map { it.extractEmails }
    val extractAddress = extractorSettingsDataStore.data.map { it.extractLocation }
    val ignoreAllDayEvents = extractorSettingsDataStore.data.map { it.ignoreAllDayEvents }
    val defaultDuration = extractorSettingsDataStore.data.map { it.defaultEventDuration }

    fun updateExtractEmails(newValue: Boolean) {
        viewModelScope.launch {
            extractorSettingsDataStore.updateData { it.copy(extractEmails = newValue) }
        }
    }

    fun updateExtractAddress(newValue: Boolean) {
        viewModelScope.launch {
            extractorSettingsDataStore.updateData { it.copy(extractLocation = newValue) }
        }
    }

    fun updateExtractLanguage(newValue: ExtractorSettings.ExtractorLanguage) {
        viewModelScope.launch {
            extractorSettingsDataStore.updateData { it.copy(language = newValue) }
        }
    }

    fun updateAllDayEvents(newValue: Boolean) {
        viewModelScope.launch {
            extractorSettingsDataStore.updateData { it.copy(ignoreAllDayEvents = newValue) }
        }
    }

    fun updateDefaultEventDuration(newValue: Long) {
        viewModelScope.launch {
            extractorSettingsDataStore.updateData { it.copy(defaultEventDuration = newValue) }
        }
    }
}
