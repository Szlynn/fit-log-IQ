package io.github.szlynn.fitlogiq.ui.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.szlynn.fitlogiq.data.db.RunEntity
import io.github.szlynn.fitlogiq.data.model.DayStatus
import io.github.szlynn.fitlogiq.data.model.RunType
import io.github.szlynn.fitlogiq.data.repository.RunRepository
import io.github.szlynn.fitlogiq.data.repository.TrainingPlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RunLogFilter { ALL, TRAINING_PLAN, RACES }

data class RunLogUiState(
    val runs: List<RunEntity> = emptyList(),
    val filter: RunLogFilter = RunLogFilter.ALL,
    val isLoading: Boolean = true,
    /** Set when the user taps delete; cleared after the dialog is dismissed. */
    val pendingDeleteRun: RunEntity? = null,
)

@HiltViewModel
class RunLogViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val trainingPlanRepository: TrainingPlanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunLogUiState())
    val uiState: StateFlow<RunLogUiState> = _uiState.asStateFlow()

    init {
        observeRuns()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRuns() {
        _uiState
            .flatMapLatest { state ->
                when (state.filter) {
                    RunLogFilter.ALL -> runRepository.getAllRuns()
                    RunLogFilter.TRAINING_PLAN -> runRepository.getRunsByType(RunType.TRAINING_PLAN)
                    RunLogFilter.RACES -> runRepository.getRunsByType(RunType.RACE)
                }
            }
            .onEach { runs ->
                _uiState.update { it.copy(runs = runs, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onFilterChange(filter: RunLogFilter) {
        _uiState.update { it.copy(filter = filter, isLoading = true) }
    }

    fun onDeleteRequest(run: RunEntity) {
        _uiState.update { it.copy(pendingDeleteRun = run) }
    }

    fun onDeleteConfirm() {
        val run = _uiState.value.pendingDeleteRun ?: return
        _uiState.update { it.copy(pendingDeleteRun = null) }
        viewModelScope.launch {
            // If the run was linked to a training plan day, reset that day to PENDING
            if (run.trainingPlanDayId != null) {
                trainingPlanRepository.updateDayStatus(run.trainingPlanDayId, DayStatus.PENDING)
            }
            runRepository.deleteRun(run)
        }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(pendingDeleteRun = null) }
    }
}
