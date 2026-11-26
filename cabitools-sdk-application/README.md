# Cabitools SDK

Named after *cabidela*, my favourite dish: a bit of everything you need, in one pot.

An in-app tools screen for my own apps. It shows these sections, in this order:

- **Application**: version name, version code and app id of the host app
- **Device**: manufacturer and model, plus Android version and API level
- **Logs**: an **Open Logs** row that opens the HTTP logs recorded by the SDK interceptor

## Integration

Use `:sdk` in debug and QA builds, and `:sdk-no-op` in production builds. `:sdk-no-op` has the
same public API and does nothing, so your code compiles unchanged in every build, and production
never records traffic or ships the screen:

```kotlin
dependencies {
    // The SDK in debug and QA builds; the no-op, with the same API, in production.
    debugImplementation(project(":sdk"))
    "qaImplementation"(project(":sdk"))
    releaseImplementation(project(":sdk-no-op"))
}
```

A custom build type such as `qa` needs `matchingFallbacks += listOf("release")`, because the
libraries only have `debug` and `release` variants.

Open the screen from anywhere you have a `Context`:

```kotlin
val context = LocalContext.current
Button(onClick = { Cabitools.open(context) }) {
    Text("Open Cabitools")
}
```

`Cabitools.open` works with an Activity or the Application context. The screen is an
SDK-owned, non-exported Activity that is merged into the host manifest automatically. It
needs no theme, DI framework or manifest entries from the host app.

## HTTP logs

Add the SDK interceptor to the OkHttp client your Retrofit instance uses. Every call through
that client is recorded and shown under **Logs > Open Logs**:

```kotlin
private fun sampleClient(context: Context): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(
        Cabitools.httpInterceptor(context) {
            retention = Retention.ONE_DAY
            maxContentLength = MAX_STORED_BODY_BYTES
            redactHeaders("X-Api-Key")
            hideHeaders("X-Internal-Trace")
        },
    )
    .build()
```

All options are optional:

| Option | Default | Effect |
|---|---|---|
| `retention` | `Retention.ONE_DAY` | `ONE_HOUR`, `ONE_DAY` or `ONE_WEEK`. Older logs are deleted. |
| `maxContentLength` | `250_000L` | Bytes stored per request and per response body. Longer bodies are stored truncated and marked; the real size is still shown. Must be positive. |
| `redactHeaders(...)` | none | Extra headers whose value is stored as `****`. |
| `hideHeaders(...)` | none | Headers that are not stored at all. |
| `redactDefaultHeaders` | `true` | Stores `Authorization`, `Proxy-Authorization`, `Cookie` and `Set-Cookie` as `****`. |

Header names match case-insensitively. Rules apply **before** a log is saved: the real values
are sent to the server, but never stored. Changing the rules only affects new logs.

What to know:

- At most **500** logs are kept, newest first. The oldest are deleted when there are more,
  and logs older than the retention period are deleted and hidden.
- The interceptor never changes the call: the app gets the same status, headers and full body.
- A call is logged when its response body is fully read or closed. Retrofit always closes
  bodies; if you use OkHttp directly, close every `Response`, as OkHttp requires.
- Binary bodies (images, protobuf...) are recorded by size only. Bodies the app asked to receive
  compressed (its own `Accept-Encoding`) are not stored as text.
- Logs are stored on the device, in the SDK database `cabitools_http_logs.db`. Anything you
  don't redact or hide (request and response bodies included) is readable on that device and
  in shared logs. Keep `:sdk` out of production builds.
- One database serves the whole process. If you create several interceptors with different
  options, the retention of the most recently created one applies.
- OkHttp is a `compileOnly` dependency: the SDK uses your app's OkHttp version (4.12 or later).

## Requirements

- minSdk 24
- compileSdk 37.1 or later in the host app (required by the AndroidX versions the SDK uses)

## Architecture

```
presentation  CabitoolsActivity (Navigation 3 host), Settings, Logs and LogDetail screens and ViewModels
     |
     v
domain        AppInfo, DeviceInfo, HttpLog models, use cases, repository interfaces (pure Kotlin)
     ^
     |
data          PackageManager / android.os.Build repositories, HTTP logs interceptor and Room store
di            SdkContainer (manual wiring)
```

Only `Cabitools` and the HTTP logs configuration (`HttpLogsConfig`, `Retention`, `HttpLogsDsl`)
are public (`explicitApi()` is enabled); everything else is `internal`. `:sdk-no-op` must expose
exactly the same public API: `./gradlew checkNoOpApi` fails when the two differ.
