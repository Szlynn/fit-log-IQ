package io.github.szlynn.fitlogiq.data.repository

import io.github.szlynn.fitlogiq.data.db.TrainingPlanDao
import io.github.szlynn.fitlogiq.data.db.TrainingPlanDayEntity
import io.github.szlynn.fitlogiq.data.db.TrainingPlanEntity
import io.github.szlynn.fitlogiq.data.db.TrainingPlanWeekEntity
import io.github.szlynn.fitlogiq.data.model.DayStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingPlanRepository @Inject constructor(private val dao: TrainingPlanDao) {
    // --- Plans ---

    fun getAllPlans(): Flow<List<TrainingPlanEntity>> = dao.getAllPlans()

    fun getActivePlan(): Flow<TrainingPlanEntity?> = dao.getActivePlan()

    suspend fun getActivePlanOnce(): TrainingPlanEntity? = dao.getActivePlanOnce()

    suspend fun savePlan(plan: TrainingPlanEntity): Long = dao.insertPlan(plan)

    suspend fun updatePlan(plan: TrainingPlanEntity) = dao.updatePlan(plan)

    suspend fun deletePlan(plan: TrainingPlanEntity) = dao.deletePlan(plan)

    /**
     * Sets the given plan as the only active plan.
     * Deactivates all other plans first.
     */
    suspend fun setActivePlan(planId: Long) {
        dao.deactivateAllPlans()
        val plan = dao.getPlanById(planId) ?: return
        dao.updatePlan(plan.copy(isActive = true))
    }

    // --- Weeks ---

    fun getWeeksForPlan(planId: Long): Flow<List<TrainingPlanWeekEntity>> =
        dao.getWeeksForPlan(planId)

    suspend fun saveWeek(week: TrainingPlanWeekEntity): Long = dao.insertWeek(week)

    suspend fun updateWeek(week: TrainingPlanWeekEntity) = dao.updateWeek(week)

    suspend fun deleteWeek(week: TrainingPlanWeekEntity) = dao.deleteWeek(week)

    // --- Days ---

    fun getDaysForWeek(weekId: Long): Flow<List<TrainingPlanDayEntity>> =
        dao.getDaysForWeek(weekId)

    suspend fun getDaysForWeekOnce(weekId: Long): List<TrainingPlanDayEntity> =
        dao.getDaysForWeekOnce(weekId)

    suspend fun saveDay(day: TrainingPlanDayEntity): Long = dao.insertDay(day)

    suspend fun updateDay(day: TrainingPlanDayEntity) = dao.updateDay(day)

    suspend fun deleteDay(day: TrainingPlanDayEntity) = dao.deleteDay(day)

    suspend fun getDayById(id: Long): TrainingPlanDayEntity? = dao.getDayById(id)

    suspend fun updateDayStatus(dayId: Long, status: DayStatus) =
        dao.updateDayStatus(dayId, status)

    /** Resets all days in a week to PENDING. Does not delete run entries. */
    suspend fun restartWeek(weekId: Long) = dao.resetWeekDays(weekId)

    /**
     * Returns the next PENDING day across all weeks of the active plan,
     * ordered by weekNumber ASC, then dayOrder ASC.
     * Returns null if there is no active plan or no pending days remain.
     */
    suspend fun getNextPendingDay(): TrainingPlanDayEntity? {
        val activePlan = dao.getActivePlanOnce() ?: return null
        val weeks = dao.getWeeksForPlanOnce(activePlan.id)
        for (week in weeks) {
            val days = dao.getDaysForWeekOnce(week.id)
            val next = days.firstOrNull { it.status == DayStatus.PENDING }
            if (next != null) return next
        }
        return null
    }
}
