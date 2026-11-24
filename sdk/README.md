# App Settings SDK

An in-app tools screen for my own apps. It shows these sections, in this order:

- **Application**: version name, version code and app id of the host app
- **Device**: manufacturer and model, plus Android version and API level
- **Logs**: an **Open Logs** row that opens the HTTP logs recorded by the SDK interceptor

## Integration

Add the dependency. Which build variants include it is up to each app:

```kotlin
dependencies {
    implementation(project(":sdk"))
}
```

Open the screen from anywhere you have a `Context`:

```kotlin
val context = LocalContext.current
Button(onClick = { AppSettings.open(context) }) {
    Text("Open App Settings")
}
```

`AppSettings.open` works with an Activity or the Application context. The screen is an
SDK-owned, non-exported Activity that is merged into the host manifest automatically. It
needs no theme, DI framework or manifest entries from the host app.

## Requirements

- minSdk 24
- compileSdk 37.1 or later in the host app (required by the AndroidX versions the SDK uses)

## Architecture

```
presentation  AppSettingsActivity (Navigation 3 host), SettingsScreen (Compose), SettingsViewModel
     |
     v
domain        AppInfo, DeviceInfo, use cases, repository interfaces (pure Kotlin)
     ^
     |
data          PackageManager / android.os.Build backed repositories
di            SdkContainer (manual wiring)
```

Only `AppSettings` is public (`explicitApi()` is enabled); everything else is `internal`.
