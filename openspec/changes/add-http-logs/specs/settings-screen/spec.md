# Spec Delta

## MODIFIED Requirements

### Requirement: Screen shows sections in a fixed order
The settings screen SHALL display a vertically scrollable list of sections, each with a visible title above its content. In this version the sections are, in order: Application, Device, then Logs.

#### Scenario: Initial content
- **WHEN** the settings screen opens
- **THEN** the Application section is shown first, the Device section second and the Logs section third, each with its title

#### Scenario: Content taller than the screen
- **WHEN** the sections do not fit on the screen
- **THEN** the user can scroll to reach all of them

## ADDED Requirements

### Requirement: Sections can contain a row that opens another screen
A section SHALL be able to contain a navigation row: a row with a label and no value that opens another SDK screen when selected. Pressing back on that screen SHALL return to the settings screen.

#### Scenario: Open Logs
- **GIVEN** the settings screen is showing
- **WHEN** the user selects the "Open Logs" row in the Logs section
- **THEN** the logs list screen opens

#### Scenario: Back from the logs list
- **GIVEN** the logs list screen was opened from the settings screen
- **WHEN** the user presses back
- **THEN** the settings screen is shown again

### Requirement: Entry point compiles in production builds
The entry point SHALL also exist in the production no-op library with the same signature, so host app code that opens the settings screen compiles in every build. In a production build the call SHALL do nothing.

#### Scenario: Open in a production build
- **GIVEN** a host app production build that depends on the no-op library
- **WHEN** the app calls the entry point
- **THEN** nothing is shown and the app keeps running normally
