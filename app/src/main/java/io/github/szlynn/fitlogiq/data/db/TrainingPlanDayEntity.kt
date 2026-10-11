package io.github.szlynn.fitlogiq.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.szlynn.fitlogiq.data.model.DayStatus

@Entity(
    tableName = "training_plan_days",
    foreignKeys = [
        ForeignKey(
            entity = TrainingPlanWeekEntity::class,
            parentColumns = ["id"],
            childColumns = ["weekId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("weekId")],
)
data class TrainingPlanDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekId: Long,
    /** Position within the week; determines progression order. */
    val dayOrder: Int,
    /** Free-text label, e.g. "Tuesday", "Day A". */
    val dayLabel: String,
    val targetDistanceKm: Double,
    /** Optional description, e.g. "easy run", "long run". */
    val character: String? = null,
    val status: DayStatus = DayStatus.PENDING,
)
