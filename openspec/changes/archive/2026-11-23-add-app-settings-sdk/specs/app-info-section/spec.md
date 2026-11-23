# Spec Delta

## Purpose

Shows identifying information about the host app (the app that includes the SDK), so testers and developers can see exactly which build is installed.

## ADDED Requirements

### Requirement: Application section shows the host app's information
The Application section SHALL show the version name, version code and application id of the host app that is running the SDK, not of the SDK library itself.

#### Scenario: Values match the installed host app
- **GIVEN** a host app installed with application id `com.example.host`, version name `2.3.0` and version code `230`
- **WHEN** the user opens the settings screen
- **THEN** the Application section shows App id `com.example.host`, Version name `2.3.0` and Version code `230`

#### Scenario: Application id suffix from build variant
- **GIVEN** a host build whose application id includes a variant suffix (for example `com.example.host.debug`)
- **WHEN** the user opens the settings screen
- **THEN** the App id row shows the full id, including the suffix

### Requirement: Version code supports large values
The version code SHALL be shown as a whole number, including values that do not fit in 32 bits, on every supported Android version.

#### Scenario: Large version code
- **GIVEN** a host app whose version code is larger than 2147483647
- **WHEN** the user opens the settings screen on a device running Android 9 or later
- **THEN** the Version code row shows the full value

### Requirement: Missing version name is shown explicitly
If the host app has no version name, the Version name row SHALL still be shown, with a placeholder value instead of being blank or crashing.

#### Scenario: No version name
- **GIVEN** a host app built without a version name
- **WHEN** the user opens the settings screen
- **THEN** the Version name row shows a placeholder such as "Unknown"
