# FitLogIQ

A personal Android fitness tracking app replacing Excel-based workout logs.

## Modules

- **Running** – training plan tracking, race log, casual runs
- **Calisthenics** – bodyweight exercises with sets and reps *(planned)*

## Tech Stack

- Kotlin, Jetpack Compose, Room, ViewModel + StateFlow, Hilt, Navigation Compose
- Testing: JUnit4, Turbine
- Code style: ktlint

## Architecture

`Repository → ViewModel → Composable`

No separate domain/use-case layer. ViewModels expose `StateFlow<UiState>`, repositories are the only layer touching Room DAOs.

## Project Structure

```
app/src/main/java/io/github/szlynn/fitlogiq/
  data/
    db/            # Room database, DAOs, entities, relation classes
    model/         # Enums (RunType, DayStatus)
    repository/    # RunRepository, TrainingPlanRepository
  di/              # Hilt DatabaseModule
  ui/
    components/    # Shared Composables (DurationPickerDialog)
    navigation/    # Screen routes, NavHost, bottom nav destinations
    running/       # All running module screens and ViewModels
  util/            # FormatUtils (duration, pace formatting)
  FitLogIQApp.kt   # @HiltAndroidApp
  MainActivity.kt  # @AndroidEntryPoint
```

## Running Module – Screens

| Screen | Description |
|--------|-------------|
| Run Log | Chronological run list with filter (All / Training / Races) and delete |
| Log Run | Add or edit a run entry; pre-fills next training plan day |
| Training Plans | List of all plans with active plan indicator |
| Training Plan Detail | Week/day matrix overview with status colours; restart week |
| Training Plan Editor | Create or edit a plan with weeks and days (up/down reorder) |
| Stats & Race Log | Monthly/yearly totals, records, dedicated race list |

## Functional Spec

See [`docs/running-requirements.md`](docs/running-requirements.md) for the full Running module spec.

## Project Conventions

See [`.kiro/steering/`](.kiro/steering/) for coding conventions and data model reference.

## Known Issues / TODOs

See [`docs/todos.md`](docs/todos.md).
