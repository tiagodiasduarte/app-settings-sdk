---
paths:
  - "**/src/test/**"
  - "**/src/androidTest/**"
---

# Test Conventions

Every test follows the Given-When-Then structure, in its name and in its body.

## Name

- JVM tests (`src/test`): `` `given <context> when <action> then <expected outcome>` ``
- Instrumented tests (`src/androidTest`): `given<Context>_when<Action>_then<ExpectedOutcome>`.
  Method names with spaces only dex from API 30, and `minSdk` is 24.

## Body

Three labelled comment blocks, `// Given`, `// When` and `// Then`, separated by a blank line:

```kotlin
@Test
fun `given a manufacturer missing from the model when device info is read then the model is prefixed`() {
    // Given
    val useCase = GetDeviceInfoUseCase(FakeDeviceInfoRepository(manufacturer = "samsung", model = "SM-S918B"))

    // When
    val details = useCase()

    // Then
    assertEquals("Samsung SM-S918B", details.model)
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
