package com.boswelja.autoevent.about.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Source
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.autoevent.R

@Composable
fun AboutApp(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        SupportOptions()
        InfoOptions()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SupportOptions(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier) {
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.boswelja.autoevent")
                )
                context.startActivity(intent)
            },
            text = { Text(stringResource(R.string.rate_app)) },
            icon = { Icon(Icons.Default.RateReview, null) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InfoOptions(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier) {
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/boswelja/AutoEvent")
                )
                context.startActivity(intent)
            },
            text = { Text(stringResource(R.string.source_code)) },
            icon = { Icon(Icons.Default.Source, null) }
        )
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/boswelja/AutoEvent/blob/main/PRIVACY.md")
                )
                context.startActivity(intent)
            },
            text = { Text(stringResource(R.string.privacy_policy)) },
            icon = { Icon(Icons.Default.PrivacyTip, null) }
        )
    }
}
