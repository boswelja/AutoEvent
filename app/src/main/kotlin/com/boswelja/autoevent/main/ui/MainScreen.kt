package com.boswelja.autoevent.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.autoevent.R
import com.boswelja.autoevent.eventextractor.ui.ExtractorSettings
import com.boswelja.autoevent.notificationeventextractor.ui.NotiExtractorSettings

@ExperimentalMaterialApi
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NotiExtractorCard()
        ExtractorSettingsCard()
    }
}

@ExperimentalMaterialApi
@Composable
fun NotiExtractorCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    MainCardItem(
        modifier = modifier,
        title = { Text(stringResource(R.string.noti_extractor_settings_title)) }
    ) {
        NotiExtractorSettings(
            modifier = contentModifier
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun ExtractorSettingsCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    MainCardItem(
        modifier = modifier,
        title = { Text(stringResource(R.string.extractor_settings_title)) }
    ) {
        ExtractorSettings(
            modifier = contentModifier
        )
    }
}
