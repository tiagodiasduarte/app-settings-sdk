# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

Two modules:

- `:sdk` (`com.android.library`, namespace `pt.tiagodiasduarte.appsettings.sdk`) is the App Settings SDK, an in-app tools screen for my own apps. Its only public API is `AppSettings.open(context)` (`explicitApi()` is on), which launches the internal, non-exported `AppSettingsActivity`. It uses clean-architecture packages (`domain` is pure Kotlin, `data` reads PackageManager/`Build`, `presentation` holds the Compose UI and ViewModel) and manual DI in `di/SdkContainer` (no Hilt, so host apps aren't forced to adopt it). The screen renders an ordered list of sections, currently Application and Device. SDK resources must use the `appsettings_` prefix (`resourcePrefix`). See `sdk/README.md`.
- `:app` (namespace `pt.tiagodiasduarte.appsettings`) is a sample host: a Compose `MainActivity` with a button that calls `AppSettings.open`.

## Commands

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew test                          # JVM unit tests (sdk/src/test, app/src/test), incl. Robolectric/Compose UI tests
./gradlew connectedAndroidTest          # instrumented tests (sdk/src/androidTest, app/src/androidTest), needs a device
./gradlew lint                          # Android lint
./gradlew detekt                        # static analysis (config/detekt/detekt.yml)
./gradlew :sdk:koverHtmlReport :sdk:koverVerify   # SDK coverage report and the 90% line/branch minimum

# single test class / method
./gradlew :sdk:testDebugUnitTest --tests "pt.tiagodiasduarte.appsettings.sdk.presentation.SettingsViewModelTest"
./gradlew :sdk:testDebugUnitTest --tests "pt.tiagodiasduarte.appsettings.sdk.domain.usecase.GetDeviceInfoUseCaseTest"
./gradlew :sdk:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=pt.tiagodiasduarte.appsettings.sdk.AppSettingsTest
```

CI (`.github/workflows/ci.yml`) runs on PRs into `develop` and `main`: build, unit tests with the coverage check, lint and detekt. The `🏁 Finish` job aggregates the result.

Framework-dependent SDK code (`AppSettings.open`, PackageManager/`Build` access, the Compose screen) is unit tested with Robolectric so Kover can measure it; Kover excludes the Activity, `di` and `theme` packages and `@Composable`/`@Preview` functions.

## Build setup

- AGP 9.x with its built-in Kotlin support: there is no separate `org.jetbrains.kotlin.android` plugin. Don't add one unless the AGP version requires it. The Kotlin Compose compiler plugin (`libs.plugins.kotlin.compose`) is pinned to the Kotlin version AGP bundles (check with `./gradlew buildEnvironment`).
- Dependencies and plugins are declared in the version catalog `gradle/libs.versions.toml`; reference them as `libs.*` and don't hardcode coordinates in build scripts.
- `compileSdk` 37 (minor API level 1, required by current AndroidX/Compose), `targetSdk` 36, `minSdk` 24, Java 11 source/target, in both modules.
- Configuration cache is enabled (`gradle.properties`), so build logic must be configuration-cache compatible.
- `settings.gradle.kts` uses `FAIL_ON_PROJECT_REPOS`: add repositories there, not in module build files.
- R8 keep rules go in `<module>/src/main/keepRules/` (AGP merges every file in that directory), not in a `proguard-rules.pro`. Release optimization is currently disabled (`optimization { enable = false }`).
- UI is Jetpack Compose with Material 3 (Compose BOM). There's no AppCompat or Material Components: the XML themes are platform `android:Theme.Material.*.NoActionBar` window themes only. The SDK's `AppSettingsTheme` follows the system dark mode and uses dynamic color on API 31+.

## Local skills

`skills/` holds Android guidance skills (architecture/Hilt, data layer, ViewModel, coroutines, Retrofit, Compose UI, Navigation 3, testing, Gradle convention plugins, build performance, emulator automation scripts, migrations). They are reference material, not part of the build. Read the relevant `skills/<category>/<name>/SKILL.md` before doing work in that area.

## OpenSpec

The project uses OpenSpec (spec-driven schema) for planning changes. Specs live in `openspec/specs/`, in-progress changes in `openspec/changes/` (archived ones in `openspec/changes/archive/`), and project-level constraints for artifacts go in `openspec/config.yaml`. The workflow skills (`openspec-propose`, `-explore`, `-apply-change`, `-update-change`, `-sync-specs`, `-archive-change`) are in `.claude/skills/`, mirrored in `.agents/skills/`.
