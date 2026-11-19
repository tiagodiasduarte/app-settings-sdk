# Design

## Context

The repo is a fresh AGP 9.3.3 template: a single `:app` module (`com.android.application`, View/AppCompat themes), no Compose, and no library module. AGP 9 has built-in Kotlin, so there is no `org.jetbrains.kotlin.android` plugin. Compose still needs the Kotlin Compose compiler plugin. All dependencies go through `gradle/libs.versions.toml`. The configuration cache is on. Repositories are declared only in `settings.gradle.kts`. Every host app already uses Compose. See proposal.md for motivation and scope; behavior is in `specs/`.

## Goals / Non-Goals

**Goals:**
- A clean-architecture layering that later tools (logs, environments) can extend without restructuring.
- The smallest possible public API, so that internals can change freely.
- Nothing forced on host apps (no DI framework, no theme, no manifest entries).

**Non-Goals:**
- Splitting `:sdk` into per-layer or per-feature Gradle modules (deferred until there are more tools).
- A plugin API for host-defined sections.
- Publishing to Maven, and binary-compatibility tooling.

## Decisions

### 1. One `:sdk` library module, layered by package

```
pt.tiagodiasduarte.appsettings.sdk
 +-- AppSettings.kt                 public object: open(context)
 +-- di/        SdkContainer        manual wiring from applicationContext
 +-- domain/
 |    +-- model/    AppInfo, DeviceInfo
 |    +-- repository/ AppInfoRepository, DeviceInfoRepository (interfaces)
 |    +-- usecase/  GetAppInfoUseCase, GetDeviceInfoUseCase
 +-- data/
 |    +-- AppInfoRepositoryImpl     (PackageManager)
 |    +-- DeviceInfoRepositoryImpl  (android.os.Build, via a small wrapper)
 +-- presentation/
      +-- AppSettingsActivity       internal, exported=false
      +-- SettingsViewModel         exposes StateFlow<SettingsUiState>
      +-- SettingsScreen.kt         stateless composables
      +-- model/ SettingsUiState, SectionUi, InfoRowUi
      +-- theme/ AppSettingsTheme
```

Dependency direction: presentation -> domain <- data. Domain is pure Kotlin with no `android.*` imports. Android data such as `PackageInfo` and `Build` fields is mapped in the data layer.

*Alternative:* separate `:sdk:domain`/`:sdk:data`/`:sdk:ui` modules. These give compiler-enforced boundaries, but with two read-only sections the cost is mostly Gradle boilerplate. We can revisit when feature modules make sense.

### 2. Public API is a single `AppSettings.open(context)`

It starts `AppSettingsActivity` with an explicit intent and adds `FLAG_ACTIVITY_NEW_TASK` when the context is not an Activity. Everything else is `internal`, and `explicitApi()` is enabled in `:sdk` so new public symbols are deliberate. The Activity is declared in the SDK's manifest with `android:exported="false"`, and manifest merging adds it to the host.

*Alternative:* a public `@Composable`, rejected during exploration (option A) to keep hosts isolated from SDK navigation and theming.

### 3. Manual DI through `SdkContainer`

`SdkContainer(applicationContext)` constructs the repositories and use cases. The Activity gets the `SettingsViewModel` through a `ViewModelProvider.Factory` (`viewModelFactory { initializer { ... } }`) that pulls from the container. No global state is kept beyond what the Activity creates.

*Alternative:* Hilt/Koin. This would force a DI framework on every host app.

### 4. Reading host app info

`context.packageManager.getPackageInfo(context.packageName, 0)`, using the `PackageInfoFlags` overload on API 33+. Version code comes from `PackageInfoCompat.getLongVersionCode` (minSdk 24 predates `longVersionCode`). A null `versionName` maps to `null` in the domain, and the UI layer replaces it with a "Unknown" string resource, so the placeholder text stays in presentation.

### 5. Device info formatting

The data layer returns raw `manufacturer`, `model`, `release` and `sdkInt`. A domain function (or the use case) produces the display model: it capitalizes the manufacturer and drops it when `model` already starts with it (case-insensitive). The Android version is formatted as `"<release> (API <sdkInt>)"`. Keeping this in domain makes the scenarios in `device-info-section` testable on the JVM with fake inputs. `Build` is read through a small `DeviceBuildSource` interface so tests don't depend on static fields.

### 6. Sections as UI models

`SettingsUiState` holds `sections: List<SectionUi>`, where `SectionUi(titleRes, rows: List<InfoRowUi(labelRes, value)>)`. The ViewModel maps domain models to sections in the fixed order Application, Device. The screen renders the list in a `LazyColumn`. Future tools add new `SectionUi` variants (for example action or navigation sections) as a sealed type, which keeps the order rule in one place.

Loading is synchronous and cheap, but the ViewModel still exposes a `StateFlow`, with the use cases called on `Dispatchers.Default` through an injected dispatcher. Later sections (logs) will be asynchronous, and tests use `StandardTestDispatcher`.

### 7. Theming

`AppSettingsTheme` wraps Material 3 `MaterialTheme`. It follows the system dark mode and uses dynamic color on API 31+, falling back to a default light/dark scheme. The Activity uses `enableEdgeToEdge()` and a `Scaffold` with a top app bar titled "App Settings".

### 8. Build setup

- Catalog additions: `android-library` plugin (same `agp` version), `kotlin-compose` plugin (`org.jetbrains.kotlin.plugin.compose`, at the Kotlin version bundled with AGP 9.3.3), Compose BOM, `ui`, `ui-tooling(-preview)`, `material3`, `activity-compose`, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, `kotlinx-coroutines-test`, and `ui-test-junit4`/`ui-test-manifest` for screen tests.
- compileSdk moves from 36.1 to **37.1** in both modules: current stable AndroidX (Compose BOM 2026.11.01, lifecycle 2.11, core 1.19) requires it at AAR-metadata check. targetSdk stays 36 and minSdk 24, so runtime behavior doesn't change.
- `:sdk`: `namespace = "pt.tiagodiasduarte.appsettings.sdk"`, the same compileSdk/minSdk/Java 11 settings as `:app`, `resourcePrefix = "appsettings_"`, `buildFeatures { compose = true }`. Consumer keep rules aren't needed for now (no reflection).
- `:app`: drop AppCompat/Material Components, add Compose and `implementation(project(":sdk"))`, plus a `MainActivity` with an "Open App Settings" button and a Compose-only theme.

## Risks / Trade-offs

- [Packages don't enforce layer boundaries the way modules do] -> keep domain free of `android.*` imports and review imports in code review; split modules when the SDK grows.
- [The Kotlin Compose plugin version must match the Kotlin bundled with AGP 9] -> pin it in the catalog to the AGP's Kotlin version, and verify with `./gradlew assembleDebug` on the first task.
- [Dynamic color makes the screen look different from the host app] -> accepted: the screen is a standalone tool. Easy to change later in `AppSettingsTheme`.
- [Manifest merge adds an Activity to every host variant that includes the SDK] -> intended. Variant inclusion is the host's responsibility (see proposal, out of scope).

## Migration Plan

Not applicable: nothing is published or deployed yet. `:app` template code is replaced in place.
