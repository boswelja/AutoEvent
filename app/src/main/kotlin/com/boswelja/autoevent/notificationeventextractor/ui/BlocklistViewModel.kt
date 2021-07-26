package com.boswelja.autoevent.notificationeventextractor.ui

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.autoevent.common.AppInfo
import com.boswelja.autoevent.notificationeventextractor.notiExtractorSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BlocklistViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val packageManager = application.packageManager
    private val dataStore = application.notiExtractorSettingsStore

    val allApps = MutableStateFlow(emptyList<AppInfo>())

    val blocklist = dataStore.data.map {
        it.blocklist.map { packageName ->
            try {
                val applicationInfo = packageManager.getApplicationInfo(
                    packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES
                )
                AppInfo(
                    applicationInfo.loadIcon(packageManager).toBitmap(),
                    applicationInfo.loadLabel(packageManager).toString(),
                    packageName
                )
            } catch (e: PackageManager.NameNotFoundException) {
                AppInfo(
                    null,
                    packageName,
                    packageName
                )
            }
        }.sortedBy { appInfo -> appInfo.name }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val allAppInfos = packageManager.getInstalledApplications(0)
                .map { applicationInfo ->
                    AppInfo(
                        applicationInfo.loadIcon(packageManager).toBitmap(),
                        applicationInfo.loadLabel(packageManager).toString(),
                        applicationInfo.packageName
                    )
                }
                .sortedBy { it.name }
            allApps.emit(allAppInfos)
        }
    }

    fun removeFromBlocklist(packageName: String) {
        viewModelScope.launch {
            dataStore.updateData {
                val newList = it.blocklist - packageName
                it.copy(blocklist = newList)
            }
        }
    }

    fun addToBlocklist(packageName: String) {
        viewModelScope.launch {
            dataStore.updateData {
                val newList = it.blocklist + packageName
                it.copy(blocklist = newList)
            }
        }
    }
}
