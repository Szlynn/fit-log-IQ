package io.github.szlynn.fitlogiq.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RunEntity::class,
        TrainingPlanEntity::class,
        TrainingPlanWeekEntity::class,
        TrainingPlanDayEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class FitLogIQDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
    abstract fun trainingPlanDao(): TrainingPlanDao
}
