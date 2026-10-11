# FitLogIQ

A personal Android fitness tracking app replacing Excel-based workout logs.

## Modules

- **Running** – training plan tracking, race log, casual runs
- **Calisthenics** – bodyweight exercises with sets and reps

## Tech Stack

- Kotlin, Jetpack Compose, Room, ViewModel + StateFlow, Hilt, Navigation Compose
- Testing: JUnit4, Turbine
- Code style: ktlint

## Architecture

`Repository → ViewModel → Composable`

No separate domain/use-case layer. ViewModels expose `StateFlow<UiState>`, repositories are the only layer touching Room DAOs.

## Functional Spec

See [`plan.md`](plan.md) (in Hungarian) for the full feature and data model description.

## Project Conventions

See [`.kiro/steering/`](.kiro/steering/) for coding conventions and data model reference used by the AI assistant.
