package io.github.szlynn.fitlogiq.ui.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.szlynn.fitlogiq.data.db.RunEntity
import io.github.szlynn.fitlogiq.data.model.RunType
import io.github.szlynn.fitlogiq.data.repository.RunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val races: List<RunEntity> = emptyList(),
    val totalKmThisMonth: Double = 0.0,
    val totalKmThisYear: Double = 0.0,
    val fastestPaceMinPerKm: Double? = null,
    val longestRunKm: Double? = null,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val runRepository: RunRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        // Observe race list reactively
        runRepository.getRunsByType(RunType.RACE)
            .onEach { races -> _uiState.update { it.copy(races = races) } }
            .launchIn(viewModelScope)

        // Load aggregated stats once; reload when the run list changes
        runRepository.getAllRuns()
            .onEach { loadStats() }
            .launchIn(viewModelScope)
    }

    private suspend fun loadStats() {
        val today = LocalDate.now()

        val monthStart = today.withDayOfMonth(1).toEpochDay()
        val monthEnd = today.toEpochDay()

        val yearStart = today.withDayOfYear(1).toEpochDay()
        val yearEnd = today.toEpochDay()

        val totalMonth = runRepository.getTotalDistanceKm(monthStart, monthEnd)
        val totalYear = runRepository.getTotalDistanceKm(yearStart, yearEnd)
        val fastestPace = runRepository.getFastestPaceMinPerKm()
        val longestRun = runRepository.getLongestRunKm()

        _uiState.update {
            it.copy(
                isLoading = false,
                totalKmThisMonth = totalMonth,
                totalKmThisYear = totalYear,
                fastestPaceMinPerKm = fastestPace,
                longestRunKm = longestRun,
            )
        }
    }
}
