package io.github.szlynn.fitlogiq.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.szlynn.fitlogiq.data.model.RunType

@Entity(
    tableName = "runs",
    foreignKeys = [
        ForeignKey(
            entity = TrainingPlanDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingPlanDayId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("trainingPlanDayId")],
)
data class RunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Stored as epoch day (LocalDate.toEpochDay()). */
    val date: Long,
    val type: RunType,
    val distanceKm: Double,
    /** Total duration in seconds. */
    val durationSeconds: Long,
    val note: String? = null,
    /** Set only when type == TRAINING_PLAN. */
    val trainingPlanDayId: Long? = null,
    /** Set only when type == RACE. */
    val raceName: String? = null,
)
