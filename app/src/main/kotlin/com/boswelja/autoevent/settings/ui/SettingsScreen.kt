package com.boswelja.autoevent.settings.ui

import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.autoevent.R
import com.boswelja.autoevent.notificationlistener.NotiListenerService
import com.boswelja.autoevent.settings.NotificationListenerSettings
import kotlinx.coroutines.Dispatchers

@ExperimentalMaterialApi
@Preview
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel()

    val isServiceEnabled by viewModel.serviceEnabled.collectAsState(Dispatchers.IO)

    val listenerSettingsLauncher = rememberLauncherForActivityResult(
        NotificationListenerSettings()
    ) {
        viewModel.updateServiceStatus()
    }

    Column(modifier) {
        ListItem(
            modifier = Modifier
                .clickable {
                    listenerSettingsLauncher.launch(
                        ComponentName(context, NotiListenerService::class.java)
                    )
                },
            text = {
                Text(stringResource(R.string.noti_listener_settings_title))
            },
            secondaryText = {
                val text = if (isServiceEnabled) {
                    stringResource(R.string.noti_listener_settings_enabled_desc)
                } else {
                    stringResource(R.string.noti_listener_settings_disabled_desc)
                }
                Text(text)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null
                )
            }
        )
    }
}
