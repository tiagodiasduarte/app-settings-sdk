# Proposal

## Why

All SDK code lives in one `:sdk` module, so the clean-architecture layers are only folders: nothing stops presentation code from reaching into the database, or Android code from creeping into the domain. Splitting the layers into Gradle modules makes the build enforce the boundaries, and keeps each layer small enough to change on its own as more tools are added.

## What Changes

- Add four modules, one per layer:
  - `:cabitools-sdk-domain` (pure Kotlin/JVM): models, `ContentTypes` and repository interfaces.
  - `:cabitools-sdk-application` (pure Kotlin/JVM): use cases, depending only on domain.
  - `:cabitools-sdk-infrastructure` (Android library): PackageManager/`Build` repositories, the HTTP logs interceptor and capture code, and the Room store, depending only on domain.
  - `:cabitools-sdk-presentation` (Android library): Compose screens, ViewModels, Navigation 3 and resources, depending on domain and application.
- `:sdk` becomes a thin facade and composition root: it keeps the public API (`Cabitools`, `HttpLogsConfig`, `Retention`, `HttpLogsDsl`), the manual DI (`SdkContainer`) and `CabitoolsActivity`, and depends on the four modules with `implementation`. Host apps keep depending on `:sdk` only, and `:sdk-no-op` is unchanged.
- Remove the two dependencies that point the wrong way: ViewModel factories and the navigation host take their dependencies (through a `PresentationDependencies` interface) instead of `SdkContainer`, and infrastructure takes plain values (header name sets, retention in milliseconds) instead of the public `HttpLogsConfig`/`Retention` types.
- Declarations used across modules become `public` in their module (`explicitApi()` everywhere); everything else stays `internal`.
- Coverage is merged across the five modules and still verified at 90% by `:sdk:koverVerify`; tests move with their code.

No behavior changes, and the public API is unchanged.

Out of scope: publishing the modules separately, renaming the `:sdk`/`:sdk-no-op` modules, and changes to host apps (they keep depending on `:sdk`).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
<!-- None: no observable behavior changes. The change sets skip_specs: true. -->

## Impact

- **Build:** four new modules in `settings.gradle.kts`; the Kotlin JVM plugin (pinned to AGP's Kotlin 2.2.10) and `kotlinx-coroutines-core` for the two JVM modules; Room/KSP move to infrastructure, Compose and Navigation 3 to presentation.
- **Code:** every SDK source file moves to a layer module. Use cases move from `domain.usecase` to the `application` package, `data` becomes `infrastructure`, and resources move to presentation (its `R` class gets the presentation namespace).
- **Public API:** unchanged (`checkNoOpApi` still compares `:sdk` and `:sdk-no-op`). Types that become `public` inside the layer modules are not on host compile classpaths, because `:sdk` depends on the layers with `implementation`.
- **Host apps:** no change; they still depend on `:sdk`, and composite builds still substitute `:sdk`.
