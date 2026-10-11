package io.github.szlynn.fitlogiq.data.db

import androidx.room.Embedded
import androidx.room.Relation

/** A week with all its days, ordered by dayOrder. */
data class WeekWithDays(
    @Embedded val week: TrainingPlanWeekEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "weekId",
    )
    val days: List<TrainingPlanDayEntity>,
) {
    /** Days sorted by dayOrder ascending. */
    val sortedDays: List<TrainingPlanDayEntity>
        get() = days.sortedBy { it.dayOrder }
}

/** A full training plan with all its weeks and days. */
data class PlanWithWeeksAndDays(
    @Embedded val plan: TrainingPlanEntity,
    @Relation(
        entity = TrainingPlanWeekEntity::class,
        parentColumn = "id",
        entityColumn = "planId",
    )
    val weeksWithDays: List<WeekWithDays>,
) {
    /** Weeks sorted by weekNumber ascending. */
    val sortedWeeks: List<WeekWithDays>
        get() = weeksWithDays.sortedBy { it.week.weekNumber }
}
