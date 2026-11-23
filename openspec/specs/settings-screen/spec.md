# settings-screen Specification

## Purpose

Lets a host app open the SDK's tools screen with a single call. The screen presents the SDK's tools as an ordered list of titled sections.

## Requirements

### Requirement: Host app opens the settings screen with a single call
The SDK SHALL expose one public entry point that, given an Android `Context`, opens the settings screen as a full-screen UI owned by the SDK. The host app SHALL NOT need to declare any Activity, theme or dependency-injection setup to use it.

#### Scenario: Open from an Activity context
- **WHEN** the host app calls the entry point with an Activity context
- **THEN** the settings screen is shown on top of the host app's current screen

#### Scenario: Open from a non-Activity context
- **WHEN** the host app calls the entry point with the Application context
- **THEN** the settings screen is shown without crashing

#### Scenario: Return to the host app
- **WHEN** the settings screen is showing and the user presses back
- **THEN** the screen closes and the host app's previous screen is shown again

### Requirement: Settings screen is not reachable from other apps
Other apps SHALL NOT be able to launch the settings screen. Only the host app's own process can open it, through the public entry point.

#### Scenario: External launch attempt
- **WHEN** another app sends an explicit intent targeting the SDK's settings screen component
- **THEN** the system refuses to start it

### Requirement: Screen shows sections in a fixed order
The settings screen SHALL display a vertically scrollable list of sections, each with a visible title above its content. In this version the sections are, in order: Application, then Device.

#### Scenario: Initial content
- **WHEN** the settings screen opens
- **THEN** the Application section is shown first and the Device section second, each with its title

#### Scenario: Content taller than the screen
- **WHEN** the sections do not fit on the screen
- **THEN** the user can scroll to reach all of them

### Requirement: Information rows show a label and a value
Each information item in a section SHALL be shown as a row with a human-readable label and its value.

#### Scenario: Row rendering
- **WHEN** a section contains an item such as the application id
- **THEN** a row shows the label (for example "App id") together with the value

### Requirement: Screen survives configuration changes
The settings screen SHALL keep showing the same content after configuration changes such as rotation or dark mode toggles, without crashing.

#### Scenario: Rotate device
- **WHEN** the settings screen is showing and the device is rotated
- **THEN** the screen shows the same sections and values
