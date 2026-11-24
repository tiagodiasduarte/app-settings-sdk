# Design

## Context

See proposal.md - Why. Today `:sdk` has one public API, `AppSettings.open(context)`. It opens a non-exported `AppSettingsActivity` that renders `SettingsUiState(sections: List<SectionUi>)`, where every row is an `InfoRowUi(label, value)`. Wiring is manual (`SdkContainer`), `explicitApi()` is on, and resources use the `appsettings_` prefix. The SDK has no network, database or navigation dependency yet. Kotlin is pinned to the version AGP 9 bundles (2.2.10), and there is no KSP. CI enforces 90% line and branch coverage on `:sdk` with Kover, and detekt and lint run on every PR.

Host apps use Retrofit on OkHttp, and include the SDK in debug and QA builds (QA builds are distributed with Firebase App Distribution and may be minified), never in production.

## Goals / Non-Goals

**Goals:**
- Recording never changes what the host app sends or receives, and adds no noticeable latency to its calls.
- Production builds contain no recording code, database or screens, while host app code stays the same in every variant.
- The public API can gain new options later without breaking host apps that are already compiled.
- The SDK forces as few dependency versions as possible onto host apps.

**Non-Goals:**
- A general logging framework. Only HTTP calls through OkHttp are covered.
- Database migrations between SDK versions (see Decisions - Room).
- Querying or filtering logs in the UI. The list is newest first, with no search.

## Decisions

### Public API: a Kotlin DSL on `AppSettings`

```kotlin
public fun AppSettings.httpInterceptor(
    context: Context,
    configure: HttpLogsConfig.() -> Unit = {},
): okhttp3.Interceptor

@HttpLogsDsl
public class HttpLogsConfig {
    public var retention: Retention = Retention.ONE_DAY
    public var maxContentLength: Long = 250_000L
    public var redactDefaultHeaders: Boolean = true
    public fun redactHeaders(vararg names: String)
    public fun hideHeaders(vararg names: String)
}

public enum class Retention { ONE_HOUR, ONE_DAY, ONE_WEEK }
```

It is a member of `AppSettings`, so host apps find it next to `open`. `HttpLogsConfig` is a class with properties and functions, not a data class or a function with default parameters, so new options can be added without changing any existing signature. `maxContentLength` must be positive, otherwise the call throws `IllegalArgumentException`.

Alternatives: a function with default parameters breaks binary compatibility with every new option, and a Java-style builder is verbose for Kotlin-only hosts.

### `:sdk-no-op`: same public API, no behavior

A separate library module with the same package and the same public declarations (`AppSettings`, `HttpLogsConfig`, `Retention`, `HttpLogsDsl`). `open` does nothing, and `httpInterceptor` evaluates the configuration block and returns `Interceptor { it.proceed(it.request()) }`. It has no Room, Compose or Activity, so production builds stay clean. Its namespace is `pt.tiagodiasduarte.appsettings.sdk.noop`, so its `R` class does not clash. Host apps depend on exactly one of the two modules per variant.

The two APIs must not drift apart. The Kotlin binary-compatibility-validator plugin generates an `.api` dump for each module, and a configuration-cache compatible Gradle task, which runs in CI, fails when the two dumps differ.

Alternatives: a runtime flag in a single artifact would ship the database and screens to production. Asking hosts to wrap calls in `if (BuildConfig.DEBUG)` blocks pushes the problem onto every app.

### OkHttp is `compileOnly`

`:sdk` and `:sdk-no-op` compile against OkHttp 4.12 as `compileOnly`, and use only APIs that are the same in OkHttp 4.x and 5.x. At runtime the host's own OkHttp version is used. QA and production builds then run the same OkHttp version, and the SDK never upgrades it. Every host already has OkHttp through Retrofit.

Alternative: `api(okhttp)` would make debug and QA builds resolve the SDK's OkHttp version while production uses the host's, so QA would test a different HTTP client than production.

### Capture: an application interceptor that tees the response body

The interceptor runs as an application interceptor (`addInterceptor`). It sees the request as the app built it and the response after OkHttp decompressed it, so sizes are decoded sizes. For each call:

1. Copy the request headers, applying redaction and hiding, and read the request body into a buffer, keeping up to `maxContentLength` bytes. One-shot and duplex request bodies are not read, and their body is recorded as not captured.
2. Measure `durationMs` from just before `chain.proceed` until the response headers arrive.
3. On an `IOException`, record the log with the error and rethrow the same exception.
4. On a response, replace its body with one that wraps the original source. As the app reads it, the wrapper copies up to `maxContentLength` bytes and counts the total size. The log is written when the body is fully read or closed, so the app always gets the full body and the log has the real size. Responses without a body are recorded immediately.

A body is captured as text only when its content type is text-based (`text/*`, JSON, XML, form-urlencoded, or a `+json`/`+xml` suffix) or has no content type. It is decoded with the charset from the content type, falling back to UTF-8.

Alternative: `response.peekBody(maxContentLength)` is simpler, but cannot report the real size of a body over the limit and buffers it twice.

### Writing logs off the network thread

The interceptor hands each finished log to an SDK-owned `CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))`. One writer coroutine inserts the log and its bodies in a single transaction, then prunes. Calls never wait for the database, and inserts and pruning never run concurrently. A database failure is caught and logged with `android.util.Log`, never thrown into the host's call.

The scope lives in a process-wide `HttpLogsStore` singleton, created on the first `httpInterceptor` call from the application context. Several OkHttp clients, or several interceptor instances, share one database. If interceptors are created with different configurations, the retention and limits of the most recently created interceptor apply.

### Room schema

Two tables, as agreed in the explore session:

```
http_logs                                   http_log_bodies
  id                 INTEGER PK auto  <---    logId         INTEGER PK, FK -> http_logs.id ON DELETE CASCADE
  method             TEXT                     requestBody   TEXT null
  url                TEXT                     responseBody  TEXT null
  requestedAt        INTEGER (epoch ms, indexed)
  durationMs         INTEGER
  protocol           TEXT null
  responseCode       INTEGER null    -- null: no response, see error
  responseMessage    TEXT null
  error              TEXT null
  requestHeaders     TEXT            -- JSON list of {name, value}, already redacted
  requestContentType TEXT null
  requestBodySize    INTEGER null    -- real size, null if unknown or no body
  requestBodyTruncated  INTEGER (bool)
  responseHeaders    TEXT
  responseContentType TEXT null
  responseBodySize   INTEGER null
  responseBodyTruncated INTEGER (bool)
```

- Headers are a JSON list rather than a map, so repeated headers such as `Set-Cookie` are kept. They are encoded with `org.json`, which is part of Android, so no serialization library is added.
- Bodies live in their own table. The list query reads only `http_logs` and the detail screen loads the bodies of one log. Deleting from `http_logs` cascades, so pruning and clearing are a single statement each.
- A binary or uncaptured body is stored as `null` with its size set. The UI tells "binary" apart from "empty" with the content type and size.
- The database is `appsettings_http_logs.db`, at version 1, with `fallbackToDestructiveMigration(dropAllTables = true)`. The logs expire within a week at most, so losing them on an SDK update is acceptable, and migrations are not worth their cost. The schema is still exported to `sdk/schemas/` so changes show up in review.
- Room 2.8.x with KSP. The DAO uses `suspend` functions for writes and `Flow` for the list, so new logs appear while the list is open.

### Retention and the 500 log limit

After every insert, in the same writer coroutine:

```sql
DELETE FROM http_logs WHERE requestedAt < :now - :retentionMs;
DELETE FROM http_logs WHERE id NOT IN (SELECT id FROM http_logs ORDER BY requestedAt DESC, id DESC LIMIT 500);
```

The list query also filters by `requestedAt >= now - retention`, so expired logs are never shown, even when no call has been made for a while. Opening the logs list also runs the age prune. Time comes from an injected clock, so tests can control it.

### Screens and navigation

`AppSettingsActivity` becomes the host for three destinations, using Navigation 3 (`androidx.navigation3`) with a back stack of keys: `Settings`, `Logs` and `LogDetail(id)`. Navigation 3 keeps the back stack as plain Compose state, so it is light and easy to test. Back from `Logs` returns to `Settings`, and back from `Settings` closes the Activity, as today.

- **Settings:** rows become a sealed type, `RowUi.Info(label, value)` and `RowUi.Navigation(label, destination)`. The ViewModel builds the sections in the order Application, Device, Logs. Logs has one `RowUi.Navigation("Open Logs", Logs)` row.
- **Logs (`LogsScreen`):** `LogsViewModel` collects the DAO's `Flow`, maps rows to items, and exposes a clear action. Times are formatted as `HH:mm:ss` in the device time zone. Sizes are formatted in decimal units (B, kB, MB). A failed call shows an error indicator in place of the status, and no size.
- **Detail (`LogDetailScreen`):** `LogDetailViewModel(id)` loads the log and its bodies once. A `PrimaryTabRow` switches between Overview, Request and Response. JSON bodies are pretty-printed with `org.json` (`JSONObject`/`JSONArray.toString(2)`, with `\/` turned back into `/`). If parsing fails, the body is shown as stored.
- **Copy** uses `ClipboardManager`. **Share** builds a plain-text report (request line, status, headers and bodies, with redacted values and no hidden headers) and sends it with `Intent.ACTION_SEND`. To stay below the binder transaction limit, each body in the shared text is cut to 100,000 characters, with a "body truncated" note.

Each new ViewModel is created through a factory from `SdkContainer`, as `SettingsViewModel` is today. All new strings use the `appsettings_` prefix.

### Sample app

`:app` gets a `qa` build type (R8 minified, debuggable) next to debug and release. Debug and QA depend on `:sdk`, release depends on `:sdk-no-op`, and the screen gets a button that makes a request through an `OkHttpClient` with the interceptor. This shows host apps the intended wiring, and the QA build checks that minified builds still work. The sample needs the `INTERNET` permission. The SDK itself does not.

### Testing

- The interceptor is tested on the JVM with OkHttp `MockWebServer`: success, error, truncation, binary bodies, redaction, hiding, unchanged bodies for the app, and the log written when the body is closed.
- The DAO, retention and 500 log pruning are tested with Robolectric and an in-memory Room database, with a fake clock.
- ViewModels are tested with fakes and `StandardTestDispatcher`, and screens with Compose UI tests on Robolectric, following the test conventions.
- `:sdk-no-op` gets a test that its interceptor passes calls through.
- Kover excludes Room's generated `*_Impl` classes, in addition to the current exclusions.

## Risks / Trade-offs

- [The KSP version must work with AGP 9's built-in Kotlin 2.2.10] → The first task adds KSP and Room and runs a build before any feature code. If no KSP version fits, the fallback is to align the Kotlin version the way the interview project does, as a separate decision.
- [The binary-compatibility-validator plugin may not support AGP 9 built-in Kotlin] → The same first task checks it. The fallback is Kotlin's built-in ABI validation, or comparing the two modules' public declarations with a small custom task.
- [OkHttp 4 and 5 API differences] → Use only APIs present in both (`Interceptor`, `Request`, `Response`, `ResponseBody`, `okio.Buffer`/`ForwardingSource`). Tests compile against 4.12, the oldest supported version.
- [If an app never closes a response body, that call is never logged] → OkHttp requires bodies to be closed, and Retrofit always closes them. This is documented in the SDK README.
- [Data written before redaction rules change stays as it was] → Redaction applies when saving. Changing the rules affects new logs only, and old ones expire with retention or can be cleared.
- [Debug and QA devices store unredacted payloads, for example personal data in bodies] → This is accepted for internal builds. Production uses `:sdk-no-op`, and the README tells hosts to redact or hide sensitive headers.
- [Room and the database add to debug and QA builds] → Accepted: debug and QA only.
- [Several interceptors with different configurations] → Most recent wins, as documented above. This is rare and not worth per-client databases.

## Migration Plan

This is additive for existing host apps: `AppSettings.open` is unchanged, and until a host adds the interceptor, the Logs section just shows an empty list. To adopt it, hosts:

1. Switch production variants to `:sdk-no-op`.
2. Add `AppSettings.httpInterceptor(context) { ... }` to their OkHttp client.

Rollback is removing the interceptor line. The database is dropped with the app's data, or by an SDK version that removes the feature.
