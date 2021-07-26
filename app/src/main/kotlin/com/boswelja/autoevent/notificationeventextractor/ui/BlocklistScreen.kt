package com.boswelja.autoevent.notificationeventextractor.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.ui.CardHeader
import kotlinx.coroutines.Dispatchers

@ExperimentalMaterialApi
@Composable
fun BlocklistScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: BlocklistViewModel = viewModel()
    val blocklist by viewModel.blocklist.collectAsState(emptyList(), Dispatchers.IO)

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
            if (blocklist.isNotEmpty()) {
                LazyColumn {
                    items(blocklist) { appInfo ->
                        ListItem(
                            text = { Text(appInfo.name) },
                            icon = { Image(appInfo.icon.asImageBitmap(), null) },
                            trailing = {
                                TextButton(
                                    onClick = { viewModel.removeFromBlocklist(appInfo.packageName) }
                                ) {
                                    Text(stringResource(R.string.remove))
                                }
                            }
                        )
                    }
                }
            } else {
                ListItem(
                    text = { Text(stringResource(R.string.noti_extractor_blocklist_none)) }
                )
            }
        }
    }
}
