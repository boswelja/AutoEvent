package com.boswelja.autoevent.notificationeventextractor.ui

import android.app.Application
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.autoevent.common.AppInfo
import com.boswelja.autoevent.notificationeventextractor.notiExtractorSettingsStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BlocklistViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val packageManager = application.packageManager
    private val dataStore = application.notiExtractorSettingsStore

    val blocklist = dataStore.data.map {
        it.blocklist.map { packageName ->
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            AppInfo(
                applicationInfo.loadIcon(packageManager).toBitmap(),
                applicationInfo.loadLabel(packageManager).toString(),
                packageName
            )
        }.sortedBy { appInfo -> appInfo.name }
    }

    fun removeFromBlocklist(packageName: String) {
        viewModelScope.launch {
            dataStore.updateData {
                val newList = it.blocklist - packageName
                it.copy(blocklist = newList)
            }
        }
    }
}
