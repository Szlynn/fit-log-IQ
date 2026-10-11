package io.github.szlynn.fitlogiq.ui.running

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.szlynn.fitlogiq.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanEditorScreen(
    navController: NavController,
    viewModel: TrainingPlanEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TrainingPlanEditorEvent.NavigateToDetail -> {
                    navController.navigate("training_plan/${event.planId}") {
                        popUpTo(Screen.TrainingPlans.route)
                    }
                }
                is TrainingPlanEditorEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Plan" else "New Plan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Plan name
            OutlinedTextField(
                value = uiState.planName,
                onValueChange = viewModel::onPlanNameChange,
                label = { Text("Plan name") },
                isError = uiState.planNameError,
                supportingText = if (uiState.planNameError) {
                    { Text("Plan name is required") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Weeks
            uiState.weeks.forEach { week ->
                WeekEditor(
                    week = week,
                    onDeleteWeek = { viewModel.onDeleteWeek(week.weekNumber) },
                    onAddDay = { viewModel.onAddDay(week.weekNumber) },
                    onDeleteDay = { dayOrder -> viewModel.onDeleteDay(week.weekNumber, dayOrder) },
                    onMoveDayUp = { dayOrder -> viewModel.onMoveDayUp(week.weekNumber, dayOrder) },
                    onMoveDayDown = { dayOrder -> viewModel.onMoveDayDown(week.weekNumber, dayOrder) },
                    onLabelChange = { dayOrder, label ->
                        viewModel.onDayLabelChange(week.weekNumber, dayOrder, label)
                    },
                    onDistanceChange = { dayOrder, dist ->
                        viewModel.onDayDistanceChange(week.weekNumber, dayOrder, dist)
                    },
                    onCharacterChange = { dayOrder, char ->
                        viewModel.onDayCharacterChange(week.weekNumber, dayOrder, char)
                    },
                )
            }

            OutlinedButton(
                onClick = viewModel::onAddWeek,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("+ Add week")
            }

            Button(
                onClick = viewModel::onSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isEditMode) "Save changes" else "Create plan")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WeekEditor(
    week: EditorWeek,
    onDeleteWeek: () -> Unit,
    onAddDay: () -> Unit,
    onDeleteDay: (Int) -> Unit,
    onMoveDayUp: (Int) -> Unit,
    onMoveDayDown: (Int) -> Unit,
    onLabelChange: (Int, String) -> Unit,
    onDistanceChange: (Int, String) -> Unit,
    onCharacterChange: (Int, String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Week ${week.weekNumber}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDeleteWeek) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete week",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        week.days.forEach { day ->
            DayEditor(
                day = day,
                isFirst = day.dayOrder == week.days.minOfOrNull { it.dayOrder },
                isLast = day.dayOrder == week.days.maxOfOrNull { it.dayOrder },
                onDelete = { onDeleteDay(day.dayOrder) },
                onMoveUp = { onMoveDayUp(day.dayOrder) },
                onMoveDown = { onMoveDayDown(day.dayOrder) },
                onLabelChange = { onLabelChange(day.dayOrder, it) },
                onDistanceChange = { onDistanceChange(day.dayOrder, it) },
                onCharacterChange = { onCharacterChange(day.dayOrder, it) },
            )
        }

        OutlinedButton(
            onClick = onAddDay,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text("+ Add day")
        }

        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun DayEditor(
    day: EditorDay,
    isFirst: Boolean,
    isLast: Boolean,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onLabelChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onCharacterChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Up/down arrows
        Column {
            IconButton(onClick = onMoveUp, enabled = !isFirst) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
            }
            IconButton(onClick = onMoveDown, enabled = !isLast) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
            }
        }

        // Fields
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = day.dayLabel,
                onValueChange = onLabelChange,
                label = { Text("Label") },
                isError = day.labelError,
                supportingText = if (day.labelError) { { Text("Required") } } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = day.targetDistanceKm,
                    onValueChange = onDistanceChange,
                    label = { Text("Distance (km)") },
                    isError = day.distanceError,
                    supportingText = if (day.distanceError) { { Text("Required") } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = day.character,
                    onValueChange = onCharacterChange,
                    label = { Text("Type (opt.)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
        }

        // Delete button
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete day",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
