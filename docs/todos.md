# TODOs & Known Issues

Tracked items that need attention in future sessions.
Format: `- [ ] description *(file:line if relevant)*`

---

## UI / Polish

- [ ] Run type icons in `RunLogScreen` are all placeholder `DateRange` icons. Replace with proper icons for each type (training plan = calendar, race = trophy, casual = shoe). *(RunLogScreen.kt – `RunType.icon`)*
- [ ] `LogRunScreen`: show a Snackbar on error events (currently `ShowError` event is a no-op TODO). *(LogRunScreen.kt – `LaunchedEffect`)*
- [ ] `DurationPickerDialog`: verify scroll selection accuracy on a real device (hard to test on emulator).

## Features (Planned)

- [ ] Calisthenics module – not yet designed or implemented.
- [ ] Unit tests – ViewModels not yet covered.
