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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.autoevent.R
import com.boswelja.autoevent.common.ui.DialogPickerSetting
import com.boswelja.autoevent.eventextractor.ExtractorSettings
import kotlinx.coroutines.Dispatchers
import java.util.Locale

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
