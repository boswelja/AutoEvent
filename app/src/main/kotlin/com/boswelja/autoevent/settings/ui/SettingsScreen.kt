package com.boswelja.autoevent.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.boswelja.autoevent.eventextractor.ui.ExtractorSettings
import com.boswelja.autoevent.notificationeventextractor.ui.NotiExtractorSettings

@ExperimentalMaterialApi
@Preview
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        NotiExtractorSettings()
        Divider()
        ExtractorSettings()
    }
}
