# Tasks

## 1. Module scaffolding

- [ ] 1.1 Add the Kotlin JVM plugin (pinned to the catalog `kotlin` version) and `kotlinx-coroutines-core` to the catalog, sorted per the Gradle conventions, and create empty `:cabitools-sdk-domain` and `:cabitools-sdk-application` (JVM, Java 11, `explicitApi()`, detekt, Kover) and `:cabitools-sdk-infrastructure` and `:cabitools-sdk-presentation` (Android library, namespaces `…sdk.infrastructure`/`…sdk.presentation`, `resourcePrefix = "cabitools_"`, `explicitApi()`, detekt, Kover) modules, included in `settings.gradle.kts`. Verify with `./gradlew assemble --configuration-cache`
- [ ] 1.2 Make `:sdk` depend on the four modules with `implementation` and merge their coverage (`kover(project(...))`). Verify that `./gradlew :sdk:koverVerify checkNoOpApi` still passes with the code not yet moved

## 2. Domain and application

- [ ] 2.1 Move `domain/model` and `domain/repository` (with `ContentTypesTest`) to `:cabitools-sdk-domain`, making them `public`. Verify that `:cabitools-sdk-domain:test` passes and the module has no Android dependency
- [ ] 2.2 Move the use cases (with their tests) to `:cabitools-sdk-application` in the `…sdk.application.usecase` package, making them `public`. Verify that `:cabitools-sdk-application:test` passes

## 3. Infrastructure

- [ ] 3.1 Replace `HeaderRules.from(HttpLogsConfig)` with a private mapping in `Cabitools`, and `HttpLogsStore.retention: Retention` with `retentionMillis: Long`, updating their tests. Verify that `./gradlew :sdk:testDebugUnitTest` passes
- [ ] 3.2 Move `data/*` (repositories, `http`, `local`, Room schema) with its unit and instrumented tests to `:cabitools-sdk-infrastructure` in the `…sdk.infrastructure` packages, with Room/KSP there, and make the declarations listed in design.md `public`. Verify that `:cabitools-sdk-infrastructure:testDebugUnitTest` passes and the Room schema is exported from the new module

## 4. Presentation

- [ ] 4.1 Add `PresentationDependencies`, make `CabitoolsNavHost` and the ViewModel factories take dependencies instead of `SdkContainer`, and implement the interface in `SdkContainer`. Verify that `./gradlew :sdk:testDebugUnitTest` passes
- [ ] 4.2 Move `presentation/*` (except `CabitoolsActivity`), resources and tests to `:cabitools-sdk-presentation` with Compose and Navigation 3, using its `R` class and fake `PresentationDependencies` in tests; move `CabitoolsActivity` and its manifest entry into `:sdk`'s root package. Verify that `:cabitools-sdk-presentation:testDebugUnitTest` passes and the merged manifest of `:app` declares `CabitoolsActivity` with `exported=false`

## 5. Facade, docs and checks

- [ ] 5.1 Leave only the public API, `SdkContainer` and `CabitoolsActivity` in `:sdk`, with `HttpLogsConfigTest`, `CabitoolsOpenTest`, `CabitoolsHttpInterceptorTest`, a new `SdkContainer` wiring test and the instrumented `CabitoolsTest`; move Kover exclusions to the modules that own the code. Verify that `./gradlew :sdk:koverVerify` passes at 90% line and branch coverage
- [ ] 5.2 Update `CLAUDE.md` (modules, dependency rules, commands) and the architecture section of `sdk/README.md`. Verify that the documented module graph matches `settings.gradle.kts` and the module dependencies
- [ ] 5.3 Run `./gradlew assembleDebug test :sdk:koverVerify lint detekt checkNoOpApi` and the instrumented tests on an emulator, and verify that all pass and the public API dump is unchanged

## Workflow follow-up

- Open the PR from `refactor/split-sdk-modules` into `develop`, and archive the change as its last commit.
