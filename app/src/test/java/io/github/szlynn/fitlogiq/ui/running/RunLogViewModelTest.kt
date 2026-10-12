package io.github.szlynn.fitlogiq.ui.running

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.github.szlynn.fitlogiq.data.db.RunEntity
import io.github.szlynn.fitlogiq.data.model.DayStatus
import io.github.szlynn.fitlogiq.data.model.RunType
import io.github.szlynn.fitlogiq.data.repository.RunRepository
import io.github.szlynn.fitlogiq.data.repository.TrainingPlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RunLogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var runRepository: RunRepository
    private lateinit var trainingPlanRepository: TrainingPlanRepository

    private val today = LocalDate.now().toEpochDay()

    private val casualRun = RunEntity(
        id = 1L, date = today, type = RunType.CASUAL, distanceKm = 5.0, durationSeconds = 1800,
    )
    private val trainingRun = RunEntity(
        id = 2L, date = today, type = RunType.TRAINING_PLAN, distanceKm = 8.0,
        durationSeconds = 2700, trainingPlanDayId = 10L,
    )
    private val raceRun = RunEntity(
        id = 3L, date = today, type = RunType.RACE, distanceKm = 21.1,
        durationSeconds = 7200, raceName = "Half Marathon",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        runRepository = mockk(relaxed = true)
        trainingPlanRepository = mockk(relaxed = true)
        every { runRepository.getAllRuns() } returns flowOf(listOf(trainingRun, casualRun, raceRun))
        every { runRepository.getRunsByType(RunType.TRAINING_PLAN) } returns flowOf(listOf(trainingRun))
        every { runRepository.getRunsByType(RunType.RACE) } returns flowOf(listOf(raceRun))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads all runs`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.runs.size)
        assertEquals(RunLogFilter.ALL, state.filter)
    }

    @Test
    fun `filter change loads only training plan runs`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFilterChange(RunLogFilter.TRAINING_PLAN)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.runs.size)
        assertEquals(RunType.TRAINING_PLAN, vm.uiState.value.runs.first().type)
    }

    @Test
    fun `filter change loads only races`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFilterChange(RunLogFilter.RACES)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.runs.size)
        assertEquals(RunType.RACE, vm.uiState.value.runs.first().type)
    }

    @Test
    fun `delete request sets pendingDeleteRun`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDeleteRequest(casualRun)

        assertEquals(casualRun, vm.uiState.value.pendingDeleteRun)
    }

    @Test
    fun `delete dismiss clears pendingDeleteRun`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDeleteRequest(casualRun)
        vm.onDeleteDismiss()

        assertNull(vm.uiState.value.pendingDeleteRun)
    }

    @Test
    fun `delete confirm deletes run and clears pending`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDeleteRequest(casualRun)
        vm.onDeleteConfirm()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { runRepository.deleteRun(casualRun) }
        assertNull(vm.uiState.value.pendingDeleteRun)
    }

    @Test
    fun `delete training plan run resets linked day to pending`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDeleteRequest(trainingRun)
        vm.onDeleteConfirm()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { trainingPlanRepository.updateDayStatus(10L, DayStatus.PENDING) }
        coVerify { runRepository.deleteRun(trainingRun) }
    }

    @Test
    fun `delete casual run does not touch training plan`() = runTest {
        val vm = RunLogViewModel(runRepository, trainingPlanRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDeleteRequest(casualRun)
        vm.onDeleteConfirm()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { trainingPlanRepository.updateDayStatus(any(), any()) }
    }
}
