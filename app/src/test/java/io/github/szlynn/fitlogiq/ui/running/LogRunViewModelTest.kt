package io.github.szlynn.fitlogiq.ui.running

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.github.szlynn.fitlogiq.data.db.RunEntity
import io.github.szlynn.fitlogiq.data.db.TrainingPlanDayEntity
import io.github.szlynn.fitlogiq.data.db.TrainingPlanEntity
import io.github.szlynn.fitlogiq.data.model.DayStatus
import io.github.szlynn.fitlogiq.data.model.RunType
import io.github.szlynn.fitlogiq.data.repository.RunRepository
import io.github.szlynn.fitlogiq.data.repository.TrainingPlanRepository
import io.github.szlynn.fitlogiq.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LogRunViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var runRepository: RunRepository
    private lateinit var trainingPlanRepository: TrainingPlanRepository

    private val activePlan = TrainingPlanEntity(id = 1L, name = "Test Plan", isActive = true)
    private val pendingDay = TrainingPlanDayEntity(
        id = 10L,
        weekId = 1L,
        dayOrder = 1,
        dayLabel = "Day A",
        targetDistanceKm = 5.0,
        status = DayStatus.PENDING,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        runRepository = mockk(relaxed = true)
        trainingPlanRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(runId: Long? = null): LogRunViewModel {
        val savedState = if (runId != null) {
            SavedStateHandle(mapOf(Screen.LogRunEdit.ARG to runId))
        } else {
            SavedStateHandle()
        }
        return LogRunViewModel(runRepository, trainingPlanRepository, savedState)
    }

    // --- New run defaults ---

    @Test
    fun `new run pre-fills next pending day and distance from active plan`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns activePlan
        coEvery { trainingPlanRepository.getNextPendingDay() } returns pendingDay

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEditMode)
        assertEquals("5.0", state.distanceKm)
        assertEquals(pendingDay, state.nextPendingDay)
        assertEquals("Test Plan", state.activePlanName)
    }

    @Test
    fun `new run with no active plan leaves distance empty`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns null
        coEvery { trainingPlanRepository.getNextPendingDay() } returns null

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("", state.distanceKm)
        assertEquals(null, state.activePlanName)
    }

    // --- Type switching ---

    @Test
    fun `switching from training plan to casual clears pre-filled distance`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns activePlan
        coEvery { trainingPlanRepository.getNextPendingDay() } returns pendingDay

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onRunTypeChange(RunType.CASUAL)

        assertEquals("", vm.uiState.value.distanceKm)
    }

    @Test
    fun `switching back to training plan restores pre-filled distance`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns activePlan
        coEvery { trainingPlanRepository.getNextPendingDay() } returns pendingDay

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onRunTypeChange(RunType.CASUAL)
        vm.onRunTypeChange(RunType.TRAINING_PLAN)

        assertEquals("5.0", vm.uiState.value.distanceKm)
    }

    // --- Validation ---

    @Test
    fun `save with empty distance sets distanceError`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns null
        coEvery { trainingPlanRepository.getNextPendingDay() } returns null

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onRunTypeChange(RunType.CASUAL)
        vm.onSave()

        assertTrue(vm.uiState.value.distanceError)
    }

    @Test
    fun `save with zero duration sets durationError`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns null
        coEvery { trainingPlanRepository.getNextPendingDay() } returns null

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onRunTypeChange(RunType.CASUAL)
        vm.onDistanceChange("5.0")
        // duration stays 0
        vm.onSave()

        assertTrue(vm.uiState.value.durationError)
    }

    @Test
    fun `save race with empty race name sets raceNameError`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns null
        coEvery { trainingPlanRepository.getNextPendingDay() } returns null

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onRunTypeChange(RunType.RACE)
        vm.onDistanceChange("10.0")
        vm.onDurationChange(0, 50, 0)
        vm.onSave()

        assertTrue(vm.uiState.value.raceNameError)
    }

    // --- Save new run ---

    @Test
    fun `saving valid training plan run inserts run and marks day completed`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns activePlan
        coEvery { trainingPlanRepository.getNextPendingDay() } returns pendingDay
        coEvery { runRepository.saveRun(any()) } returns 99L

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDurationChange(0, 45, 0)
        vm.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { runRepository.saveRun(match { it.trainingPlanDayId == 10L && it.distanceKm == 5.0 }) }
        coVerify { trainingPlanRepository.updateDayStatus(10L, DayStatus.COMPLETED) }
    }

    @Test
    fun `saving valid casual run does not touch training plan`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns null
        coEvery { trainingPlanRepository.getNextPendingDay() } returns null
        coEvery { runRepository.saveRun(any()) } returns 99L

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onRunTypeChange(RunType.CASUAL)
        vm.onDistanceChange("8.5")
        vm.onDurationChange(0, 55, 30)
        vm.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { runRepository.saveRun(match { it.type == RunType.CASUAL && it.trainingPlanDayId == null }) }
        coVerify(exactly = 0) { trainingPlanRepository.updateDayStatus(any(), any()) }
    }

    @Test
    fun `save emits NavigateBack on success`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns null
        coEvery { trainingPlanRepository.getNextPendingDay() } returns null
        coEvery { runRepository.saveRun(any()) } returns 99L

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.events.test {
            vm.onRunTypeChange(RunType.CASUAL)
            vm.onDistanceChange("5.0")
            vm.onDurationChange(0, 30, 0)
            vm.onSave()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(LogRunEvent.NavigateBack, awaitItem())
        }
    }

    // --- Skip day ---

    @Test
    fun `skip day marks pending day as skipped and emits NavigateBack`() = runTest {
        coEvery { trainingPlanRepository.getActivePlanOnce() } returns activePlan
        coEvery { trainingPlanRepository.getNextPendingDay() } returns pendingDay

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.events.test {
            vm.onSkipDay()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { trainingPlanRepository.updateDayStatus(10L, DayStatus.SKIPPED) }
            assertEquals(LogRunEvent.NavigateBack, awaitItem())
        }
    }
}
