# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

Freshly generated Android Studio project (single `:app` module, package/namespace `pt.tiagodiasduarte.appsettings`). There is no application code yet: the manifest declares no activities, and the only Kotlin sources are the template `ExampleUnitTest` and `ExampleInstrumentedTest`. Despite the name "App Settings Sdk", there is no library module yet; it is configured as `com.android.application`.

## Commands

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew test                          # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest          # instrumented tests (app/src/androidTest), needs a device
./gradlew lint                          # Android lint

# single test class / method
./gradlew :app:testDebugUnitTest --tests "pt.tiagodiasduarte.appsettings.ExampleUnitTest"
./gradlew :app:testDebugUnitTest --tests "pt.tiagodiasduarte.appsettings.ExampleUnitTest.addition_isCorrect"
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=pt.tiagodiasduarte.appsettings.ExampleInstrumentedTest
```

## Build setup

- AGP 9.x with its built-in Kotlin support: there is no separate `org.jetbrains.kotlin.android` plugin. Don't add one unless the AGP version requires it.
- Dependencies and plugins are declared in the version catalog `gradle/libs.versions.toml`; reference them as `libs.*` and don't hardcode coordinates in build scripts.
- `compileSdk` 36 (minor API level 1), `targetSdk` 36, `minSdk` 24, Java 11 source/target.
- Configuration cache is enabled (`gradle.properties`), so build logic must be configuration-cache compatible.
- `settings.gradle.kts` uses `FAIL_ON_PROJECT_REPOS`: add repositories there, not in module build files.
- R8 keep rules go in `app/src/main/keepRules/` (AGP merges every file in that directory), not in a `proguard-rules.pro`. Release optimization is currently disabled (`optimization { enable = false }`).
- UI is currently View-based (AppCompat + Material Components themes in `res/values/themes.xml`); Compose is not set up.

## Local skills

`skills/` holds Android guidance skills (architecture/Hilt, data layer, ViewModel, coroutines, Retrofit, Compose UI, Navigation 3, testing, Gradle convention plugins, build performance, emulator automation scripts, migrations). They are reference material, not part of the build. Read the relevant `skills/<category>/<name>/SKILL.md` before doing work in that area.

## OpenSpec

The project uses OpenSpec (spec-driven schema) for planning changes. Specs live in `openspec/specs/`, in-progress changes in `openspec/changes/` (archived ones in `openspec/changes/archive/`), and project-level constraints for artifacts go in `openspec/config.yaml`. The workflow skills (`openspec-propose`, `-explore`, `-apply-change`, `-update-change`, `-sync-specs`, `-archive-change`) are in `.claude/skills/`, mirrored in `.agents/skills/`.
