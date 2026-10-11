package io.github.szlynn.fitlogiq.ui.running

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.szlynn.fitlogiq.data.db.TrainingPlanDayEntity
import io.github.szlynn.fitlogiq.data.db.TrainingPlanEntity
import io.github.szlynn.fitlogiq.data.db.TrainingPlanWeekEntity
import io.github.szlynn.fitlogiq.data.repository.TrainingPlanRepository
import io.github.szlynn.fitlogiq.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Mutable editor state for a single day within the editor. */
data class EditorDay(
    /** Null for new days not yet saved. */
    val id: Long? = null,
    val dayOrder: Int,
    val dayLabel: String = "",
    val targetDistanceKm: String = "",
    val character: String = "",
    // Validation
    val labelError: Boolean = false,
    val distanceError: Boolean = false,
)

/** Mutable editor state for a single week within the editor. */
data class EditorWeek(
    val id: Long? = null,
    val weekNumber: Int,
    val days: List<EditorDay> = emptyList(),
)

data class TrainingPlanEditorUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val planName: String = "",
    val planNameError: Boolean = false,
    val weeks: List<EditorWeek> = emptyList(),
)

sealed interface TrainingPlanEditorEvent {
    data class NavigateToDetail(val planId: Long) : TrainingPlanEditorEvent
    data object NavigateBack : TrainingPlanEditorEvent
}

@HiltViewModel
class TrainingPlanEditorViewModel @Inject constructor(
    private val repository: TrainingPlanRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editPlanId: Long? =
        savedStateHandle.get<String>(Screen.TrainingPlanEditor.ARG)?.toLongOrNull()

    private val _uiState = MutableStateFlow(TrainingPlanEditorUiState())
    val uiState: StateFlow<TrainingPlanEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TrainingPlanEditorEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            if (editPlanId != null) {
                loadExistingPlan(editPlanId)
            } else {
                _uiState.update { it.copy(isLoading = false, isEditMode = false) }
            }
        }
    }

    private suspend fun loadExistingPlan(planId: Long) {
        val planWithWeeks = repository.getPlanWithWeeksAndDays(planId)
        // Collect just the first emission for initial load
        planWithWeeks.collect { data ->
            if (data == null) {
                _events.emit(TrainingPlanEditorEvent.NavigateBack)
                return@collect
            }
            val editorWeeks = data.sortedWeeks.map { weekWithDays ->
                EditorWeek(
                    id = weekWithDays.week.id,
                    weekNumber = weekWithDays.week.weekNumber,
                    days = weekWithDays.sortedDays.map { day ->
                        EditorDay(
                            id = day.id,
                            dayOrder = day.dayOrder,
                            dayLabel = day.dayLabel,
                            targetDistanceKm = day.targetDistanceKm.toString(),
                            character = day.character ?: "",
                        )
                    },
                )
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isEditMode = true,
                    planName = data.plan.name,
                    weeks = editorWeeks,
                )
            }
            return@collect  // only need the first emission
        }
    }

    fun onPlanNameChange(name: String) =
        _uiState.update { it.copy(planName = name, planNameError = false) }

    fun onAddWeek() {
        _uiState.update { state ->
            val nextNumber = (state.weeks.maxOfOrNull { it.weekNumber } ?: 0) + 1
            state.copy(weeks = state.weeks + EditorWeek(weekNumber = nextNumber))
        }
    }

    fun onDeleteWeek(weekNumber: Int) {
        _uiState.update { state ->
            state.copy(weeks = state.weeks.filter { it.weekNumber != weekNumber })
        }
    }

    fun onAddDay(weekNumber: Int) {
        _uiState.update { state ->
            state.copy(
                weeks = state.weeks.map { week ->
                    if (week.weekNumber != weekNumber) return@map week
                    val nextOrder = (week.days.maxOfOrNull { it.dayOrder } ?: 0) + 1
                    week.copy(days = week.days + EditorDay(dayOrder = nextOrder))
                },
            )
        }
    }

    fun onDeleteDay(weekNumber: Int, dayOrder: Int) {
        _uiState.update { state ->
            state.copy(
                weeks = state.weeks.map { week ->
                    if (week.weekNumber != weekNumber) return@map week
                    week.copy(days = week.days.filter { it.dayOrder != dayOrder })
                },
            )
        }
    }

    fun onMoveDayUp(weekNumber: Int, dayOrder: Int) = swapDays(weekNumber, dayOrder, dayOrder - 1)

    fun onMoveDayDown(weekNumber: Int, dayOrder: Int) = swapDays(weekNumber, dayOrder, dayOrder + 1)

    private fun swapDays(weekNumber: Int, fromOrder: Int, toOrder: Int) {
        _uiState.update { state ->
            state.copy(
                weeks = state.weeks.map { week ->
                    if (week.weekNumber != weekNumber) return@map week
                    val days = week.days.toMutableList()
                    val fromIdx = days.indexOfFirst { it.dayOrder == fromOrder }
                    val toIdx = days.indexOfFirst { it.dayOrder == toOrder }
                    if (fromIdx < 0 || toIdx < 0) return@map week
                    // Swap dayOrder values, keep everything else
                    val updatedFrom = days[fromIdx].copy(dayOrder = toOrder)
                    val updatedTo = days[toIdx].copy(dayOrder = fromOrder)
                    days[fromIdx] = updatedFrom
                    days[toIdx] = updatedTo
                    week.copy(days = days.sortedBy { it.dayOrder })
                },
            )
        }
    }

    fun onDayLabelChange(weekNumber: Int, dayOrder: Int, label: String) =
        updateDay(weekNumber, dayOrder) { it.copy(dayLabel = label, labelError = false) }

    fun onDayDistanceChange(weekNumber: Int, dayOrder: Int, distance: String) =
        updateDay(weekNumber, dayOrder) { it.copy(targetDistanceKm = distance, distanceError = false) }

    fun onDayCharacterChange(weekNumber: Int, dayOrder: Int, character: String) =
        updateDay(weekNumber, dayOrder) { it.copy(character = character) }

    private fun updateDay(weekNumber: Int, dayOrder: Int, transform: (EditorDay) -> EditorDay) {
        _uiState.update { state ->
            state.copy(
                weeks = state.weeks.map { week ->
                    if (week.weekNumber != weekNumber) return@map week
                    week.copy(
                        days = week.days.map { day ->
                            if (day.dayOrder != dayOrder) day else transform(day)
                        },
                    )
                },
            )
        }
    }

    fun onSave() {
        if (!validate()) return
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isEditMode && editPlanId != null) {
                saveEdits(editPlanId, state)
            } else {
                val newPlanId = saveNewPlan(state)
                _events.emit(TrainingPlanEditorEvent.NavigateToDetail(newPlanId))
            }
        }
    }

    private suspend fun saveNewPlan(state: TrainingPlanEditorUiState): Long {
        val planId = repository.savePlan(TrainingPlanEntity(name = state.planName))
        saveWeeksAndDays(planId, state.weeks, existingWeekIds = emptyMap())
        return planId
    }

    private suspend fun saveEdits(planId: Long, state: TrainingPlanEditorUiState) {
        // Update plan entity
        val planEntity = TrainingPlanEntity(id = planId, name = state.planName)
        repository.updatePlan(planEntity)

        // Build a map of existing week ids
        val existingWeekIds = state.weeks
            .filter { it.id != null }
            .associate { it.weekNumber to it.id!! }

        saveWeeksAndDays(planId, state.weeks, existingWeekIds)
        _events.emit(TrainingPlanEditorEvent.NavigateToDetail(planId))
    }

    private suspend fun saveWeeksAndDays(
        planId: Long,
        weeks: List<EditorWeek>,
        existingWeekIds: Map<Int, Long>,
    ) {
        weeks.forEach { editorWeek ->
            val weekId = if (editorWeek.id != null) {
                repository.updateWeek(
                    TrainingPlanWeekEntity(
                        id = editorWeek.id,
                        planId = planId,
                        weekNumber = editorWeek.weekNumber,
                    ),
                )
                editorWeek.id
            } else {
                repository.saveWeek(
                    TrainingPlanWeekEntity(planId = planId, weekNumber = editorWeek.weekNumber),
                )
            }

            editorWeek.days.forEach { editorDay ->
                val distance = editorDay.targetDistanceKm.toDoubleOrNull() ?: 0.0
                val dayEntity = TrainingPlanDayEntity(
                    id = editorDay.id ?: 0L,
                    weekId = weekId,
                    dayOrder = editorDay.dayOrder,
                    dayLabel = editorDay.dayLabel,
                    targetDistanceKm = distance,
                    character = editorDay.character.ifBlank { null },
                )
                if (editorDay.id != null) {
                    repository.updateDay(dayEntity)
                } else {
                    repository.saveDay(dayEntity)
                }
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        val planNameValid = state.planName.isNotBlank()
        var allDaysValid = true

        val validatedWeeks = state.weeks.map { week ->
            week.copy(
                days = week.days.map { day ->
                    val labelValid = day.dayLabel.isNotBlank()
                    val distanceValid = day.targetDistanceKm.toDoubleOrNull()?.let { it > 0 } ?: false
                    if (!labelValid || !distanceValid) allDaysValid = false
                    day.copy(labelError = !labelValid, distanceError = !distanceValid)
                },
            )
        }

        _uiState.update {
            it.copy(
                planNameError = !planNameValid,
                weeks = validatedWeeks,
            )
        }
        return planNameValid && allDaysValid
    }
}
