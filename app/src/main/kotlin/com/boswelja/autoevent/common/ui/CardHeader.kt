package com.boswelja.autoevent.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardHeader(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null
) {
    Row(modifier = modifier.padding(16.dp)) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(16.dp))
        }
        Column {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleMedium
            ) {
                title()
            }
            if (subtitle != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleSmall
                ) {
                    subtitle()
                }
            }
        }
    }
}
