package com.boswelja.autoevent.notificationeventextractor.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.boswelja.autoevent.common.ui.SimpleDialog
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlocklistScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: BlocklistViewModel = viewModel()
    val blocklist by viewModel.blocklist.collectAsState(emptyList(), Dispatchers.IO)

    var addDialogVisible by remember {
        mutableStateOf(false)
    }
    Column(modifier) {
        LazyColumn {
            stickyHeader {
                Text(stringResource(R.string.noti_extractor_blocklist_title))
            }
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
                    headlineContent = { Text(stringResource(R.string.add)) },
                    leadingContent = { Icon(Icons.Default.Add, null) }
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

@Composable
fun AppInfoItem(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    trailing: @Composable (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(appInfo.name) },
        leadingContent = {
            appInfo.icon?.let {
                Image(
                    modifier = Modifier.size(36.dp),
                    bitmap = appInfo.icon.asImageBitmap(),
                    contentDescription = null
                )
            }
        },
        trailingContent = trailing
    )
}
