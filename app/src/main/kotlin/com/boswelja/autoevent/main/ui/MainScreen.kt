package com.boswelja.autoevent.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.autoevent.R
import com.boswelja.autoevent.about.ui.AboutApp
import com.boswelja.autoevent.eventextractor.ui.ExtractorSettings
import com.boswelja.autoevent.notificationeventextractor.ui.NotiExtractorSettings

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    onNavigate: (Destinations) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                SectionHeader(stringResource(R.string.noti_extractor_settings_title))
                NotiExtractorSettings(onNavigate = onNavigate)
            }
        }
        item {
            Column {
                SectionHeader(stringResource(R.string.extractor_settings_title))
                ExtractorSettings()
            }
        }
        item {
            Column {
                SectionHeader(stringResource(R.string.about_app_title))
                AboutApp()
            }
        }
    }
}

@Composable
internal fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).then(modifier)
    )
}
