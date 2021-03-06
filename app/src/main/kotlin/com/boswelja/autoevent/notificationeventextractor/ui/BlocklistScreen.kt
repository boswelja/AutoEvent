package com.boswelja.autoevent.notificationeventextractor.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.AppInfo
import com.boswelja.autoevent.common.ui.CardHeader
import com.boswelja.autoevent.common.ui.SimpleDialog
import kotlinx.coroutines.Dispatchers

@ExperimentalMaterialApi
@Composable
fun BlocklistScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: BlocklistViewModel = viewModel()
    val blocklist by viewModel.blocklist.collectAsState(emptyList(), Dispatchers.IO)

    var addDialogVisible by remember {
        mutableStateOf(false)
    }

    Card(modifier) {
        Column {
            CardHeader(
                title = {
                    Text(stringResource(R.string.noti_extractor_blocklist_title))
                },
                icon = {
                    Icon(Icons.Default.AppBlocking, null)
                }
            )
            Divider()
            LazyColumn {
                items(blocklist) { appInfo ->
                    AppInfoItem(
                        appInfo = appInfo,
                        trailing = {
                            TextButton(
                                onClick = { viewModel.removeFromBlocklist(appInfo.packageName) }
                            ) {
                                Text(stringResource(R.string.remove))
                            }
                        }
                    )
                }
                item {
                    ListItem(
                        modifier = Modifier.clickable { addDialogVisible = true },
                        text = { Text(stringResource(R.string.add)) },
                        icon = { Icon(Icons.Default.Add, null) }
                    )
                }
            }
        }

        if (addDialogVisible) {
            val allApps by viewModel.allApps.collectAsState()
            SimpleDialog(
                title = { Text(stringResource(R.string.noti_extractor_blocklist_add)) },
                onDismissRequest = { addDialogVisible = false },
                onItemSelected = { viewModel.addToBlocklist(it.packageName) },
                itemContent = { AppInfoItem(appInfo = it) },
                items = allApps
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun AppInfoItem(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    trailing: @Composable (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier,
        text = { Text(appInfo.name) },
        icon = {
            appInfo.icon?.let {
                Image(
                    modifier = Modifier.size(36.dp),
                    bitmap = appInfo.icon.asImageBitmap(),
                    contentDescription = null
                )
            }
        },
        trailing = trailing
    )
}
