# Tasks

## 1. Build setup

- [x] 1.1 Add OkHttp 4.12 (`okhttp`, `mockwebserver`), Room 2.8.x (`room-runtime`, `room-ktx`, `room-compiler`, `room-testing`), Navigation 3 and the KSP plugin to `gradle/libs.versions.toml`, sorted per the Gradle conventions, with a KSP version that works with AGP 9's built-in Kotlin 2.2.10. Verify with `./gradlew help --configuration-cache`, which should succeed
- [x] 1.2 Apply KSP and Room to `:sdk` (`compileOnly` OkHttp, Room runtime/KTX, `ksp(room-compiler)`, schema export to `sdk/schemas/`) and add an empty `@Database` with one placeholder entity. Verify that `./gradlew :sdk:assembleDebug` succeeds and generates the Room `_Impl` class
- [x] 1.3 Create the `:sdk-no-op` module (`com.android.library`, namespace `pt.tiagodiasduarte.appsettings.sdk.noop`, same SDK levels, `explicitApi()`, `compileOnly` OkHttp, detekt) and include it in `settings.gradle.kts`. Verify that `./gradlew :sdk-no-op:assembleDebug` succeeds
- [x] 1.4 Add the binary-compatibility-validator to `:sdk` and `:sdk-no-op` (or the fallback from design.md if it does not support AGP 9 built-in Kotlin), plus a configuration-cache compatible task that fails when the two `.api` dumps differ. Verify that the task passes on matching dumps and fails after a temporary extra public function in one module

## 2. Public API and no-op

- [x] 2.1 Add the public `Retention` enum, the `@HttpLogsDsl` marker, `HttpLogsConfig` (retention, `maxContentLength` with a positive check, `redactDefaultHeaders`, `redactHeaders`, `hideHeaders`) and `AppSettings.httpInterceptor(context, configure)` to `:sdk`, with KDoc, returning a temporary pass-through interceptor. Verify with unit tests that the defaults are 1 day, 250,000 bytes and default redaction on, that the DSL collects header names, and that a non-positive `maxContentLength` throws `IllegalArgumentException`
- [x] 2.2 Mirror the same public declarations in `:sdk-no-op`: `open` does nothing, and `httpInterceptor` evaluates the block and returns a pass-through interceptor. Verify with a `MockWebServer` test that calls pass through unchanged, and that the API comparison task from 1.4 passes
- [x] 2.3 Wire the API comparison task into CI (the `android-test` or `static-analysis` job) and verify that the workflow file runs it

## 3. Storage

- [ ] 3.1 Replace the placeholder with the `http_logs` and `http_log_bodies` entities from design.md (index on `requestedAt`, cascade delete), the header list JSON converter, and `appsettings_http_logs.db` version 1 with destructive migration. Verify that `./gradlew :sdk:assembleDebug` exports `sdk/schemas/.../1.json` with both tables
- [ ] 3.2 Implement the DAO: insert a log with its bodies in one transaction, a `Flow` of list items newer than a cutoff, newest first, without bodies, load one log with its bodies, clear all, prune by age, and keep the newest 500. Verify with Robolectric in-memory Room tests for each query, including that cascade delete removes bodies
- [ ] 3.3 Implement `HttpLogsStore` (process-wide, application context, single-writer scope, injected clock, errors caught and logged): save then prune, prune when the list opens, apply the latest configuration. Verify with tests using a fake clock: logs past 1 hour, 1 day and 1 week are deleted and hidden, the 501st log drops the oldest, and a database error does not throw to the caller

## 4. Capture

- [ ] 4.1 Implement header capture: default redaction of `Authorization`, `Proxy-Authorization`, `Cookie` and `Set-Cookie` to `****`, extra redacted and hidden headers, case-insensitive matching, repeated headers kept. Verify with unit tests for every `http-logs` redaction and hiding scenario
- [ ] 4.2 Implement body capture: text-based content type detection, charset decoding, truncation at `maxContentLength` with the real size and the truncated flag, binary bodies stored as null with their size, and one-shot or duplex request bodies not read. Verify with unit tests for truncation, binary `image/png` and charset cases
- [ ] 4.3 Implement the interceptor: duration until headers, an error log with the exception rethrown unchanged, a teeing response body that writes the log when fully read or closed, and immediate logs for bodyless responses. Replace the temporary pass-through from 2.1. Verify with `MockWebServer` tests: the success log has method, URL, status, protocol and sizes; the app reads the full body over the limit; the server receives the real `Authorization` value; a timeout is logged with no status and still reaches the caller; a second client without the interceptor logs nothing
- [ ] 4.4 Add Kover exclusions for Room's generated `*_Impl` classes and consumer keep rules for `:sdk` if Room's own rules are not enough. Verify that `./gradlew :sdk:koverVerify` passes at 90% line and branch coverage

## 5. Settings screen and navigation

- [ ] 5.1 Turn rows into a sealed `RowUi` (`Info`, `Navigation`), render navigation rows as tappable rows with a chevron, and add the Logs section with an "Open Logs" row after Device, with `appsettings_` strings. Verify with an updated `SettingsViewModelTest` (sections Application, Device, Logs) and a `SettingsScreenTest` that the Logs section and "Open Logs" row render third
- [ ] 5.2 Host the Settings, Logs and LogDetail destinations in `AppSettingsActivity` with Navigation 3. Verify with a Compose UI test that selecting "Open Logs" shows the logs screen and back returns to settings, and update the instrumented `AppSettingsTest` for back from the logs screen
- [ ] 5.3 Update the `settings-screen` related docs in `sdk/README.md` (screen contents) and verify that the README lists the Logs section

## 6. Logs list

- [ ] 6.1 Implement `LogsViewModel` (collects the store's `Flow`, maps to items with `HH:mm:ss` times, durations and decimal sizes, clear action) with a factory from `SdkContainer`. Verify with ViewModel tests using a fake store: newest first, success and error items, empty state, clear
- [ ] 6.2 Implement the stateless `LogsScreen` (top bar with back and clear, list items with method, URL, time, duration, size and status or an error indicator, an empty state message) with `@Preview`s. Verify with Compose UI tests for the success item, error item, empty message, a new log appearing at the top, and selecting an item opening the detail screen

## 7. Log detail

- [ ] 7.1 Implement `LogDetailViewModel(id)` and the JSON pretty-printer (`org.json`, `\/` restored, invalid or truncated JSON shown as stored). Verify with unit tests for valid JSON, invalid JSON, a truncated marker, a binary body message and a missing body
- [ ] 7.2 Implement `LogDetailScreen` with Overview, Request and Response tabs (`PrimaryTabRow`). Overview shows URL, method, status and message, start time, duration, protocol, sizes and error; Request and Response show headers and body. Verify with Compose UI tests for each tab's content and the truncated marker
- [ ] 7.3 Add the copy body action (clipboard) and the share log action (`ACTION_SEND` text with redacted values, no hidden headers, bodies cut at 100,000 characters). Verify with Robolectric tests that the clipboard holds the body and that the share intent text contains `Authorization: ****` and no hidden header

## 8. Sample app and docs

- [ ] 8.1 Add a `qa` build type to `:app` (R8 enabled, debuggable), depend on `:sdk` for debug and QA and on `:sdk-no-op` for release, add the `INTERNET` permission, and add a button that makes a request through an `OkHttpClient` with `AppSettings.httpInterceptor(context) { ... }`. Verify that `./gradlew :app:assembleDebug :app:assembleQa :app:assembleRelease` succeeds
- [ ] 8.2 Document the feature in `sdk/README.md`: variant wiring with `:sdk-no-op`, the DSL options and defaults, redaction, retention and limits, closing response bodies, and the on-device data caveat. Verify that the README snippet matches the sample app's code

## 9. Integration checks

- [ ] 9.1 Install the `qa` build on a device or emulator, make sample requests, and confirm the flow: Settings → Open Logs → list → detail tabs → copy and share. Confirm that the minified build behaves like debug
- [ ] 9.2 Run `./gradlew assembleDebug test :sdk:koverVerify lint detekt` and the API comparison task, and verify that all pass locally and in the PR's CI ("🏁 Finish")

## Workflow follow-up

- Open the PR from `feature/http-logs` into `develop` after the tasks are done.
- Archive the change with `/openspec-archive-change` as the last commit of that PR, syncing `http-logs` and the `settings-screen` delta into `openspec/specs/`.
