# FitLogIQ – Project Conventions

## Language

- All code, comments, documentation, commit messages, and file names must be in **English**.
- User-facing UI strings are in **Hungarian**, defined in `res/values/strings.xml` only.
- Developer conversations may be in Hungarian.

## Project Overview

FitLogIQ is a personal Android fitness tracking app replacing Excel-based workout logs.
Two workout modules: **Running** and **Calisthenics**.

- Running: training plan tracking (week/day progression), race log, casual runs
- Calisthenics: bodyweight exercises with sets and reps *(planned)*
- Stats: totals, personal records, race history

Full functional spec: `docs/running-requirements.md`

## Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Database | Room (SQLite) |
| State management | ViewModel + StateFlow |
| Navigation | Jetpack Navigation Compose |
| Dependency injection | Hilt (minimal use) |
| Code style | ktlint |
| Unit testing | JUnit4 + Turbine (for Flow) |

## Architecture

Flat and simple: **Repository → ViewModel → Composable**

- No separate domain/use-case layer unless complexity clearly demands it
- No `LiveData` — use `StateFlow` / `SharedFlow`
- ViewModels expose a single `UiState` data class via `StateFlow<UiState>`
- One-time events use `SharedFlow<Event>` with a sealed interface
- Repositories are the only layer that talks to Room DAOs

## Project Structure

```
app/src/main/java/io/github/szlynn/fitlogiq/
  data/
    db/            # Room database, DAOs, entities, PlanRelations
    model/         # Enums: RunType, DayStatus
    repository/    # RunRepository, TrainingPlanRepository
  di/              # Hilt DatabaseModule
  ui/
    components/    # Shared Composables (DurationPickerDialog)
    navigation/    # Screen, FitLogIQNavHost, TopLevelDestinations
    running/       # All running module screens and ViewModels
  util/            # FormatUtils (formatDuration, formatPace)
  FitLogIQApp.kt   # @HiltAndroidApp
  MainActivity.kt  # @AndroidEntryPoint
```

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Screen composable | `<Feature>Screen` | `RunLogScreen` |
| ViewModel | `<Feature>ViewModel` | `RunLogViewModel` |
| UI state | `<Feature>UiState` | `RunLogUiState` |
| UI events | `<Feature>Event` | `LogRunEvent` |
| Room entity | `<Feature>Entity` | `RunEntity` |
| DAO | `<Feature>Dao` | `RunDao` |
| Repository | `<Feature>Repository` | `RunRepository` |
| Room relations | descriptive name | `PlanWithWeeksAndDays` |

## Coding Rules

- No `!!` non-null assertion — use `?.`, `?:`, or `requireNotNull("reason")`
- No hardcoded Hungarian strings in Kotlin — always use `stringResource()` in Compose
- No business logic in Composables — keep them purely presentational
- No direct DAO access from ViewModels — always go through a Repository
- All Room queries are `suspend fun` or return `Flow`
- Prefer `data class` for models; keep models immutable
- Use `sealed interface` for `UiState` events

## Code Style

- ktlint with default rules
- Max line length: 120 characters
- Trailing commas in multi-line parameter lists
