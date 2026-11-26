# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

Three modules:

- `:sdk` (`com.android.library`, namespace `pt.tiagodiasduarte.cabitools.sdk`) is Cabitools (named after *cabidela*), an in-app debug and QA tools screen for my own apps. Its public API is the `Cabitools` object (`explicitApi()` is on): `Cabitools.open(context)` launches the internal, non-exported `CabitoolsActivity`, and `Cabitools.httpInterceptor(context) { ... }` returns the HTTP logs OkHttp interceptor, configured with `HttpLogsConfig`/`Retention`. It uses clean-architecture packages (`domain` is pure Kotlin, `data` reads PackageManager/`Build`, `presentation` holds the Compose UI and ViewModel) and manual DI in `di/SdkContainer` (no Hilt, so host apps aren't forced to adopt it). The screen renders an ordered list of sections, currently Application, Device and Logs; Logs opens the HTTP logs screens (Navigation 3), backed by a Room database. SDK resources must use the `cabitools_` prefix (`resourcePrefix`). See `sdk/README.md`.
- `:sdk-no-op` (namespace `pt.tiagodiasduarte.cabitools.sdk.noop`) has the same public API as `:sdk` (same package and declarations) and does nothing. Host apps use it in production builds. Any public API change must be made in both modules; `./gradlew checkNoOpApi` fails when they differ.
- `:app` (namespace and application id `pt.tiagodiasduarte.cabitools.sample`, "Cabitools Sample") is a sample host: a Compose `MainActivity` that opens Cabitools and makes HTTP calls through the interceptor. Debug and the minified `qa` build type use `:sdk`; release uses `:sdk-no-op`.

## Commands

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew test                          # JVM unit tests (sdk/src/test, app/src/test), incl. Robolectric/Compose UI tests
./gradlew connectedAndroidTest          # instrumented tests (sdk/src/androidTest, app/src/androidTest), needs a device
./gradlew lint                          # Android lint
./gradlew detekt                        # static analysis (config/detekt/detekt.yml)
./gradlew :sdk:koverHtmlReport :sdk:koverVerify   # SDK coverage report and the 90% line/branch minimum
./gradlew checkNoOpApi                  # :sdk and :sdk-no-op must expose the same public API

# single test class / method
./gradlew :sdk:testDebugUnitTest --tests "pt.tiagodiasduarte.cabitools.sdk.presentation.SettingsViewModelTest"
./gradlew :sdk:testDebugUnitTest --tests "pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetDeviceInfoUseCaseTest"
./gradlew :sdk:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=pt.tiagodiasduarte.cabitools.sdk.CabitoolsTest
```

CI (`.github/workflows/ci.yml`) runs on PRs into `develop` and `main`: build, unit tests with the coverage check, lint, detekt and `checkNoOpApi`. The `🏁 Finish` job aggregates the result.

Framework-dependent SDK code (`Cabitools.open`, PackageManager/`Build` access, the Compose screen) is unit tested with Robolectric so Kover can measure it; Kover excludes the Activity, `di` and `theme` packages and `@Composable`/`@Preview` functions.

## Build setup

- AGP 9.x with its built-in Kotlin support: there is no separate `org.jetbrains.kotlin.android` plugin. Don't add one unless the AGP version requires it. The Kotlin Compose compiler plugin (`libs.plugins.kotlin.compose`) is pinned to the Kotlin version AGP bundles (check with `./gradlew buildEnvironment`).
- Dependencies and plugins are declared in the version catalog `gradle/libs.versions.toml`; reference them as `libs.*` and don't hardcode coordinates in build scripts.
- `compileSdk` 37 (minor API level 1, required by current AndroidX/Compose), `targetSdk` 36, `minSdk` 24, Java 11 source/target, in both modules.
- Configuration cache is enabled (`gradle.properties`), so build logic must be configuration-cache compatible.
- `settings.gradle.kts` uses `FAIL_ON_PROJECT_REPOS`: add repositories there, not in module build files.
- R8 keep rules go in `<module>/src/main/keepRules/` (AGP merges every file in that directory), not in a `proguard-rules.pro`. Release optimization is currently disabled (`optimization { enable = false }`).
- UI is Jetpack Compose with Material 3 (Compose BOM). There's no AppCompat or Material Components: the XML themes are platform `android:Theme.Material.*.NoActionBar` window themes only. The SDK's `CabitoolsTheme` follows the system dark mode and uses dynamic color on API 31+.

## Local skills

`skills/` holds Android guidance skills (architecture/Hilt, data layer, ViewModel, coroutines, Retrofit, Compose UI, Navigation 3, testing, Gradle convention plugins, build performance, emulator automation scripts, migrations). They are reference material, not part of the build. Read the relevant `skills/<category>/<name>/SKILL.md` before doing work in that area.

## OpenSpec

The project uses OpenSpec (spec-driven schema) for planning changes. Specs live in `openspec/specs/`, in-progress changes in `openspec/changes/` (archived ones in `openspec/changes/archive/`), and project-level constraints for artifacts go in `openspec/config.yaml`. The workflow skills (`openspec-propose`, `-explore`, `-apply-change`, `-update-change`, `-sync-specs`, `-archive-change`) are in `.claude/skills/`, mirrored in `.agents/skills/`.
