package com.boswelja.autoevent.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.boswelja.autoevent.R
import java.util.concurrent.TimeUnit

@Composable
fun DurationPickerDialog(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    durationMillis: Long,
    onDurationChange: (Long) -> Unit,
    elevation: Dp = 24.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(backgroundColor),
    dialogProperties: DialogProperties = DialogProperties()
) {
    val hourAndMinute = remember(durationMillis) {
        getHourAndMinuteFromMillis(durationMillis)
    }
    var hours by remember(hourAndMinute) {
        mutableStateOf(hourAndMinute.first.toString())
    }
    var minutes by remember(hourAndMinute) {
        mutableStateOf(hourAndMinute.second.toString())
    }
    val saveEnabled by remember(hours, minutes) {
        val hoursValid = hours.toLongOrNull() != null
        val minutesValid = minutes.toLongOrNull() != null
        mutableStateOf(hoursValid && minutesValid)
    }
    MaterialDialog(
        modifier = modifier,
        elevation = elevation,
        shape = shape,
        contentColor = contentColor,
        backgroundColor = backgroundColor,
        dialogProperties = dialogProperties,
        onDismissRequest = onDismissRequest
    ) {
        Column {
            DialogHeader(title = title)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DurationInput(
                    modifier = Modifier.weight(1f),
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text(stringResource(R.string.duration_picker_hours)) }
                )
                DurationInput(
                    modifier = Modifier.weight(1f),
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = { Text(stringResource(R.string.duration_picker_minutes)) }
                )
            }
            DialogButtons(
                positiveButton = {
                    TextButton(
                        enabled = saveEnabled,
                        onClick = {
                            val newDuration = getMillisFromHoursAndMinutes(
                                hours.toLong(), minutes.toLong()
                            )
                            onDurationChange(newDuration)
                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                negativeButton = {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
internal fun DurationInput(
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = true
    )
}

fun getHourAndMinuteFromMillis(millis: Long): Pair<Long, Long> {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return Pair(hours, minutes)
}

private fun getMillisFromHoursAndMinutes(hours: Long, minutes: Long): Long {
    return TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes)
}
