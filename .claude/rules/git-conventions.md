# Git Conventions

Source of truth for commit messages, branch names, PR titles, and PR descriptions across this repo.
Follow these conventions for every branch, commit and PR.


## Branch naming

Format:

`<type>/<short-description>`

**Types:**

| Type       | Use for                              |
|------------|--------------------------------------|
| `feature`  | New functionality                    |
| `fix`      | Bug fixes                            |
| `hotfix`   | Urgent production fixes              |
| `chore`    | Tooling, dependencies, CI, config    |
| `refactor` | Code changes with no behavior change |
| `docs`     | Documentation only                   |
| `test`     | Test-only changes                    |

**Examples:**
- `feature/build-info-section`
- `fix/device-section-api-24`
- `chore/bump-target-sdk-37`

Use lowercase, hyphen-separated words. Keep the description short and specific.

## Pull request titles

Format:

`<type>(<scope>): <short summary>`

Map branch `<type>` to the Conventional Commits type: `feature` → `feat`,
`hotfix` → `fix`, everything else unchanged (`fix`, `chore`, `refactor`, `docs`, `test`).

**Examples:**
- `feat(sdk): add build info section`
- `fix(sdk): show correct SDK level on API 24`
- `chore(ci): add tests workflow`

## Pull request description template

Use exactly the sections defined below unless explicitly requested.
```markdown
## Summary
<!-- What does this PR do and why? -->

## Changes
-
```

## Commit message

Format:

`<type>(<scope>): <short summary>`

**Examples:**
- `feat(sdk): add build info section`
- `fix(sdk): show correct SDK level on API 24`
- `chore(ci): add tests workflow`

## Base branch

Branch off `develop` and open pull requests against `develop`, not `main`. `main` only receives merges from `develop` for releases.

## Branch protection

`main` and `develop` require:
- Pull requests before merging (no direct pushes)
- No force pushes or deletions
