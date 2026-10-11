package io.github.szlynn.fitlogiq.ui.running

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.szlynn.fitlogiq.data.db.TrainingPlanDayEntity
import io.github.szlynn.fitlogiq.data.db.WeekWithDays
import io.github.szlynn.fitlogiq.data.model.DayStatus
import io.github.szlynn.fitlogiq.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanDetailScreen(
    navController: NavController,
    viewModel: TrainingPlanDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrainingPlanDetailEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    val plan = uiState.plan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan?.plan?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (plan != null) {
                        IconButton(onClick = {
                            navController.navigate("training_plan_editor/${plan.plan.id}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit plan")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            plan != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(plan.sortedWeeks, key = { it.week.id }) { weekWithDays ->
                        WeekRow(
                            weekWithDays = weekWithDays,
                            onRestartWeek = { viewModel.onRestartWeekRequest(weekWithDays.week.id) },
                        )
                    }
                }
            }
        }
    }

    // Restart week confirmation dialog
    if (uiState.pendingRestartWeekId != null) {
        val weekNumber = uiState.plan?.sortedWeeks
            ?.firstOrNull { it.week.id == uiState.pendingRestartWeekId }
            ?.week?.weekNumber
        AlertDialog(
            onDismissRequest = viewModel::onRestartWeekDismiss,
            title = { Text("Restart week?") },
            text = {
                Text("Reset all days of Week $weekNumber to Pending? Logged runs are not deleted.")
            },
            confirmButton = {
                TextButton(onClick = viewModel::onRestartWeekConfirm) { Text("Restart") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onRestartWeekDismiss) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun WeekRow(
    weekWithDays: WeekWithDays,
    onRestartWeek: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Week ${weekWithDays.week.weekNumber}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Week options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Restart week") },
                        onClick = {
                            menuExpanded = false
                            onRestartWeek()
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Day cells row
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            weekWithDays.sortedDays.forEach { day ->
                DayCell(day = day, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DayCell(
    day: TrainingPlanDayEntity,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (day.status) {
        DayStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        DayStatus.SKIPPED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DayStatus.PENDING -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            textDecoration = if (day.status == DayStatus.SKIPPED) TextDecoration.LineThrough else null,
            maxLines = 1,
        )
        Text(
            text = "%.1f km".format(day.targetDistanceKm),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
        )
    }
}
