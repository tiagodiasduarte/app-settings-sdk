---
paths:
  - "**/src/test/**"
  - "**/src/androidTest/**"
---

# Test Conventions

Every test follows the Given-When-Then structure, in its name and in its body.

## Name

JVM tests (`src/test`) use a backtick name with spaces, never underscores or camelCase:

`` `given <context> when <action> then <expected outcome>` ``

```kotlin
// ✅ Do
fun `given a blank manufacturer when the use case runs then the model is used on its own`()

// ❌ Don't: underscores
fun given_a_blank_manufacturer_when_the_use_case_runs_then_the_model_is_used_on_its_own()

// ❌ Don't: camelCase in a JVM test
fun givenABlankManufacturer_whenTheUseCaseRuns_thenTheModelIsUsedOnItsOwn()

// ❌ Don't: missing given/when/then
fun `blank manufacturer falls back to model`()
```

Instrumented tests (`src/androidTest`) are the only exception. D8 rejects method names with spaces below
DEX version 040 (API 30), and `minSdk` is 24, so the build fails. Use camelCase, with an underscore only
between the given, when and then parts:

```kotlin
// ✅ Do (androidTest only)
fun givenTheApplicationContext_whenOpenIsCalled_thenTheSettingsScreenIsShown()

// ❌ Don't: spaces in androidTest (fails with "Space characters in SimpleName are not allowed")
fun `given the application context when open is called then the settings screen is shown`()
```

## Body

Three labelled comment blocks, `// Given`, `// When` and `// Then`, separated by a blank line:

```kotlin
// ✅ Do
@Test
fun `given a missing version name when the use case runs then the version name stays null`() {
    // Given
    val info = AppInfo("com.example.host", null, 1L)

    // When
    val result = useCase(info)()

    // Then
    assertNull(result.versionName)
}

// ❌ Don't: no blocks, and the action is inlined in the assertion
@Test
fun `given a missing version name when the use case runs then the version name stays null`() {
    assertNull(useCase(AppInfo("com.example.host", null, 1L))().versionName)
}
```

- Always write all three blocks.
- `// Given` holds the setup specific to this test.
- `// When` holds exactly one action under test.
- `// Then` holds only assertions.
- Shared setup (rules, `@Before`, test dispatcher) stays at class level, outside the blocks.

## Test doubles and coroutines

- Prefer fakes over mocks: hand-rolled `Fake<Dependency>` classes, or an `object :` implementation for a one-off.
  There is no mocking library.
- Place a test in the `src/test` (or `src/androidTest`) of the module that owns the code, mirroring its package.
- Use `runTest` with `StandardTestDispatcher` for coroutine tests, and `Dispatchers.setMain`/`resetMain` for ViewModels.
- Use Robolectric (`@RunWith(AndroidJUnit4::class)`) for JVM tests that need the Android framework, so Kover can measure them.
  Keep `src/androidTest` for end-to-end behavior that needs a real device.
