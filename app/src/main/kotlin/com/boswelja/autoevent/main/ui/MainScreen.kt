package com.boswelja.autoevent.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        NotiExtractorSettings()
        ExtractorSettings()
    }
}
