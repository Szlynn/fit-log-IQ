package io.github.szlynn.fitlogiq.ui.running

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.szlynn.fitlogiq.data.db.RunEntity
import io.github.szlynn.fitlogiq.data.db.TrainingPlanDayEntity
import io.github.szlynn.fitlogiq.data.model.DayStatus
import io.github.szlynn.fitlogiq.data.model.RunType
import io.github.szlynn.fitlogiq.data.repository.RunRepository
import io.github.szlynn.fitlogiq.data.repository.TrainingPlanRepository
import io.github.szlynn.fitlogiq.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LogRunUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val runType: RunType = RunType.TRAINING_PLAN,
    // Training plan fields
    val activePlanName: String? = null,
    val nextPendingDay: TrainingPlanDayEntity? = null,
    // Race fields
    val raceName: String = "",
    // Metrics
    val distanceKm: String = "",
    val durationHours: Int = 0,
    val durationMinutes: Int = 0,
    val durationSeconds: Int = 0,
    val note: String = "",
    // Validation
    val distanceError: Boolean = false,
    val durationError: Boolean = false,
    val raceNameError: Boolean = false,
)

sealed interface LogRunEvent {
    data object NavigateBack : LogRunEvent
    data class ShowError(val message: String) : LogRunEvent
}

@HiltViewModel
class LogRunViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val trainingPlanRepository: TrainingPlanRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editRunId: Long? =
        savedStateHandle.get<Long>(Screen.LogRunEdit.ARG)?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(LogRunUiState())
    val uiState: StateFlow<LogRunUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LogRunEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            if (editRunId != null) {
                loadExistingRun(editRunId)
            } else {
                loadDefaults()
            }
        }
    }

    private suspend fun loadDefaults() {
        val activePlan = trainingPlanRepository.getActivePlanOnce()
        val nextDay = trainingPlanRepository.getNextPendingDay()
        _uiState.update {
            it.copy(
                isLoading = false,
                isEditMode = false,
                runType = RunType.TRAINING_PLAN,
                activePlanName = activePlan?.name,
                nextPendingDay = nextDay,
                distanceKm = nextDay?.targetDistanceKm?.toString() ?: "",
            )
        }
    }

    private suspend fun loadExistingRun(runId: Long) {
        val run = runRepository.getRunById(runId)
        if (run == null) {
            _events.emit(LogRunEvent.ShowError("Run not found"))
            _events.emit(LogRunEvent.NavigateBack)
            return
        }
        val activePlan = trainingPlanRepository.getActivePlanOnce()
        val nextDay = if (run.type == RunType.TRAINING_PLAN && run.trainingPlanDayId != null) {
            trainingPlanRepository.getDayById(run.trainingPlanDayId)
        } else {
            trainingPlanRepository.getNextPendingDay()
        }
        val totalSeconds = run.durationSeconds
        _uiState.update {
            it.copy(
                isLoading = false,
                isEditMode = true,
                date = LocalDate.ofEpochDay(run.date),
                runType = run.type,
                activePlanName = activePlan?.name,
                nextPendingDay = nextDay,
                raceName = run.raceName ?: "",
                distanceKm = run.distanceKm.toString(),
                durationHours = (totalSeconds / 3600).toInt(),
                durationMinutes = ((totalSeconds % 3600) / 60).toInt(),
                durationSeconds = (totalSeconds % 60).toInt(),
                note = run.note ?: "",
            )
        }
    }

    fun onDateChange(date: LocalDate) = _uiState.update { it.copy(date = date) }

    fun onRunTypeChange(type: RunType) = _uiState.update { it.copy(runType = type) }

    fun onRaceNameChange(name: String) =
        _uiState.update { it.copy(raceName = name, raceNameError = false) }

    fun onDistanceChange(value: String) =
        _uiState.update { it.copy(distanceKm = value, distanceError = false) }

    fun onDurationChange(hours: Int, minutes: Int, seconds: Int) =
        _uiState.update {
            it.copy(
                durationHours = hours,
                durationMinutes = minutes,
                durationSeconds = seconds,
                durationError = false,
            )
        }

    fun onNoteChange(note: String) = _uiState.update { it.copy(note = note) }

    fun onSave() {
        val state = _uiState.value
        if (!validate(state)) return

        viewModelScope.launch {
            val distance = state.distanceKm.toDouble()
            val totalSeconds = state.durationHours * 3600L +
                state.durationMinutes * 60L +
                state.durationSeconds

            if (state.isEditMode && editRunId != null) {
                val existing = runRepository.getRunById(editRunId) ?: return@launch
                val oldDayId = existing.trainingPlanDayId
                val newDayId = if (state.runType == RunType.TRAINING_PLAN) {
                    state.nextPendingDay?.id
                } else null

                // If linked day changed, reset the old day
                if (oldDayId != null && oldDayId != newDayId) {
                    trainingPlanRepository.updateDayStatus(oldDayId, DayStatus.PENDING)
                }

                runRepository.updateRun(
                    existing.copy(
                        date = state.date.toEpochDay(),
                        type = state.runType,
                        distanceKm = distance,
                        durationSeconds = totalSeconds,
                        note = state.note.ifBlank { null },
                        trainingPlanDayId = newDayId,
                        raceName = if (state.runType == RunType.RACE) state.raceName else null,
                    ),
                )

                // Mark the new day as completed
                if (state.runType == RunType.TRAINING_PLAN && newDayId != null) {
                    trainingPlanRepository.updateDayStatus(newDayId, DayStatus.COMPLETED)
                }
            } else {
                val dayId = if (state.runType == RunType.TRAINING_PLAN) {
                    state.nextPendingDay?.id
                } else null

                runRepository.saveRun(
                    RunEntity(
                        date = state.date.toEpochDay(),
                        type = state.runType,
                        distanceKm = distance,
                        durationSeconds = totalSeconds,
                        note = state.note.ifBlank { null },
                        trainingPlanDayId = dayId,
                        raceName = if (state.runType == RunType.RACE) state.raceName else null,
                    ),
                )

                if (state.runType == RunType.TRAINING_PLAN && dayId != null) {
                    trainingPlanRepository.updateDayStatus(dayId, DayStatus.COMPLETED)
                }
            }

            _events.emit(LogRunEvent.NavigateBack)
        }
    }

    fun onSkipDay() {
        val state = _uiState.value
        val dayId = state.nextPendingDay?.id ?: return
        viewModelScope.launch {
            trainingPlanRepository.updateDayStatus(dayId, DayStatus.SKIPPED)
            _events.emit(LogRunEvent.NavigateBack)
        }
    }

    private fun validate(state: LogRunUiState): Boolean {
        val distanceValid = state.distanceKm.toDoubleOrNull()?.let { it > 0 } ?: false
        val totalSeconds = state.durationHours * 3600L +
            state.durationMinutes * 60L +
            state.durationSeconds
        val durationValid = totalSeconds > 0
        val raceNameValid = state.runType != RunType.RACE || state.raceName.isNotBlank()

        _uiState.update {
            it.copy(
                distanceError = !distanceValid,
                durationError = !durationValid,
                raceNameError = !raceNameValid,
            )
        }
        return distanceValid && durationValid && raceNameValid
    }
}
