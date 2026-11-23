# device-info-section Specification

## Purpose

Shows which device and Android version the host app is running on, so bug reports and QA sessions can identify the test environment.

## Requirements

### Requirement: Device section shows the device model
The Device section SHALL show a Model row that combines the device manufacturer and model name. If the model name already starts with the manufacturer, the manufacturer SHALL NOT be repeated.

#### Scenario: Manufacturer and model differ
- **GIVEN** a device reporting manufacturer `samsung` and model `SM-S918B`
- **WHEN** the user opens the settings screen
- **THEN** the Model row shows `Samsung SM-S918B`

#### Scenario: Model already includes manufacturer
- **GIVEN** a device reporting manufacturer `Google` and model `Google Pixel 8`
- **WHEN** the user opens the settings screen
- **THEN** the Model row shows `Google Pixel 8`, with the manufacturer only once

### Requirement: Device section shows the Android version
The Device section SHALL show an Android version row containing both the user-facing Android release and the API level.

#### Scenario: Release and API level
- **GIVEN** a device running Android release `15` with API level `35`
- **WHEN** the user opens the settings screen
- **THEN** the Android version row shows `15 (API 35)`
