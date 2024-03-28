package com.boswelja.autoevent.about.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
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
            headlineContent = { Text(stringResource(R.string.rate_app)) },
            leadingContent = { Icon(Icons.Default.RateReview, null) }
        )
    }
}

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
            headlineContent = { Text(stringResource(R.string.source_code)) },
            leadingContent = { Icon(Icons.Default.Source, null) }
        )
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/boswelja/AutoEvent/blob/main/PRIVACY.md")
                )
                context.startActivity(intent)
            },
            headlineContent = { Text(stringResource(R.string.privacy_policy)) },
            leadingContent = { Icon(Icons.Default.PrivacyTip, null) }
        )
    }
}
