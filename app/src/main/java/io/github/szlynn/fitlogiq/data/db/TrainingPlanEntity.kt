package io.github.szlynn.fitlogiq.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_plans")
data class TrainingPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Only one plan can have isActive = true at a time. */
    val isActive: Boolean = false,
)
