package com.boswelja.autoevent.eventextractor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.ui.DialogPickerSetting
import com.boswelja.autoevent.common.ui.DurationPickerDialog
import com.boswelja.autoevent.common.ui.getHourAndMinuteFromMillis
import com.boswelja.autoevent.eventextractor.ExtractorSettings
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers

@ExperimentalMaterialApi
@Composable
fun ExtractorSettings(
    modifier: Modifier = Modifier
) {
    val viewModel: ExtractorSettingsViewModel = viewModel()
    val language by viewModel.extractorLanguage.collectAsState(
        initial = ExtractorSettings.ExtractorLanguage.DETECT,
        context = Dispatchers.IO
    )
    val extractEmails by viewModel.extractEmails.collectAsState(
        initial = false,
        context = Dispatchers.IO
    )
    val extractAddress by viewModel.extractAddress.collectAsState(
        initial = false,
        context = Dispatchers.IO
    )
    val ignoreAllDayEvents by viewModel.ignoreAllDayEvents.collectAsState(
        initial = false,
        context = Dispatchers.IO
    )
    val defaultDurationMillis by viewModel.defaultDuration.collectAsState(
        initial = TimeUnit.MINUTES.toMillis(30),
        context = Dispatchers.IO
    )
    Column(modifier) {
        DialogPickerSetting(
            items = ExtractorSettings.ExtractorLanguage.values(),
            selectedItem = language,
            onItemSelected = { viewModel.updateExtractLanguage(it) },
            itemContent = { Text(it.displayName()) },
            text = { Text(stringResource(R.string.extractor_language_title)) },
            secondaryText = { Text(language.displayName()) },
            icon = { Icon(Icons.Default.Language, null) }
        )
        ListItem(
            modifier = Modifier.clickable { viewModel.updateExtractEmails(!extractEmails) },
            text = { Text(stringResource(R.string.extract_emails_settings_title)) },
            icon = { Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null) },
            trailing = { Checkbox(onCheckedChange = null, checked = extractEmails) }
        )
        ListItem(
            modifier = Modifier.clickable { viewModel.updateExtractAddress(!extractAddress) },
            text = { Text(stringResource(R.string.extract_address_settings_title)) },
            icon = { Icon(imageVector = Icons.Default.AddLocation, contentDescription = null) },
            trailing = { Checkbox(onCheckedChange = null, checked = extractAddress) }
        )
        ListItem(
            modifier = Modifier.clickable { viewModel.updateAllDayEvents(!ignoreAllDayEvents) },
            text = { Text(stringResource(R.string.extractor_ignore_allday_title)) },
            secondaryText = { Text(stringResource(R.string.extractor_ignore_allday_summary)) },
            icon = { Icon(Icons.Default.ViewDay, null) },
            trailing = { Checkbox(onCheckedChange = null, checked = ignoreAllDayEvents) }
        )
        DefaultDurationSetting(
            durationMillis = defaultDurationMillis,
            onDurationChange = { viewModel.updateDefaultEventDuration(it) }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun DefaultDurationSetting(
    modifier: Modifier = Modifier,
    durationMillis: Long,
    onDurationChange: (Long) -> Unit
) {
    val context = LocalContext.current

    var dialogVisible by remember {
        mutableStateOf(false)
    }

    val displayText = remember(durationMillis) {
        val (hours, minutes) = getHourAndMinuteFromMillis(durationMillis)
        buildString {
            if (hours > 0) {
                append(context.resources.getQuantityString(R.plurals.hours, hours.toInt(), hours))
                append(" ")
            }
            if (minutes > 0) {
                append(
                    context.resources.getQuantityString(R.plurals.minutes, minutes.toInt(), minutes)
                )
            }
        }
    }

    ListItem(
        modifier = modifier.clickable { dialogVisible = true },
        icon = { Icon(Icons.Default.Timer, null) },
        text = { Text(stringResource(R.string.default_duration_title)) },
        secondaryText = { Text(displayText) }
    )

    if (dialogVisible) {
        DurationPickerDialog(
            title = { Text(stringResource(R.string.default_duration_title)) },
            onDismissRequest = { dialogVisible = false },
            durationMillis = durationMillis,
            onDurationChange = onDurationChange
        )
    }
}

@Composable
private fun ExtractorSettings.ExtractorLanguage.displayName(): String {
    return when (this) {
        ExtractorSettings.ExtractorLanguage.DETECT -> stringResource(
            R.string.language_detect,
            Locale.getDefault().displayLanguage
        )
        ExtractorSettings.ExtractorLanguage.ARABIC -> Locale("ara").displayLanguage
        ExtractorSettings.ExtractorLanguage.CHINESE -> Locale.CHINESE.displayLanguage
        ExtractorSettings.ExtractorLanguage.DUTCH -> Locale("nld").displayLanguage
        ExtractorSettings.ExtractorLanguage.ENGLISH -> Locale.ENGLISH.displayLanguage
        ExtractorSettings.ExtractorLanguage.FRENCH -> Locale.FRENCH.displayLanguage
        ExtractorSettings.ExtractorLanguage.GERMAN -> Locale.GERMAN.displayLanguage
        ExtractorSettings.ExtractorLanguage.ITALIAN -> Locale.ITALIAN.displayLanguage
        ExtractorSettings.ExtractorLanguage.JAPANESE -> Locale.JAPANESE.displayLanguage
        ExtractorSettings.ExtractorLanguage.KOREAN -> Locale.KOREAN.displayLanguage
        ExtractorSettings.ExtractorLanguage.POLISH -> Locale("pol").displayLanguage
        ExtractorSettings.ExtractorLanguage.PORTUGUESE -> Locale("por").displayLanguage
        ExtractorSettings.ExtractorLanguage.RUSSIAN -> Locale("rus").displayLanguage
        ExtractorSettings.ExtractorLanguage.SPANISH -> Locale("spa").displayLanguage
        ExtractorSettings.ExtractorLanguage.THAI -> Locale("tha").displayLanguage
        ExtractorSettings.ExtractorLanguage.TURKISH -> Locale("tur").displayLanguage
    }
}
