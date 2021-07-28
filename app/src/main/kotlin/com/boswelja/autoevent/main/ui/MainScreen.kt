package com.boswelja.autoevent.main.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.autoevent.R
import com.boswelja.autoevent.eventextractor.ui.ExtractorSettings
import com.boswelja.autoevent.notificationeventextractor.ui.NotiExtractorSettings
import com.boswelja.autoevent.support.ui.SupportOptions

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Destinations) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(state = scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NotiExtractorCard(
            onNavigate = onNavigate
        )
        ExtractorSettingsCard()
        SupportAppCard()
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun NotiExtractorCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    onNavigate: (Destinations) -> Unit
) {
    MainCardItem(
        modifier = modifier,
        title = { Text(stringResource(R.string.noti_extractor_settings_title)) }
    ) {
        NotiExtractorSettings(
            modifier = contentModifier,
            onNavigate = onNavigate
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

@ExperimentalMaterialApi
@Composable
fun SupportAppCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    MainCardItem(
        modifier = modifier,
        title = { Text(stringResource(R.string.support_app_title)) }
    ) {
        SupportOptions(modifier = contentModifier)
    }
}
