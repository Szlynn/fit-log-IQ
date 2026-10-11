package io.github.szlynn.fitlogiq.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.szlynn.fitlogiq.data.model.RunType
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Insert
    suspend fun insert(run: RunEntity): Long

    @Update
    suspend fun update(run: RunEntity)

    @Delete
    suspend fun delete(run: RunEntity)

    @Query("SELECT * FROM runs ORDER BY date DESC")
    fun getAllRuns(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE type = :type ORDER BY date DESC")
    fun getRunsByType(type: RunType): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getRunById(id: Long): RunEntity?

    @Query("SELECT * FROM runs WHERE trainingPlanDayId = :dayId LIMIT 1")
    suspend fun getRunByDayId(dayId: Long): RunEntity?

    /** Total distance for runs within a given date range (epoch days). */
    @Query("SELECT COALESCE(SUM(distanceKm), 0.0) FROM runs WHERE date >= :fromDay AND date <= :toDay")
    suspend fun getTotalDistanceKm(fromDay: Long, toDay: Long): Double

    /** Fastest pace (min/km) = min(durationSeconds / 60.0 / distanceKm). */
    @Query("SELECT MIN(CAST(durationSeconds AS REAL) / 60.0 / distanceKm) FROM runs WHERE distanceKm > 0")
    suspend fun getFastestPaceMinPerKm(): Double?

    @Query("SELECT MAX(distanceKm) FROM runs")
    suspend fun getLongestRunKm(): Double?
}
