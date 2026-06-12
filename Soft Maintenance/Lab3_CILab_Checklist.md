# Lab 3 — CILab (Impact Continuous Integration Lab): Checklist & Notes

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`

---

## Objective
Understand Continuous Integration and set up a simple CI pipeline that automatically **builds** and
**tests** the JHotDraw fork on every pull request, using GitHub Actions + Maven, with shared jars
pulled from GitHub Packages.

> CILab has **no separate "Portfolio Work" artifact** — the deliverable *is* the working pipeline.
> Evidence for the portfolio is a screenshot of a green CI run on a pull request.

---

## Checklist (tick as you go)

- [x] **1. Understand CI** — merging developers' work to a shared mainline often, validated automatically
- [x] **2. Add the workflow file** at `.github/workflows/maven.yml`
- [x] **3. Configure build on every pull request** (Maven)
- [x] **4. Add `maven-settings.xml`** in the project root for GitHub Packages access
- [x] **5. Configure automatic test execution** (`mvn clean install` runs the test suite)
- [x] **6. Confirm a green run** on a PR from `Alex-feature`

---

## Artifact 1 — Workflow file: `.github/workflows/maven.yml`
```yaml
name: Java CI with Maven

on:
  pull_request:
    branches: [ main, master, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven

      - name: Build and Test with Maven
        run: mvn clean install -s maven-settings.xml
```

### What each part does
| Part | Purpose |
|------|---------|
| `on: pull_request` | Runs the pipeline automatically on every PR to main/master/develop, so feature branches are validated **before** merge. |
| `actions/checkout@v3` | Pulls the PR's source into the runner. |
| `setup-java@v3` (JDK 11 Temurin, `cache: maven`) | Installs the JDK matching the project and caches Maven dependencies for faster runs. |
| `mvn clean install` | Builds **all** modules and runs the **test suite**. A compile error or failing test fails the check. |
| `-s maven-settings.xml` | Uses the repo settings file so shared jars can be pulled from GitHub Packages (authenticated via `GITHUB_TOKEN`). |

---

## Artifact 2 — `maven-settings.xml` (project root)
Needed so the build can authenticate to GitHub Packages and pull shared jars. Replace the
placeholders with your GitHub username and a token (in CI this is the injected `GITHUB_TOKEN`).

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <servers>
    <server>
      <id>github</id>
      <username>${env.GITHUB_ACTOR}</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/Vedat-Esendag/JHotDraw</url>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>
</settings>
```

> In GitHub Actions, `GITHUB_ACTOR` and `GITHUB_TOKEN` are provided automatically, so no secrets
> need to be hard-coded.

---

## Link to the feature
Because the Save-as-PNG change spans four modules (**api**, **app**, **samples-misc**, and
indirectly **core**), the CI build is what guarantees the new `exportToPNG` hook and its `SVGView`
override compile and integrate cleanly across the whole project on each pull request — it is the
safety net for the evolution work in the later labs.

---

## Screenshots to capture (evidence)
![[Screenshot 2026-06-11 151036.png]]