package com.boswelja.autoevent.notificationeventextractor.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.autoevent.R
import com.boswelja.autoevent.main.ui.Destinations
import com.boswelja.autoevent.notificationeventextractor.NotiEventExtractorService
import kotlinx.coroutines.Dispatchers

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun NotiExtractorSettings(
    modifier: Modifier = Modifier,
    onNavigate: (Destinations) -> Unit
) {
    val context = LocalContext.current
    val viewModel: NotiExtractorSettingsViewModel = viewModel()

    val serviceEnabled by viewModel.serviceEnabled.collectAsState(false, Dispatchers.IO)
    val blocklistCount by viewModel.blocklistCount.collectAsState(0, Dispatchers.IO)

    Column(modifier) {
        ListItem(
            modifier = Modifier.clickable {
                launchNotiListenerSettings(context)
            },
            text = { Text(stringResource(R.string.noti_listener_settings_title)) },
            secondaryText = {
                val text = if (serviceEnabled) {
                    stringResource(R.string.noti_listener_settings_enabled_desc)
                } else {
                    stringResource(R.string.noti_listener_settings_disabled_desc)
                }
                Text(text)
            },
            icon = { Icon(Icons.Default.Notifications, null) }
        )
        BlocklistSetting(
            enabled = serviceEnabled,
            blocklistCount = blocklistCount,
            onNavigateToBlocklist = { onNavigate(Destinations.BLOCKLIST) }
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun BlocklistSetting(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    blocklistCount: Int,
    onNavigateToBlocklist: () -> Unit
) {
    val context = LocalContext.current

    ListItem(
        modifier = modifier.clickable(enabled = enabled, onClick = onNavigateToBlocklist),
        text = { Text(stringResource(R.string.noti_extractor_blocklist_title)) },
        secondaryText = {
            val text = context.resources.getQuantityString(
                R.plurals.noti_extractor_blocklist_summary,
                blocklistCount,
                blocklistCount
            )
            Text(text)
        },
        icon = { Icon(Icons.Default.AppBlocking, null) }
    )
}

private fun launchNotiListenerSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
            .putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                ComponentName(context, NotiEventExtractorService::class.java).flattenToString()
            )
    } else {
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
    context.startActivity(intent)
}
