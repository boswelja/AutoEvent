package com.boswelja.autoevent.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@ExperimentalMaterialApi
@Composable
fun <T> DialogPickerSetting(
    modifier: Modifier = Modifier,
    dialogModifier: Modifier = Modifier,
    items: Array<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemContent: @Composable RowScope.(item: T) -> Unit,
    text: @Composable () -> Unit,
    secondaryText: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null
) {
    var dialogVisible by remember {
        mutableStateOf(false)
    }

    if (dialogVisible) {
        ConfirmationDialog(
            modifier = dialogModifier,
            title = text,
            onDismissRequest = { dialogVisible = false },
            items = items,
            selectedItem = selectedItem,
            onItemSelectionChanged = onItemSelected,
            itemContent = itemContent
        )
    }

    ListItem(
        modifier = modifier.clickable { dialogVisible = true },
        text = text,
        secondaryText = secondaryText,
        icon = icon
    )
}
