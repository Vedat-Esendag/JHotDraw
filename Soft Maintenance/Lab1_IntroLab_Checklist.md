# Lab 1 — IntroLab: Checklist & Notes

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw
**Fork:** https://github.com/Vedat-Esendag/JHotDraw
**My branch:** `Alex-feature`
**Environment:** JDK 11 · Maven 3.9.x (latest 3.x line; the lab's stated 3.8.x also works on JDK 11)

---

## Objective
Check out the course CASE study source code, get the toolchain working (Maven + GitHub flow),
build JHotDraw, and launch its SVG sample GUI to confirm the environment is set up correctly.

---

## Checklist (tick as you go)

- [x] **1. Fork the project repo** — team fork created at https://github.com/Vedat-Esendag/JHotDraw
- [x] **2. GitHub flow set up** — each member works on their own feature branch; mine is `Alex-feature`
- [x] **3. Install JDK 11** and **Maven 3.9.x** (3.8.x per lab text is also fine)
- [x] **4. Build** the project from the root: `mvn clean install -DskipTests`
- [x] **5. Run** the SVG sample from `jhotdraw-samples-misc`
- [x] **6. Confirm** the JHotDraw GUI window appears

---

## Commands (copy-paste)

**Clone your fork and create your feature branch**
```bash
git clone https://github.com/Vedat-Esendag/JHotDraw.git
cd JHotDraw
git checkout -b Alex-feature
```

**Verify the toolchain**
```bash
java -version      # should report 11.x
mvn -v             # should report Apache Maven 3.9.x (or 3.8.x) on JDK 11
```

**Build from the project root**
```bash
mvn clean install -DskipTests
```
> `-DskipTests` compiles and packages all modules but skips running the test suite, so the
> first build is fast. Expect `BUILD SUCCESS` across all modules.

**Launch the JHotDraw SVG sample GUI**
```bash
cd jhotdraw-samples-misc
mvn exec:java "-Dexec.mainClass=org.jhotdraw.samples.svg.Main"
```
>![[Pasted image 20260612003849.png]]

---

## What each step proves
| Step | Purpose |
|------|---------|
| Fork + branch | GitHub flow is in place; work is isolated on `Alex-feature` and merged via pull request. |
| `mvn clean install -DskipTests` | The multi-module Maven build compiles and packages cleanly on JDK 11. |
| `mvn exec:java ... svg.Main` | The SVG sample runs, confirming the runtime works end to end. |
| GUI appears | Environment is fully working and ready for Concept Location / feature work. |

---

## Screenshots to capture (evidence for the portfolio)
1. **The GitHub fork page** — showing `Vedat-Esendag/JHotDraw` and your `Alex-feature` branch.
2. **The running JHotDraw SVG GUI** — the drawing editor window open.

> Tip: capture #2 with the terminal scrolled to the `BUILD SUCCESS` / reactor summary so all
> module lines are visible — it screenshots well.

---

## Troubleshooting (common gotchas)
- **`mvn` not found** → Maven isn't on your `PATH`. Re-open the terminal after install, or add
  `MAVEN_HOME/bin` to `PATH`.
- **Wrong Java version** → if `java -version` shows something other than 11, set `JAVA_HOME` to
  your JDK 11 install and ensure it precedes other JDKs on `PATH`.
- **Build fails downloading dependencies** → check internet/proxy; if the repo uses GitHub
  Packages, make sure your `settings.xml` / `maven-settings.xml` has valid credentials.
- **GUI doesn't appear but build succeeds** → confirm you ran `exec:java` from inside
  `jhotdraw-samples-misc`, and that the main class is exactly
  `org.jhotdraw.samples.svg.Main`.
