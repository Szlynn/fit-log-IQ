# Running Module – Requirements

## Overview

The Running module allows the user to log runs, manage a single active training plan, and view history and statistics. The primary goal is fast, low-friction data entry on mobile.

---

## 1. Data Model

### 1.1. Run Entry

Every logged run has the following fields:

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK) | Auto-generated |
| `date` | LocalDate | Defaults to today; user can change via date picker |
| `type` | RunType | `TRAINING_PLAN`, `RACE`, or `CASUAL` |
| `distanceKm` | Double | e.g. 5.4 |
| `durationSeconds` | Long | Stored as total seconds |
| `note` | String? | Optional free text |
| `trainingPlanDayId` | Long? | FK → TrainingPlanDay; only set when type = TRAINING_PLAN |
| `raceName` | String? | Only set when type = RACE |

**Computed (not stored):**
- Pace: `durationSeconds / 60.0 / distanceKm` → min/km
- Average speed: `distanceKm / (durationSeconds / 3600.0)` → km/h

### 1.2. Training Plan

A training plan is a named template broken into weeks and days.

**TrainingPlan**

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK) | |
| `name` | String | e.g. "5km → 10km" |
| `isActive` | Boolean | Only one plan can be active at a time |

**TrainingPlanWeek**

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK) | |
| `planId` | Long | FK → TrainingPlan |
| `weekNumber` | Int | 1-based, determines display order |

**TrainingPlanDay**

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long (PK) | |
| `weekId` | Long | FK → TrainingPlanWeek |
| `dayOrder` | Int | 1-based position within the week; determines progression order |
| `dayLabel` | String | Free-text label, e.g. "Tuesday", "Day A", "Long run day" |
| `targetDistanceKm` | Double | Planned distance |
| `character` | String? | e.g. "easy run", "long run" |
| `status` | DayStatus | `PENDING`, `COMPLETED`, or `SKIPPED` |

```kotlin
enum class DayStatus { PENDING, COMPLETED, SKIPPED }
```

---

## 2. Screens

### 2.1. Log Run Screen

The most frequently used screen. Optimised for minimal typing.

**Fields and controls:**

- **Date picker** – defaults to today; tapping opens a calendar picker
- **Run type selector** – horizontal 3-button toggle: `Training Plan` / `Race` / `Casual`
- **Dynamic section** (shown/hidden based on type):
  - `TRAINING_PLAN`: shows the active plan name and the pre-filled next pending day (read-only, auto-populated)
  - `RACE`: shows a free-text "Race name" input field
  - `CASUAL`: no extra fields
- **Distance** – numeric input (decimal keyboard), in km
- **Duration** – scrollable picker (hours : minutes : seconds)
- **Note** – optional free-text field
- **Skip Day button** (Training Plan type only) – marks the pre-filled day as `SKIPPED` without requiring distance/duration; saves immediately
- **Save button** – validates and saves the run entry

**Pre-fill logic:**
When the screen opens with type = `TRAINING_PLAN`, the app automatically finds the next `PENDING` day in the active training plan (ordered by week number, then day order within the week) and pre-fills it as a placeholder. The target distance of that day is shown as a placeholder in the distance field.

**Validation:**
- Distance and duration are required for `TRAINING_PLAN` and `RACE` and `CASUAL` types (not required for Skip Day)
- Race name is required when type = `RACE`

### 2.2. Training Plans Screen

Displays and manages the user's training plans.

**Plan list view:**
- Lists all training plans; the active plan is visually highlighted
- Tapping a plan opens its detail/overview view
- Button to create a new plan

**Plan detail / overview view:**
- Displays the plan as a matrix: rows = weeks, columns = days
- Each day cell is colour-coded by status:
  - **Green** – `COMPLETED`
  - **Light grey / strikethrough** – `SKIPPED`
  - **White / empty** – `PENDING`
- Each week row has a `...` overflow menu with:
  - **Restart week** – sets all days in that week back to `PENDING`; existing run log entries are NOT deleted
  - ~~Mark week complete~~ *(removed – not needed)*
- Tapping a day cell shows its details (target distance, character, linked run entry if completed)

**Plan editor:**
- Accessible from the plan detail view (edit button)
- Fields: plan name
- `+ Add Week` button appends a new week
- Within each week: `+ Add Day` button; each day has:
  - Day label (free text, e.g. "Tuesday")
  - Target distance (km)
  - Character (optional, free text, e.g. "easy run")
  - Delete button
- Editing a plan does NOT retroactively affect already-completed or skipped days; it only affects future `PENDING` days
- Option to set this plan as the active plan

### 2.3. Run Log Screen

A chronological list of all logged run entries.

- **Filter bar** at the top: `All` | `Training Plan` | `Races`
- Each entry is displayed as a card:
  - Left: icon indicating type (calendar / trophy / shoe)
  - Centre: date, distance, duration, pace
  - Tapping a card opens the entry detail view
- **Entry detail view:**
  - Shows all fields including note, average speed, linked plan day (if applicable)
  - **Edit** button – opens the Log Run screen pre-filled with existing data
  - **Delete** button – deletes the entry with a confirmation dialog; if the entry was linked to a training plan day, that day's status is reset to `PENDING`

### 2.4. Stats & Race Log Screen

**Race log section:**
- Dedicated list of all `RACE` type runs
- Shows: race name, date, distance, duration, pace

**Statistics section:**
- Total km this month
- Total km this year
- Personal records:
  - Fastest pace (over any distance)
  - Longest single run (km)

> Statistics include all run entries regardless of week restarts or skips.

---

## 3. Business Rules

1. **Single active plan** – only one training plan can be active at a time. Setting a plan as active deactivates the previously active one.
2. **Next pending day** – determined by ascending week number, then ascending day order within the week.
3. **Skip Day** – creates no `RunEntity`; only sets the `TrainingPlanDay.status` to `SKIPPED`.
4. **Save run (Training Plan type)** – sets the linked `TrainingPlanDay.status` to `COMPLETED`.
5. **Restart week** – sets all `TrainingPlanDay` rows of that week to `PENDING`. Existing `RunEntity` rows linked to those days are NOT deleted and remain in the log and statistics.
6. **Delete run entry** – if the entry was linked to a `TrainingPlanDay`, that day's status is reset to `PENDING`.
7. **Edit run entry** – editing a run's type, distance, duration, or linked day updates the existing `RunEntity`. If the linked day changes, the old day's status is reset to `PENDING` and the new day's status is set to `COMPLETED`.
8. **Plan edits are forward-only** – editing a training plan's structure (adding/removing days or weeks) does not affect the status of already-completed or skipped days.
9. **Duration display** – shown as `mm:ss` when under 1 hour, `h:mm:ss` when 1 hour or more.

---

## 4. Out of Scope

- Multiple simultaneous active training plans — **by design**: the Running module supports exactly one active plan at a time, permanently. This is not a v1 limitation.
- Notifications or reminders
- GPS / map tracking
- Export to CSV or Excel
- Calisthenics module (separate spec)
