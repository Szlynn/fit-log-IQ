package io.github.szlynn.fitlogiq.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val ITEM_HEIGHT_DP = 44
private const val VISIBLE_ITEMS = 5
private const val PADDING_ITEMS = VISIBLE_ITEMS / 2  // 2 padding items on each side

@Composable
fun DurationPickerDialog(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onConfirm: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    // initialFirstVisibleItemIndex points to the padding item before the real first item,
    // so we offset by PADDING_ITEMS to land on the correct value.
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hours)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minutes)
    val secondState = rememberLazyListState(initialFirstVisibleItemIndex = seconds)

    // The first visible item index includes the leading padding items, so subtract them.
    val selectedHour by remember {
        derivedStateOf { (hourState.firstVisibleItemIndex - PADDING_ITEMS).coerceAtLeast(0) }
    }
    val selectedMinute by remember {
        derivedStateOf { (minuteState.firstVisibleItemIndex - PADDING_ITEMS).coerceAtLeast(0) }
    }
    val selectedSecond by remember {
        derivedStateOf { (secondState.firstVisibleItemIndex - PADDING_ITEMS).coerceAtLeast(0) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour, selectedMinute, selectedSecond) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Duration") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PickerColumn(
                    state = hourState,
                    count = 24,
                    label = "h",
                    selectedIndex = selectedHour,
                )
                Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 4.dp))
                PickerColumn(
                    state = minuteState,
                    count = 60,
                    label = "min",
                    selectedIndex = selectedMinute,
                )
                Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 4.dp))
                PickerColumn(
                    state = secondState,
                    count = 60,
                    label = "sec",
                    selectedIndex = selectedSecond,
                )
            }
        },
    )
}

@Composable
private fun PickerColumn(
    state: androidx.compose.foundation.lazy.LazyListState,
    count: Int,
    label: String,
    selectedIndex: Int,
) {
    val itemHeight = ITEM_HEIGHT_DP.dp
    val listHeight = itemHeight * VISIBLE_ITEMS

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))

        Box {
            LazyColumn(
                state = state,
                modifier = Modifier
                    .height(listHeight)
                    .width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Leading padding so the first real value can be centered
                items(PADDING_ITEMS) {
                    Spacer(Modifier.height(itemHeight))
                }
                items(count) { index ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = index.toString().padStart(2, '0'),
                        modifier = Modifier
                            .height(itemHeight)
                            .width(48.dp)
                            .alpha(if (isSelected) 1f else 0.4f),
                        style = if (isSelected) {
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        textAlign = TextAlign.Center,
                    )
                }
                // Trailing padding so the last real value can be centered
                items(PADDING_ITEMS) {
                    Spacer(Modifier.height(itemHeight))
                }
            }

            // Selection indicator lines around the center item
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = itemHeight / 2 + 1.dp)
                    .width(48.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = itemHeight / 2 + 1.dp)
                    .width(48.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
