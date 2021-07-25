package com.boswelja.autoevent.notificationeventextractor.ui

import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.NotificationListenerDetailSettings
import com.boswelja.autoevent.common.ui.AnimatedVisibilityItem
import com.boswelja.autoevent.common.ui.ListDialog
import com.boswelja.autoevent.notificationeventextractor.NotiEventExtractorService
import kotlinx.coroutines.Dispatchers

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun NotiExtractorSettings(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: NotiExtractorSettingsViewModel = viewModel()

    val listenerSettingsLauncher = rememberLauncherForActivityResult(
        NotificationListenerDetailSettings()
    ) {
        viewModel.updateServiceStatus()
    }

    val serviceEnabled by viewModel.serviceEnabled.collectAsState()
    val blocklist by viewModel.blocklist.collectAsState(emptyList(), Dispatchers.IO)

    Column(modifier) {
        ListItem(
            modifier = Modifier.clickable {
                listenerSettingsLauncher.launch(
                    ComponentName(context, NotiEventExtractorService::class.java)
                )
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
            blocklist = blocklist,
            onListChanged = viewModel::updateBlocklist
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun BlocklistSetting(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    blocklist: List<String>,
    onListChanged: (List<String>) -> Unit
) {
    val context = LocalContext.current

    var dialogVisible by remember {
        mutableStateOf(false)
    }
    val list = remember(blocklist) {
        blocklist.toMutableStateList()
    }

    ListItem(
        modifier = modifier.clickable(enabled = enabled) { dialogVisible = true },
        text = { Text(stringResource(R.string.noti_extractor_blocklist_title)) },
        secondaryText = {
            val count = blocklist.count()
            val text = context.resources.getQuantityString(
                R.plurals.noti_extractor_blocklist_summary,
                count,
                count
            )
            Text(text)
        },
        icon = { Icon(Icons.Default.AppBlocking, null) }
    )

    if (dialogVisible) {
        ListDialog(
            title = { Text(stringResource(R.string.noti_extractor_blocklist_title)) },
            onDismissRequest = {
                onListChanged(list)
                dialogVisible = false
            },
            itemContent = {
                var remove by remember {
                    mutableStateOf(false)
                }
                AnimatedVisibilityItem(
                    remove = remove,
                    item = it,
                    onItemRemoved = list::remove,
                ) {
                    ListItem(
                        text = { Text(it) },
                        trailing = {
                            TextButton(onClick = { remove = true }) {
                                Text(stringResource(R.string.remove))
                            }
                        }
                    )
                }
            },
            noItemsContent = {
                ListItem {
                    Text(stringResource(R.string.noti_extractor_blocklist_none))
                }
            },
            items = list
        )
    }
}
