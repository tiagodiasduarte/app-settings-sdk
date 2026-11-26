# Design

## Context

See proposal.md - Why. Today `:sdk` holds everything under `pt.tiagodiasduarte.cabitools.sdk`, in the packages `domain` (models, repository interfaces, use cases), `data` (repositories, `http` capture, `local` Room store), `presentation` (Compose, ViewModels, navigation, theme), `di` (`SdkContainer`) and the root package (public `Cabitools`, `HttpLogsConfig`, `Retention`, `HttpLogsDsl`). Mapping each file's imports shows two dependencies that point the wrong way for a layered split:

1. Presentation depends on DI: the ViewModel factories take `SdkContainer`, and `CabitoolsActivity` creates it.
2. Infrastructure depends on the public API types: `HeaderRules.from(HttpLogsConfig)` and `HttpLogsStore.retention: Retention`.

Everything else already points inward (towards domain).

## Goals / Non-Goals

**Goals:**
- The build enforces the layers: domain and application can't use Android, infrastructure can't use presentation, and nothing depends on the facade.
- Host apps and the public API don't change.
- The same checks keep passing: tests, the 90% coverage minimum (merged), lint, detekt, `checkNoOpApi`.

**Non-Goals:**
- Splitting by feature (app info, device info, HTTP logs) instead of by layer.
- Publishing each module as its own artifact.

## Decisions

### Module graph

```
                 :sdk  (facade: public API, SdkContainer, CabitoolsActivity)
       +-----------+-----------+-----------+
       v           v           v           v
 :cabitools-sdk-  :cabitools-sdk-  :cabitools-sdk-  :cabitools-sdk-
  presentation    infrastructure    application        domain
   (Android)        (Android)         (JVM)            (JVM)
     |   |              |                |
     |   +--------------|--------> application
     +------------------+--------> domain <-------------+
```

- `domain` depends on nothing but `kotlinx-coroutines-core` (`HttpLogsRepository` exposes a `Flow`).
- `application` depends on `domain`.
- `infrastructure` depends on `domain` (not on application: it implements domain's repository interfaces).
- `presentation` depends on `domain` and `application`.
- `:sdk` depends on all four with `implementation`, so their public types never reach host compile classpaths.

Alternative: presentation as the entry point (no facade). It would make presentation depend on infrastructure, which is exactly the coupling the split should prevent.

### What goes where

| Today | Module | Package |
|---|---|---|
| `domain/model/*`, `domain/repository/*` | domain | `…sdk.domain.model`, `…sdk.domain.repository` (unchanged) |
| `domain/usecase/*` | application | `…sdk.application.usecase` |
| `data/*` (repositories, `http`, `local`) | infrastructure | `…sdk.infrastructure`, `…sdk.infrastructure.http`, `…sdk.infrastructure.local` |
| `presentation/*` except the Activity | presentation | `…sdk.presentation…` (unchanged) |
| `Cabitools`, `HttpLogsConfig`, `Retention`, `HttpLogsDsl`, `di/SdkContainer`, `presentation/CabitoolsActivity` | `:sdk` | root and `…sdk.di` (unchanged); the Activity moves to `…sdk` |

Android namespaces: `…sdk.infrastructure` and `…sdk.presentation`. Presentation code then imports `pt.tiagodiasduarte.cabitools.sdk.presentation.R`. All Android modules keep `resourcePrefix = "cabitools_"`; only presentation has resources, plus the window theme the Activity uses.

### Fixing the two wrong-way dependencies

**Presentation without DI.** Presentation declares what its screens need:

```kotlin
public interface PresentationDependencies {
    public val getAppInfo: GetAppInfoUseCase
    public val getDeviceInfo: GetDeviceInfoUseCase
    public val httpLogsRepository: HttpLogsRepository
}

@Composable
public fun CabitoolsNavHost(dependencies: PresentationDependencies)
```

ViewModel factories take those dependencies directly (for example `LogsViewModel.factory(repository)`). `SdkContainer` implements `PresentationDependencies`. `CabitoolsActivity` moves to `:sdk`, the composition root, next to the other Android entry point (`Cabitools.open`), and its manifest entry moves with it. That also keeps Activity recreation after process death safe: the Activity builds `SdkContainer` itself, so there is no global holder that could be empty.

Alternative: a global dependency holder in presentation set by `:sdk`. After process death the Activity can be recreated before anything sets it.

**Infrastructure without the public config types.** `HeaderRules` keeps only its plain constructor (`redactDefaultHeaders`, `redactedHeaders`, `hiddenHeaders`); `HeaderRules.from(HttpLogsConfig)` moves to `Cabitools` as a private mapping. `HttpLogsStore.retention: Retention` becomes `retentionMillis: Long` (default one day), and `Cabitools.httpInterceptor` sets it from `config.retention.millis`. `HttpLogsConfig`, `Retention` and their `internal` members stay in `:sdk`, where `checkNoOpApi` compares them with `:sdk-no-op`.

### Visibility

All modules use `explicitApi()`. Declarations used by another module become `public`:

- domain: models, `ContentTypes`, repository interfaces.
- application: the use cases.
- infrastructure: the repository implementations, `AndroidDeviceBuildSource`, `HttpLogsStore` (its `get`, `retentionMillis` and `save`), `HttpLogsInterceptor`, `HeaderRules`.
- presentation: `PresentationDependencies`, `CabitoolsNavHost`, `CabitoolsTheme`.

Everything else (DAO, entities, capture helpers, ViewModels, screens, formatting) stays `internal`. Test-only accessors (`HttpLogsStore.saveAsync`) stay `internal`, with their tests in the same module.

### Build setup

- The JVM modules apply `org.jetbrains.kotlin.jvm`, pinned to the `kotlin` version in the catalog (AGP's 2.2.10), with Java 11, detekt and Kover. The first task builds them before anything moves, to confirm the plugin coexists with AGP's built-in Kotlin.
- infrastructure takes Room, KSP and the schema export (the directory follows the database class's new package); presentation takes Compose, Navigation 3, lifecycle and the theme; `:sdk` keeps OkHttp `compileOnly` for the public `Interceptor` type, as do infrastructure.
- Kover is applied to all five modules; `:sdk` adds `kover(project(...))` dependencies on the four, so `:sdk:koverVerify` checks the merged report at 90%, and CI does not change. Exclusions move with the code (Activity and `di` in `:sdk`, theme and Composables in presentation, Room `*_Impl` in infrastructure).
- `checkNoOpApi` still reads `:sdk`'s compiled classes; the public API is still declared there.

### Tests

Tests move with the code they cover:

| Module | Tests |
|---|---|
| domain | `ContentTypesTest` |
| application | use case tests |
| infrastructure | repository, `http` and `local` tests (Robolectric, MockWebServer); instrumented `AppInfoRepositoryImplTest` |
| presentation | screen, ViewModel, navigation, formatting and sharing tests, with fake `PresentationDependencies` |
| `:sdk` | `HttpLogsConfigTest`, `CabitoolsOpenTest`, `CabitoolsHttpInterceptorTest`, a `SdkContainer` wiring test; instrumented `CabitoolsTest` with `HostActivity` |

`SettingsViewModelFactoryTest` becomes the `SdkContainer` wiring test in `:sdk`. `HeaderRulesTest` and `HttpLogsInterceptorTest` build `HeaderRules` with its constructor; the mapping from `HttpLogsConfig` gets its own test in `:sdk`.

## Risks / Trade-offs

- [The Kotlin JVM plugin clashes with AGP's built-in Kotlin] → Checked in the first task with empty modules. Fallback: make domain and application Android libraries and keep the "no Android imports" rule with a detekt `ForbiddenImport` rule.
- [Merged Kover coverage drops below 90% because a module has untested glue] → Each task runs `:sdk:koverVerify`; untested glue gets tests, not exclusions.
- [More public declarations than before] → They are public only inside the SDK's module graph; `implementation` keeps them off host compile classpaths, and `checkNoOpApi` still guards the host-facing API.
- [Build time grows with five modules] → Small modules build in parallel and are cached; the configuration cache stays on.

## Migration Plan

Nothing for host apps. The modules are new and internal to the SDK build; hosts keep `:sdk` (or its composite build substitution).
