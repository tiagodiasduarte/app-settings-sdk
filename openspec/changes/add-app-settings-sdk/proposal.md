# Proposal

## Why

My apps need one shared in-app tools screen for debugging and QA (for example: logs, environment switching, app and device details). Today each app would have to build it separately. This change creates the SDK library that hosts the screen, with two read-only sections to start: Application and Device. Later tools will be added as more sections.

## What Changes

- Add a new `:sdk` Android library module, built with Jetpack Compose and Material 3 and organised in clean-architecture layers (domain, data, presentation) with manual dependency injection.
- Add a public entry point `AppSettings.open(context)`. It launches an SDK-owned, non-exported Activity that shows the settings screen.
- The settings screen renders an ordered list of sections, so future tools can be added as new sections.
- Add an **Application** section showing the host app's version name, version code and application id.
- Add a **Device** section showing the device manufacturer and model, and the Android version and API level.
- Turn `:app` into a Compose sample host that depends on `:sdk` and opens the screen from a button.
- Set up Compose in the build (Compose compiler plugin, Compose BOM, Material 3) through the version catalog.

Out of scope: deciding which build variants include the SDK (each host app handles that), logs, environment switching, and publishing the library to a Maven repository.

## Capabilities

### New Capabilities
- `settings-screen`: the public entry point that opens the SDK screen, and how the screen presents its ordered sections.
- `app-info-section`: the Application section and the host app information it shows.
- `device-info-section`: the Device section and the device and OS information it shows.

### Modified Capabilities
<!-- None: no existing specs. -->

## Impact

- **New module** `:sdk` (`com.android.library`), added to `settings.gradle.kts`.
- **`:app`** moves from a View/AppCompat template to Compose, depends on `:sdk`, and gets a launcher Activity.
- **Dependencies** (in `gradle/libs.versions.toml`): Compose BOM, Compose UI/Material 3/tooling, `activity-compose`, `lifecycle-viewmodel-compose`/`lifecycle-runtime-compose`, the Kotlin Compose compiler plugin, `android-library` plugin, and coroutines-test for unit tests.
- **Public API surface** for host apps: `AppSettings.open(context: Context)`. Everything else in `:sdk` is `internal`.
- No Hilt or other DI framework is required of host apps.
