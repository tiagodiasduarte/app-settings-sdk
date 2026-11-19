# Tasks

## 1. Build setup

- [x] 1.1 Add the `android-library` and `kotlin-compose` plugins, the Compose BOM and libraries, `activity-compose`, the lifecycle Compose artifacts, `kotlinx-coroutines-test` and Compose UI test artifacts to `gradle/libs.versions.toml` (Compose plugin pinned to the Kotlin version AGP 9.3.3 bundles). Verify with `./gradlew help --configuration-cache`, which should succeed
- [x] 1.2 Create the `:sdk` module (`com.android.library`, namespace `pt.tiagodiasduarte.appsettings.sdk`, compileSdk 37.1 / minSdk 24 / Java 11, Compose enabled, `explicitApi()`), include it in `settings.gradle.kts`, and verify that `./gradlew :sdk:assembleDebug` succeeds
- [x] 1.3 Convert `:app` to Compose: remove the AppCompat/Material Components dependencies and themes, add Compose and `implementation(project(":sdk"))`, and verify that `./gradlew :app:assembleDebug` succeeds

## 2. Domain layer

- [x] 2.1 Add the `AppInfo` and `DeviceInfo` models and the `AppInfoRepository`/`DeviceInfoRepository` interfaces in `domain/` (no `android.*` imports). Verify that `:sdk` compiles and grep finds no `android.` import under `domain/`
- [x] 2.2 Implement `GetAppInfoUseCase` and `GetDeviceInfoUseCase`, including the model formatting (manufacturer capitalization and de-duplication, `"<release> (API <sdk>)"`). Verify with JVM unit tests covering the `device-info-section` scenarios (Samsung, Google Pixel duplicate, `15 (API 35)`) and pass-through of the app info, null version name included

## 3. Data layer

- [x] 3.1 Implement `AppInfoRepositoryImpl` using `PackageManager` (API 33+ flags overload, `PackageInfoCompat.getLongVersionCode`, null version name kept as null). Verify with an instrumented test in `:sdk` asserting that the app id equals the test package name and the version code is read without error
- [x] 3.2 Add a `DeviceBuildSource` abstraction over `android.os.Build` and implement `DeviceInfoRepositoryImpl`. Verify with a unit test that the repository maps fake source values into `DeviceInfo`
- [x] 3.3 Add `SdkContainer` wiring the repositories and use cases from `applicationContext`, and verify that `:sdk` compiles with no DI framework dependency

## 4. Presentation layer

- [x] 4.1 Add `SettingsUiState`, `SectionUi` and `InfoRowUi`, plus string resources (section titles, row labels, "Unknown"). Verify that `:sdk` compiles
- [x] 4.2 Implement `SettingsViewModel` (injected dispatcher, `StateFlow<SettingsUiState>`, sections ordered Application then Device, null version name mapped to the placeholder). Verify with unit tests using fake repositories and `StandardTestDispatcher`
- [x] 4.3 Implement `AppSettingsTheme` (Material 3, system dark mode, dynamic color on API 31+) and the stateless `SettingsScreen` (Scaffold + top bar + `LazyColumn` of titled sections with label/value rows), with `@Preview`s. Verify with a Compose UI test that both section titles and the row labels render in order and the list scrolls
- [x] 4.4 Implement the internal `AppSettingsActivity` (edge-to-edge, ViewModel through a factory from `SdkContainer`), declared `exported="false"` in the `:sdk` manifest. Verify that the merged `:app` manifest contains the Activity with `exported=false`

## 5. Public API

- [x] 5.1 Implement the public `AppSettings.open(context)` (explicit intent, `FLAG_ACTIVITY_NEW_TASK` for non-Activity contexts) with KDoc. Verify with an instrumented test that opening from an Activity context and from the Application context both show the screen, and that pressing back returns to the caller
- [x] 5.2 Document integration (dependency line plus `AppSettings.open(context)`) in a `sdk/README.md` and verify that the snippet matches the sample app's actual usage

## 6. Sample app and integration

- [x] 6.1 Add a Compose `MainActivity` launcher in `:app` with an "Open App Settings" button calling `AppSettings.open`. Verify with `./gradlew installDebug` that tapping the button shows the screen with the Application values matching `:app`'s `applicationId`/`versionName`/`versionCode` and the device's model and Android version
- [x] 6.2 Verify the screen survives rotation and a dark mode toggle with the same content, on an emulator
- [x] 6.3 Run `./gradlew test lint` and `./gradlew connectedAndroidTest`, verify they pass, and update CLAUDE.md's project state (new `:sdk` module, Compose now set up)

## Workflow follow-up

- Archive the change once implementation is reviewed.
