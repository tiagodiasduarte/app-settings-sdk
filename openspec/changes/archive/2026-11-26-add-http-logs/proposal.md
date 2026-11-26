# Proposal

## Why

Debugging and QA of my apps needs to show exactly which HTTP calls an app made and what came back, without attaching a proxy or reading Logcat. The first change planned logs as the next tool for the shared tools screen. This change adds HTTP logs: host apps plug an SDK interceptor into the OkHttp client their Retrofit uses, and testers browse the recorded calls in the tools screen.

## What Changes

- Add an SDK-owned OkHttp interceptor, created with `AppSettings.httpInterceptor(context) { ... }`. Its Kotlin DSL lets the integrating developer set:
  - the retention period: 1 hour, 1 day (default) or 1 week
  - the maximum stored body length, which defaults to 250,000 bytes
  - headers to redact (value replaced by `****`)
  - headers to hide (not stored)
- Record every finished call (response or error) in a Room database inside the SDK, keeping at most 500 logs and deleting logs older than the retention period.
- Redact sensitive headers before a log is saved. `Authorization`, `Proxy-Authorization`, `Cookie` and `Set-Cookie` are redacted by default.
- Add a **Logs** section to the settings screen, after Device, with an **Open Logs** row that opens the logs list.
- Add a **logs list** screen (newest first: method, URL, time, duration, response size and status) with an action to clear all logs.
- Add a **log detail** screen with **Overview**, **Request** and **Response** tabs. JSON bodies are pretty-printed, and bodies and whole logs can be copied and shared.
- Add a `:sdk-no-op` library with the same public API, for production builds. Its interceptor passes calls through and records nothing, and `AppSettings.open` does nothing. Host apps use `:sdk` in debug and QA builds and `:sdk-no-op` in production.
- Extend the `:app` sample to make HTTP calls through the interceptor, so the feature can be tried end to end.

Out of scope: in-progress calls (a log appears when the call finishes), editing or replaying requests, logs from HTTP clients other than OkHttp, choosing retention or header rules in the UI at runtime, other log types (for example Logcat), and publishing the libraries to a Maven repository.

## Capabilities

### New Capabilities
- `http-logs`: capturing HTTP calls through the SDK interceptor, its configuration (retention, body limit, header redaction and hiding), storage limits, the logs list and log detail screens, and the no-op behavior in production builds.

### Modified Capabilities
- `settings-screen`: the fixed section order changes from Application and Device to Application, Device and Logs. Sections can now contain a row that opens another screen, and the entry point must also compile against the production no-op library.

## Impact

- **Public API** for host apps: new `AppSettings.httpInterceptor(context, configure)` and its configuration types (retention options and the DSL). This must be identical in `:sdk` and `:sdk-no-op`.
- **New module** `:sdk-no-op` (`com.android.library`), added to `settings.gradle.kts`.
- **Dependencies** (in `gradle/libs.versions.toml`): OkHttp (the SDK's first network dependency), Room (runtime, KTX, compiler) with the KSP plugin matching AGP's bundled Kotlin version, and a Compose navigation library for the SDK's new screens.
- **Host apps** get Room and OkHttp in their dependency graph in debug and QA builds. They must switch to `:sdk-no-op` in production builds to keep traffic recording out of production.
- **Data on device**: debug and QA builds store HTTP traffic in an SDK database. Redaction runs before saving, but anything not redacted is readable on the device.
- **R8**: QA builds may be minified, so `:sdk` needs consumer keep rules for Room and its screens.
- **CI**: a check that `:sdk` and `:sdk-no-op` expose the same public API.
