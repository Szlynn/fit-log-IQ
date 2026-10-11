package io.github.szlynn.fitlogiq.ui.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.szlynn.fitlogiq.data.db.PlanWithWeeksAndDays
import io.github.szlynn.fitlogiq.data.db.TrainingPlanEntity
import io.github.szlynn.fitlogiq.data.repository.TrainingPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingPlansUiState(
    val plans: List<PlanWithWeeksAndDays> = emptyList(),
    val isLoading: Boolean = true,
    val pendingDeletePlan: TrainingPlanEntity? = null,
)

@HiltViewModel
class TrainingPlansViewModel @Inject constructor(
    private val repository: TrainingPlanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingPlansUiState())
    val uiState: StateFlow<TrainingPlansUiState> = _uiState.asStateFlow()

    init {
        repository.getAllPlansWithWeeksAndDays()
            .onEach { plans -> _uiState.update { it.copy(plans = plans, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun onDeleteRequest(plan: TrainingPlanEntity) {
        _uiState.update { it.copy(pendingDeletePlan = plan) }
    }

    fun onDeleteConfirm() {
        val plan = _uiState.value.pendingDeletePlan ?: return
        _uiState.update { it.copy(pendingDeletePlan = null) }
        viewModelScope.launch { repository.deletePlan(plan) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(pendingDeletePlan = null) }
    }

    fun onSetActive(planId: Long) {
        viewModelScope.launch { repository.setActivePlan(planId) }
    }
}
