package io.github.szlynn.fitlogiq.data.repository

import io.github.szlynn.fitlogiq.data.db.RunDao
import io.github.szlynn.fitlogiq.data.db.RunEntity
import io.github.szlynn.fitlogiq.data.model.RunType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepository @Inject constructor(private val dao: RunDao) {
    fun getAllRuns(): Flow<List<RunEntity>> = dao.getAllRuns()

    fun getRunsByType(type: RunType): Flow<List<RunEntity>> = dao.getRunsByType(type)

    suspend fun getRunById(id: Long): RunEntity? = dao.getRunById(id)

    suspend fun getRunByDayId(dayId: Long): RunEntity? = dao.getRunByDayId(dayId)

    suspend fun saveRun(run: RunEntity): Long = dao.insert(run)

    suspend fun updateRun(run: RunEntity) = dao.update(run)

    suspend fun deleteRun(run: RunEntity) = dao.delete(run)

    suspend fun getTotalDistanceKm(fromDay: Long, toDay: Long): Double =
        dao.getTotalDistanceKm(fromDay, toDay)

    suspend fun getFastestPaceMinPerKm(): Double? = dao.getFastestPaceMinPerKm()

    suspend fun getLongestRunKm(): Double? = dao.getLongestRunKm()
}
