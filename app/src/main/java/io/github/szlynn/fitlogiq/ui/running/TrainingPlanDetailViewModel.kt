package io.github.szlynn.fitlogiq.ui.running

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.szlynn.fitlogiq.data.db.PlanWithWeeksAndDays
import io.github.szlynn.fitlogiq.data.db.TrainingPlanEntity
import io.github.szlynn.fitlogiq.data.repository.TrainingPlanRepository
import io.github.szlynn.fitlogiq.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingPlanDetailUiState(
    val plan: PlanWithWeeksAndDays? = null,
    val isLoading: Boolean = true,
    val pendingRestartWeekId: Long? = null,
)

sealed interface TrainingPlanDetailEvent {
    data object NavigateBack : TrainingPlanDetailEvent
}

@HiltViewModel
class TrainingPlanDetailViewModel @Inject constructor(
    private val repository: TrainingPlanRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val planId: Long = requireNotNull(savedStateHandle[Screen.TrainingPlanDetail.ARG]) {
        "planId argument is required"
    }

    private val _uiState = MutableStateFlow(TrainingPlanDetailUiState())
    val uiState: StateFlow<TrainingPlanDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TrainingPlanDetailEvent>()
    val events = _events.asSharedFlow()

    init {
        repository.getPlanWithWeeksAndDays(planId)
            .onEach { plan ->
                if (plan == null) {
                    _events.emit(TrainingPlanDetailEvent.NavigateBack)
                } else {
                    _uiState.update { it.copy(plan = plan, isLoading = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onRestartWeekRequest(weekId: Long) {
        _uiState.update { it.copy(pendingRestartWeekId = weekId) }
    }

    fun onRestartWeekConfirm() {
        val weekId = _uiState.value.pendingRestartWeekId ?: return
        _uiState.update { it.copy(pendingRestartWeekId = null) }
        viewModelScope.launch { repository.restartWeek(weekId) }
    }

    fun onRestartWeekDismiss() {
        _uiState.update { it.copy(pendingRestartWeekId = null) }
    }

    fun onSetActive() {
        viewModelScope.launch { repository.setActivePlan(planId) }
    }
}
