# Spec Delta

## Purpose

Records the HTTP calls a host app makes through its OkHttp client and lets testers and developers browse them in the tools screen, so debugging and QA can see exactly what was sent and received.

## ADDED Requirements

### Requirement: Host app records calls by adding the SDK interceptor
The SDK SHALL provide, through its public entry point, an OkHttp interceptor that the host app adds to the OkHttp client its Retrofit instance uses. Every call that goes through that client and finishes SHALL be recorded as one log.

#### Scenario: Successful call is recorded
- **GIVEN** a host app whose OkHttp client includes the SDK interceptor
- **WHEN** the app makes a GET request that returns status 200
- **THEN** a log for that call is stored, with its method, URL, status, start time and duration

#### Scenario: Calls through other clients are not recorded
- **GIVEN** a host app with a second OkHttp client that does not include the SDK interceptor
- **WHEN** the app makes a request through that second client
- **THEN** no log is stored for it

### Requirement: Recording does not change the call
The interceptor SHALL pass requests and responses through unchanged. The host app SHALL receive the same status, headers and full response body it would receive without the interceptor, even when the stored copy of the body is truncated or redacted.

#### Scenario: App reads the full response body
- **GIVEN** a response body longer than the maximum stored body length
- **WHEN** the call finishes
- **THEN** the host app reads the complete body, and only the stored copy is truncated

#### Scenario: Redaction does not affect the request sent
- **GIVEN** a request with an `Authorization` header
- **WHEN** the call is made
- **THEN** the server receives the real header value, and only the stored copy shows `****`

### Requirement: Failed calls are recorded with their error
A call that ends without a response, for example because of a timeout or a connection failure, SHALL be recorded with the error description and no status. The error SHALL still reach the host app as it would without the interceptor.

#### Scenario: Timeout
- **GIVEN** a server that does not answer before the client timeout
- **WHEN** the app makes a request to it
- **THEN** a log is stored with the error description and no status code
- **AND** the app receives the same timeout error

### Requirement: Recording is configured in code with defaults
The integrating developer SHALL configure the interceptor in code when creating it: retention period, maximum stored body length, headers to redact and headers to hide. Options that are not set SHALL use their defaults: 1 day retention, 250,000 bytes, and the default redacted headers.

#### Scenario: No configuration
- **GIVEN** the developer creates the interceptor without setting any option
- **WHEN** calls are recorded
- **THEN** logs are kept for 1 day, bodies are stored up to 250,000 bytes, and the default sensitive headers are redacted

#### Scenario: Custom configuration
- **GIVEN** the developer sets a 1 hour retention and a maximum body length of 10,000 bytes
- **WHEN** calls are recorded
- **THEN** logs older than 1 hour are deleted and stored bodies are at most 10,000 bytes

### Requirement: Retention period is one of three options
The retention period SHALL be one of: 1 hour, 1 day or 1 week. Logs older than the configured period SHALL be deleted and SHALL NOT be shown.

#### Scenario: Expired logs are removed
- **GIVEN** a 1 hour retention and a log recorded 2 hours ago
- **WHEN** a new call is recorded or the logs list is opened
- **THEN** the 2 hour old log is not shown and is deleted

### Requirement: At most 500 logs are kept
The SDK SHALL keep at most 500 logs. When a new log would exceed that number, the oldest logs SHALL be deleted first.

#### Scenario: Oldest log is dropped
- **GIVEN** 500 stored logs within the retention period
- **WHEN** a new call is recorded
- **THEN** 500 logs remain, the new log is included, and the oldest log is deleted

### Requirement: Logs persist across app restarts
Logs SHALL be stored on the device, so they remain available after the host app process is restarted, until the retention period or the 500 log limit removes them.

#### Scenario: App is restarted
- **GIVEN** logs recorded within the retention period
- **WHEN** the host app is closed and opened again
- **THEN** the logs list still shows them

### Requirement: Long bodies are truncated when stored
Request and response bodies SHALL each be stored up to the maximum stored body length. A longer body SHALL be stored truncated to that length and marked as truncated, while its real size is still recorded.

#### Scenario: Response body over the limit
- **GIVEN** a maximum stored body length of 250,000 bytes
- **WHEN** a response with a 400,000 byte body is recorded
- **THEN** the stored body has its first 250,000 bytes, is marked as truncated, and its size is shown as 400,000 bytes

### Requirement: Binary bodies are not stored as text
A body whose content type is not text-based (for example an image or protobuf) SHALL NOT be stored as text. Its size SHALL still be recorded, and the detail screen SHALL show that the binary body is not displayed.

#### Scenario: Image response
- **WHEN** a response with content type `image/png` and a 12,400 byte body is recorded
- **THEN** no body text is stored, and the Response tab shows that a 12.4 kB binary body is not displayed

### Requirement: Sensitive headers are redacted by default
Before a log is saved, the values of the `Authorization`, `Proxy-Authorization`, `Cookie` and `Set-Cookie` headers SHALL be replaced by `****`, unless the developer turns the default redaction off. Header names SHALL be matched case-insensitively.

#### Scenario: Authorization header
- **GIVEN** the default configuration
- **WHEN** a request with header `authorization: Bearer abc123` is recorded
- **THEN** the stored log shows the header with the value `****`, and `abc123` is not stored anywhere

### Requirement: Developer can redact additional headers
The developer SHALL be able to list more header names whose values are replaced by `****` before saving, in addition to the defaults.

#### Scenario: Custom API key header
- **GIVEN** the developer configured `X-Api-Key` to be redacted
- **WHEN** a request with header `X-Api-Key: secret` is recorded
- **THEN** the stored log shows `X-Api-Key` with the value `****`

### Requirement: Developer can hide headers
The developer SHALL be able to list header names that are not stored at all. Hidden headers SHALL NOT appear in any log, copy or share output.

#### Scenario: Hidden tracing header
- **GIVEN** the developer configured `X-Internal-Trace` to be hidden
- **WHEN** a request with that header is recorded
- **THEN** the stored log does not contain `X-Internal-Trace`

### Requirement: Logs list shows the recorded calls
The logs screen SHALL list the stored logs newest first. Each item SHALL show the HTTP method, the URL, the time of day the call started, its duration, the response body size and the status, or an error indicator when there was no response.

#### Scenario: Successful call in the list
- **GIVEN** a recorded `POST` to `https://api.example.com/orders` at 14:31:55 that took 340 ms, returned status 201 and a 356 byte body
- **WHEN** the user opens the logs screen
- **THEN** its item shows `POST`, the URL, `14:31:55`, `340 ms`, `356 B` and `201`

#### Scenario: Failed call in the list
- **GIVEN** a recorded call that failed with a timeout
- **WHEN** the user opens the logs screen
- **THEN** its item shows an error indicator instead of a status and no response size

#### Scenario: No logs
- **GIVEN** no stored logs
- **WHEN** the user opens the logs screen
- **THEN** the screen shows a message that no HTTP calls have been recorded yet

#### Scenario: New call while the list is open
- **GIVEN** the logs screen is showing
- **WHEN** the host app finishes a new call
- **THEN** the new log appears at the top of the list without reopening the screen

### Requirement: User can clear all logs
The logs screen SHALL offer an action that deletes all stored logs and their bodies.

#### Scenario: Clear logs
- **GIVEN** stored logs
- **WHEN** the user clears the logs
- **THEN** the list is empty and the logs are no longer stored on the device

### Requirement: Log detail has Overview, Request and Response tabs
Selecting a log SHALL open a detail screen with three tabs: Overview, Request and Response. The Overview tab SHALL show the URL, method, status and message, start time, duration, protocol, request and response sizes, and the error, if any.

#### Scenario: Open a log
- **GIVEN** a recorded `GET` that returned `200 OK`
- **WHEN** the user selects it in the logs list
- **THEN** the detail screen opens on the Overview tab with the URL, `GET`, `200 OK`, start time, duration, protocol and sizes

### Requirement: Request and Response tabs show headers and body
The Request tab SHALL show the stored request headers and body. The Response tab SHALL show the stored response headers and body. A truncated body SHALL be marked as truncated, and a missing body SHALL be shown as empty.

#### Scenario: Truncated response
- **GIVEN** a log whose response body was truncated
- **WHEN** the user opens the Response tab
- **THEN** the tab shows the response headers, the stored part of the body, and that the body was truncated

### Requirement: JSON bodies are pretty-printed
Bodies with a JSON content type SHALL be shown pretty-printed with indentation. A body that is not valid JSON, including a truncated JSON body, SHALL be shown as stored.

#### Scenario: Valid JSON body
- **GIVEN** a response with content type `application/json` and body `{"id":42,"status":"ok"}`
- **WHEN** the user opens the Response tab
- **THEN** the body is shown on separate indented lines

#### Scenario: Invalid JSON body
- **GIVEN** a response with content type `application/json` whose body is not valid JSON
- **WHEN** the user opens the Response tab
- **THEN** the body is shown exactly as stored

### Requirement: Bodies can be copied
The Request and Response tabs SHALL each offer an action that copies the shown body to the clipboard.

#### Scenario: Copy response body
- **WHEN** the user copies the body on the Response tab
- **THEN** the clipboard contains the response body as shown

### Requirement: A log can be shared as text
The detail screen SHALL offer an action that shares the whole log as text through the Android share sheet, including the method, URL, status, headers and bodies. The shared text SHALL contain redacted values and SHALL NOT contain hidden headers.

#### Scenario: Share a log
- **GIVEN** a log with a redacted `Authorization` header
- **WHEN** the user shares the log
- **THEN** the share sheet opens with the log as text, showing `Authorization: ****`

### Requirement: Production builds record nothing
The SDK SHALL provide a no-op library with the same public API for production builds. With it, the interceptor SHALL pass calls through without recording or storing anything, and host app code that creates and configures the interceptor SHALL compile unchanged.

#### Scenario: Production build
- **GIVEN** a host app production build that depends on the no-op library
- **WHEN** the app makes HTTP calls through a client with the interceptor
- **THEN** the calls behave as without the interceptor and nothing is stored on the device
