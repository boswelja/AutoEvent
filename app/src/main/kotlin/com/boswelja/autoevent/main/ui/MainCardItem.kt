package com.boswelja.autoevent.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.boswelja.autoevent.common.ui.CardHeader

@Composable
fun MainCardItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(modifier) {
        Column {
            CardHeader(title = title)
            HorizontalDivider()
            content()
        }
    }
}
