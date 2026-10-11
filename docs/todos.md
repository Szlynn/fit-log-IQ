# TODOs & Known Issues

Tracked items that need attention in future sessions.
Format: `- [ ] description *(file:line if relevant)*`

---

## UI / Polish

- [ ] Run type icons in `RunLogScreen` are all placeholder `DateRange` icons. Replace with proper icons for each type (training plan = calendar, race = trophy, casual = shoe). *(RunLogScreen.kt – `RunType.icon`)*
- [ ] `LogRunScreen`: show a Snackbar on error events (currently `ShowError` event is a no-op TODO). *(LogRunScreen.kt – `LaunchedEffect`)*
- [ ] `DurationPickerDialog`: Hungarian button labels ("Mégse"). Decide: keep Hungarian for UI strings (consistent with strings.xml policy) or move to `strings.xml`. *(DurationPickerDialog.kt)*

## Architecture / Code Quality

## Features (Deferred)

- [ ] Training Plans screen – not yet implemented.
- [ ] Stats & Race Log screen – not yet implemented.
- [ ] Bottom navigation bar – no global nav between modules yet.
