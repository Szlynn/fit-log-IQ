# FitLogIQ – Project Conventions

## Language

- All code, comments, documentation, commit messages, and file names must be in **English**.
- User-facing UI strings are in **Hungarian**, defined in `res/values/strings.xml` only.
- Developer conversations may be in Hungarian.

## Project Overview

FitLogIQ is a personal Android fitness tracking app replacing Excel-based workout logs.
Two workout modules: **Running** and **Calisthenics**.

- Running: training plan tracking (week/day progression), race log, casual runs
- Calisthenics: bodyweight exercises with sets and reps
- Stats: totals, personal records, race history

Full functional spec: `plan.md` (in Hungarian)

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
- ViewModels expose a single `UiState` sealed class via `StateFlow<UiState>`
- Repositories are the only layer that talks to Room DAOs

## Project Structure

```
app/src/main/
  java/hu/fitlogiq/
    data/
      db/            # RoomDatabase, DAOs
      model/         # Room @Entity classes (suffix: Entity, e.g. RunEntity)
      repository/    # Repository classes
    ui/
      running/       # Running module: screens + ViewModels
      calisthenics/  # Calisthenics module: screens + ViewModels
      stats/         # Stats and race log screens
      components/    # Shared reusable Composables
    util/            # Extensions and helpers
    MainActivity.kt
    FitLogIQApp.kt   # @HiltAndroidApp Application class
  res/
    values/
      strings.xml    # All Hungarian UI strings go here
```

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Screen composable | `<Feature>Screen` | `RunningLogScreen` |
| ViewModel | `<Feature>ViewModel` | `RunningLogViewModel` |
| Room entity | `<Feature>Entity` | `RunEntity` |
| DAO | `<Feature>Dao` | `RunDao` |
| Repository | `<Feature>Repository` | `RunRepository` |
| UI state | `<Feature>UiState` | `RunningLogUiState` |

## Coding Rules

- No `!!` non-null assertion — use `?.`, `?:`, or `requireNotNull("reason")`
- No hardcoded Hungarian strings in Kotlin — always use `stringResource()` in Compose
- No business logic in Composables — keep them purely presentational
- No direct DAO access from ViewModels — always go through a Repository
- All Room queries are `suspend fun` or return `Flow`
- Prefer `data class` for models; keep models immutable
- Use `sealed interface` for `UiState` and one-time UI events

## Code Style

- ktlint with default rules
- Max line length: 120 characters
- Trailing commas in multi-line parameter lists
