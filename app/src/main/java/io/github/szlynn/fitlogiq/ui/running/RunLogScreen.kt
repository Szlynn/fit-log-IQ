package io.github.szlynn.fitlogiq.ui.running

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.szlynn.fitlogiq.data.db.RunEntity
import io.github.szlynn.fitlogiq.data.model.RunType
import io.github.szlynn.fitlogiq.ui.navigation.Screen
import io.github.szlynn.fitlogiq.util.formatDuration
import io.github.szlynn.fitlogiq.util.formatPace
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunLogScreen(
    navController: NavController,
    viewModel: RunLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Run Log") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.LogRun.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Log run")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            // Filter bar
            val filters = RunLogFilter.entries
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                filters.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.onFilterChange(filter) },
                        shape = SegmentedButtonDefaults.itemShape(index, filters.size),
                        label = { Text(filter.label) },
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.runs.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No runs logged yet.\nTap + to add your first run.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.runs, key = { it.id }) { run ->
                            RunCard(
                                run = run,
                                onEditClick = {
                                    navController.navigate("log_run/${run.id}")
                                },
                                onDeleteClick = { viewModel.onDeleteRequest(run) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    uiState.pendingDeleteRun?.let { run ->
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismiss,
            title = { Text("Delete run?") },
            text = {
                val date = LocalDate.ofEpochDay(run.date).format(DATE_FORMATTER)
                Text("Delete the run from $date? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onDeleteConfirm,
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismiss) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun RunCard(
    run: RunEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type icon
            Icon(
                imageVector = run.type.icon,
                contentDescription = run.type.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.width(12.dp))

            // Main info
            Column(modifier = Modifier.weight(1f)) {
                val date = LocalDate.ofEpochDay(run.date).format(DATE_FORMATTER)
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "%.2f km".format(run.distanceKm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = formatDuration(run.durationSeconds),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = formatPace(run.durationSeconds / 60.0 / run.distanceKm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Sub-label for race name or training plan day
                val subLabel = when (run.type) {
                    RunType.RACE -> run.raceName
                    else -> null
                }
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Actions
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private val RunType.icon: ImageVector
    get() = when (this) {
        RunType.TRAINING_PLAN -> Icons.Default.DateRange
        RunType.RACE -> Icons.Default.DateRange  // placeholder – swap for trophy icon if available
        RunType.CASUAL -> Icons.Default.DateRange // placeholder – swap for shoe icon if available
    }

private val RunLogFilter.label: String
    get() = when (this) {
        RunLogFilter.ALL -> "All"
        RunLogFilter.TRAINING_PLAN -> "Training"
        RunLogFilter.RACES -> "Races"
    }
