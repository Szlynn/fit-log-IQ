package io.github.szlynn.fitlogiq.ui.running

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.szlynn.fitlogiq.data.db.PlanWithWeeksAndDays
import io.github.szlynn.fitlogiq.data.model.DayStatus
import io.github.szlynn.fitlogiq.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlansScreen(
    navController: NavController,
    viewModel: TrainingPlansViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Training Plans") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.TrainingPlanEditor.ROUTE.replace("{planId}", "new")) },
            ) {
                Icon(Icons.Default.Add, contentDescription = "New plan")
            }
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
            uiState.plans.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No training plans yet.\nTap + to create one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.plans, key = { it.plan.id }) { planWithWeeks ->
                        PlanCard(
                            planWithWeeks = planWithWeeks,
                            onCardClick = {
                                navController.navigate("training_plan/${planWithWeeks.plan.id}")
                            },
                            onSetActiveClick = { viewModel.onSetActive(planWithWeeks.plan.id) },
                            onDeleteClick = { viewModel.onDeleteRequest(planWithWeeks.plan) },
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    uiState.pendingDeletePlan?.let { plan ->
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismiss,
            title = { Text("Delete plan?") },
            text = { Text("Delete \"${plan.name}\"? All weeks and days will be removed. Logged runs are not deleted.") },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirm) {
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
private fun PlanCard(
    planWithWeeks: PlanWithWeeksAndDays,
    onCardClick: () -> Unit,
    onSetActiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val plan = planWithWeeks.plan
    val totalDays = planWithWeeks.weeksWithDays.sumOf { it.days.size }
    val completedDays = planWithWeeks.weeksWithDays.sumOf { week ->
        week.days.count { it.status == DayStatus.COMPLETED }
    }

    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (plan.isActive) {
                        Badge { Text("Active") }
                    }
                }
                Text(
                    text = "${planWithWeeks.weeksWithDays.size} weeks · $completedDays/$totalDays days completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!plan.isActive) {
                OutlinedButton(
                    onClick = onSetActiveClick,
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    Text("Set active")
                }
            }

            Spacer(Modifier.width(4.dp))

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete plan",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
