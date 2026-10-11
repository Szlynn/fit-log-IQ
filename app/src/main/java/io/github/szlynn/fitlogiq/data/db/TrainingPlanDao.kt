package io.github.szlynn.fitlogiq.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.szlynn.fitlogiq.data.model.DayStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {
    // --- Plans ---

    @Insert
    suspend fun insertPlan(plan: TrainingPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: TrainingPlanEntity)

    @Delete
    suspend fun deletePlan(plan: TrainingPlanEntity)

    @Query("SELECT * FROM training_plans ORDER BY name ASC")
    fun getAllPlans(): Flow<List<TrainingPlanEntity>>

    @Query("SELECT * FROM training_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlan(): Flow<TrainingPlanEntity?>

    @Query("SELECT * FROM training_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlanOnce(): TrainingPlanEntity?

    /** Deactivates all plans; call before activating a new one. */
    @Query("UPDATE training_plans SET isActive = 0")
    suspend fun deactivateAllPlans()

    @Query("SELECT * FROM training_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): TrainingPlanEntity?

    // --- Weeks ---

    @Insert
    suspend fun insertWeek(week: TrainingPlanWeekEntity): Long

    @Update
    suspend fun updateWeek(week: TrainingPlanWeekEntity)

    @Delete
    suspend fun deleteWeek(week: TrainingPlanWeekEntity)

    @Query("SELECT * FROM training_plan_weeks WHERE planId = :planId ORDER BY weekNumber ASC")
    fun getWeeksForPlan(planId: Long): Flow<List<TrainingPlanWeekEntity>>

    @Query("SELECT * FROM training_plan_weeks WHERE planId = :planId ORDER BY weekNumber ASC")
    suspend fun getWeeksForPlanOnce(planId: Long): List<TrainingPlanWeekEntity>

    // --- Days ---

    @Insert
    suspend fun insertDay(day: TrainingPlanDayEntity): Long

    @Update
    suspend fun updateDay(day: TrainingPlanDayEntity)

    @Delete
    suspend fun deleteDay(day: TrainingPlanDayEntity)

    @Query("SELECT * FROM training_plan_days WHERE weekId = :weekId ORDER BY dayOrder ASC")
    fun getDaysForWeek(weekId: Long): Flow<List<TrainingPlanDayEntity>>

    @Query("SELECT * FROM training_plan_days WHERE weekId = :weekId ORDER BY dayOrder ASC")
    suspend fun getDaysForWeekOnce(weekId: Long): List<TrainingPlanDayEntity>

    @Query("SELECT * FROM training_plan_days WHERE id = :id")
    suspend fun getDayById(id: Long): TrainingPlanDayEntity?

    @Query("UPDATE training_plan_days SET status = :status WHERE id = :dayId")
    suspend fun updateDayStatus(dayId: Long, status: DayStatus)

    /** Resets all days in a week to PENDING (used by "Restart week"). */
    @Query("UPDATE training_plan_days SET status = 'PENDING' WHERE weekId = :weekId")
    suspend fun resetWeekDays(weekId: Long)

    // --- Relations ---

    @Transaction
    @Query("SELECT * FROM training_plans ORDER BY name ASC")
    fun getAllPlansWithWeeksAndDays(): Flow<List<PlanWithWeeksAndDays>>

    @Transaction
    @Query("SELECT * FROM training_plans WHERE id = :planId")
    fun getPlanWithWeeksAndDays(planId: Long): Flow<PlanWithWeeksAndDays?>
}
