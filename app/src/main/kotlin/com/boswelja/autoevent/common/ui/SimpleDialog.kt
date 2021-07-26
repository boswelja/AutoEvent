package com.boswelja.autoevent.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun <T> SimpleDialog(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    onItemSelected: (T) -> Unit,
    itemContent: @Composable (T) -> Unit,
    items: List<T>,
    elevation: Dp = 24.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.contentColorFor(backgroundColor),
    dialogProperties: DialogProperties = DialogProperties()
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dialogProperties
    ) {
        Surface(
            modifier = modifier.heightIn(max = 560.dp),
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor,
            elevation = elevation
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .padding(start = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.h6,
                        content = title
                    )
                }
                Divider()
                LazyColumn {
                    items(items) { item ->
                        Box(
                            Modifier.clickable {
                                onItemSelected(item)
                                onDismissRequest()
                            }
                        ) {
                            itemContent(item)
                        }
                    }
                }
            }
        }
    }
}
