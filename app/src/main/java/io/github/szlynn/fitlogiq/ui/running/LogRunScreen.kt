package io.github.szlynn.fitlogiq.ui.running

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.szlynn.fitlogiq.data.model.RunType
import io.github.szlynn.fitlogiq.ui.components.DurationPickerDialog
import io.github.szlynn.fitlogiq.util.formatDuration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogRunScreen(
    navController: NavController,
    viewModel: LogRunViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LogRunEvent.NavigateBack -> navController.popBackStack()
                is LogRunEvent.ShowError -> { /* TODO: show snackbar */ }
            }
        }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDurationPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Run" else "Log Run") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Date picker field
            OutlinedTextField(
                value = uiState.date.format(DATE_FORMATTER),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = false,
            )

            // Run type selector
            val runTypes = RunType.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                runTypes.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.runType == type,
                        onClick = { viewModel.onRunTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index, runTypes.size),
                        label = { Text(type.label) },
                    )
                }
            }

            // Dynamic section based on run type
            when (uiState.runType) {
                RunType.TRAINING_PLAN -> {
                    TrainingPlanSection(uiState)
                }
                RunType.RACE -> {
                    OutlinedTextField(
                        value = uiState.raceName,
                        onValueChange = viewModel::onRaceNameChange,
                        label = { Text("Race name") },
                        isError = uiState.raceNameError,
                        supportingText = if (uiState.raceNameError) {
                            { Text("Race name is required") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                RunType.CASUAL -> { /* no extra fields */ }
            }

            // Distance
            OutlinedTextField(
                value = uiState.distanceKm,
                onValueChange = viewModel::onDistanceChange,
                label = { Text("Distance (km)") },
                isError = uiState.distanceError,
                supportingText = if (uiState.distanceError) {
                    { Text("Enter a valid distance") }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Duration
            val durationText = remember(uiState.durationHours, uiState.durationMinutes, uiState.durationSeconds) {
                val total = uiState.durationHours * 3600L +
                    uiState.durationMinutes * 60L +
                    uiState.durationSeconds
                if (total == 0L) "" else formatDuration(total)
            }
            OutlinedTextField(
                value = durationText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Duration") },
                isError = uiState.durationError,
                supportingText = if (uiState.durationError) {
                    { Text("Enter a valid duration") }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDurationPicker = true },
                enabled = false,
                placeholder = { Text("0:00:00") },
            )

            // Note
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )

            Spacer(Modifier.height(8.dp))

            // Skip Day button (training plan only)
            if (uiState.runType == RunType.TRAINING_PLAN && uiState.nextPendingDay != null) {
                OutlinedButton(
                    onClick = viewModel::onSkipDay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Skip this day")
                }
            }

            // Save button
            Button(
                onClick = viewModel::onSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isEditMode) "Save changes" else "Save run")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Duration picker dialog
    if (showDurationPicker) {
        DurationPickerDialog(
            hours = uiState.durationHours,
            minutes = uiState.durationMinutes,
            seconds = uiState.durationSeconds,
            onConfirm = { h, m, s ->
                viewModel.onDurationChange(h, m, s)
                showDurationPicker = false
            },
            onDismiss = { showDurationPicker = false },
        )
    }
}

@Composable
private fun TrainingPlanSection(uiState: LogRunUiState) {
    if (uiState.activePlanName == null) {
        Text(
            text = "No active training plan. Create one in Training Plans.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Training plan",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = uiState.activePlanName,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (uiState.nextPendingDay != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Next: ${uiState.nextPendingDay.dayLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                uiState.nextPendingDay.character?.let { char ->
                    Text(
                        text = "· $char",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Text(
                text = "All days completed!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val RunType.label: String
    get() = when (this) {
        RunType.TRAINING_PLAN -> "Training Plan"
        RunType.RACE -> "Race"
        RunType.CASUAL -> "Casual"
    }
