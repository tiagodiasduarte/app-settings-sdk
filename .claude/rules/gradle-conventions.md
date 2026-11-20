---
paths:
  - "**/*.gradle.kts"
  - "gradle/libs.versions.toml"
---

# Gradle Conventions

## Version catalog (`gradle/libs.versions.toml`)

- Keep each section (`[versions]`, `[libraries]`, `[plugins]`) sorted alphabetically by key, ignoring case.
- A comment belongs to the entry below it and moves with that entry when sorting.

## Build scripts (`*.gradle.kts`)

- Keep `plugins {}` sorted alphabetically.
- Group `dependencies {}` by configuration, with a blank line between groups, in this order:
  `implementation`, `debugImplementation`, `testImplementation`, `androidTestImplementation`.
- Within a group, put `project(...)` first, then sort alphabetically by alias.
  A `platform(...)` BOM sorts by the alias it wraps.
