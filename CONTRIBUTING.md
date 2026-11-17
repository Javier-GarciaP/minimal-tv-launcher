# Contributing

Thanks for taking a look.

## Scope

This launcher exists for low-end Android TV sets. Please keep changes inside that scope:

- **No new dependencies.** The APK is 160 KB and the target device has 1.4 GB of RAM.
  Every dependency is paid for in memory on a set that has none to spare.
- **No network access.** The app does not declare `INTERNET` and should not start to.
- **No analytics, no crash reporting, no ads.**
- **Spanish first.** The interface is in Spanish. Other locales can be added as
  `values-xx/strings.xml`, but do not change the existing text in `values/strings.xml`.

## Development

```bash
export ANDROID_SDK_ROOT=~/.cache/android-sdk
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Build with JDK 17 or newer.

## Style

- Kotlin, official style, 4 spaces.
- Comments in English, in the imperative and only where the code is not obvious.
- Keep the Android APIs out of the list logic: `AppListBuilder` and `SettingsStore` are
  plain Kotlin so they stay unit testable.
- Every behaviour change needs a test in `app/src/test/`.

## Commit messages

Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
One logical change per commit.

## Before opening a pull request

- [ ] `./gradlew :app:assembleRelease` builds without warnings
- [ ] `./gradlew :app:testDebugUnitTest` passes
- [ ] The change was tested on a real device, not only in the emulator
- [ ] `git status` shows no build artefacts and no keystore
