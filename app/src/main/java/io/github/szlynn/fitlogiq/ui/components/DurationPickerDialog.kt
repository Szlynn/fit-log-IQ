package io.github.szlynn.fitlogiq.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val ITEM_HEIGHT_DP = 48
private const val VISIBLE_ITEMS = 5

@Composable
fun DurationPickerDialog(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onConfirm: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hours)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minutes)
    val secondState = rememberLazyListState(initialFirstVisibleItemIndex = seconds)

    val selectedHour by remember { derivedStateOf { hourState.firstVisibleItemIndex } }
    val selectedMinute by remember { derivedStateOf { minuteState.firstVisibleItemIndex } }
    val selectedSecond by remember { derivedStateOf { secondState.firstVisibleItemIndex } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour, selectedMinute, selectedSecond) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Mégse") }
        },
        title = { Text("Időtartam") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PickerColumn(
                        state = hourState,
                        count = 24,
                        label = "ó",
                        selectedIndex = selectedHour,
                    )
                    Spacer(Modifier.width(8.dp))
                    PickerColumn(
                        state = minuteState,
                        count = 60,
                        label = "p",
                        selectedIndex = selectedMinute,
                    )
                    Spacer(Modifier.width(8.dp))
                    PickerColumn(
                        state = secondState,
                        count = 60,
                        label = "mp",
                        selectedIndex = selectedSecond,
                    )
                }
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))

        val itemHeight = ITEM_HEIGHT_DP.dp
        val listHeight = itemHeight * VISIBLE_ITEMS

        androidx.compose.foundation.layout.Box {
            LazyColumn(
                state = state,
                modifier = Modifier
                    .height(listHeight)
                    .width(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Padding items so first and last values can be centered
                items(VISIBLE_ITEMS / 2) { Spacer(Modifier.height(itemHeight)) }
                items(count) { index ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = index.toString().padStart(2, '0'),
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .alpha(if (isSelected) 1f else 0.4f),
                        style = if (isSelected) {
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        textAlign = TextAlign.Center,
                    )
                }
                items(VISIBLE_ITEMS / 2) { Spacer(Modifier.height(itemHeight)) }
            }

            // Selection indicator lines
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = itemHeight / 2 + 1.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = itemHeight / 2 + 1.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
