# FitLogIQ – Data Model Reference

Full requirements: `docs/running-requirements.md`

---

## Running Module

### Enums

```kotlin
enum class RunType { TRAINING_PLAN, RACE, CASUAL }
enum class DayStatus { PENDING, COMPLETED, SKIPPED }
```

### RunEntity

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK, autoGenerate) | |
| `date` | Long | Stored as epoch day (LocalDate.toEpochDay()) |
| `type` | RunType | |
| `distanceKm` | Double | |
| `durationSeconds` | Long | Total seconds |
| `note` | String? | Optional |
| `trainingPlanDayId` | Long? | FK → TrainingPlanDayEntity; only when type = TRAINING_PLAN |
| `raceName` | String? | Only when type = RACE |

Computed (not stored): pace (min/km), average speed (km/h).

### TrainingPlanEntity

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK, autoGenerate) | |
| `name` | String | e.g. "5km → 10km" |
| `isActive` | Boolean | Only one plan can be active at a time |

### TrainingPlanWeekEntity

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK, autoGenerate) | |
| `planId` | Long | FK → TrainingPlanEntity |
| `weekNumber` | Int | 1-based, determines order |

### TrainingPlanDayEntity

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK, autoGenerate) | |
| `weekId` | Long | FK → TrainingPlanWeekEntity |
| `dayLabel` | String | e.g. "Tuesday" |
| `dayOrder` | Int | Order within the week |
| `targetDistanceKm` | Double | |
| `character` | String? | e.g. "easy run" |
| `status` | DayStatus | PENDING / COMPLETED / SKIPPED |

---

## Key Business Rules

1. Only one `TrainingPlanEntity` can have `isActive = true` at a time.
2. "Next pending day" = lowest `weekNumber`, then lowest `dayOrder`, where `status = PENDING`.
3. Logging a TRAINING_PLAN run → sets linked day's `status = COMPLETED`.
4. Skipping a day → sets `status = SKIPPED`; no `RunEntity` is created.
5. Deleting a run linked to a plan day → resets that day's `status = PENDING`.
6. Restarting a week → sets all days in that week to `PENDING`; linked `RunEntity` rows are NOT deleted.
7. Editing a run's linked day → old day reset to `PENDING`, new day set to `COMPLETED`.
8. Plan structure edits are forward-only; completed/skipped days are unaffected.

---

## Duration Display Rule

- Under 1 hour: `mm:ss`
- 1 hour or more: `h:mm:ss`

---

## Calisthenics Module

To be defined. Rough shape:
- `WorkoutSessionEntity` – date, note
- `ExerciseSetEntity` – FK to session, exercise name, sets, reps
