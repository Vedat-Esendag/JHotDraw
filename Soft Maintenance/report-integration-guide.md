# Report Integration Guide — What to Write, Where, and How

This guide maps the content from each lab checklist to the corresponding section in the example report. Each entry explains **what** belongs there, **where** in the report it goes, and **how** to present it (text, table, code, screenshot).

---

## Abstract

**What:** A short summary (~150–200 words) of the entire project: the change made, the process followed, and the results.

**How to write it:**
- One sentence naming the feature ("Save as PNG button added to the unsaved-changes dialog").
- One sentence on concept location: where the feature was found (`AbstractSaveUnsavedChangesAction`, debugger-based).
- One sentence on impact analysis: small CHANGED footprint (controller, `View` interface, `SVGView`, `Labels.properties`).
- One sentence on refactoring: what smell was found (Long Method, cognitive complexity 17), what was applied (Extract Method, Guard Clause, Lambda).
- One sentence on SOLID/Clean Architecture: the two default methods added to `View`, dependency rule honoured.
- One closing sentence on verification: 10 JUnit tests + 4 JGiven BDD scenarios = 18 tests total, all green on CI.

---

## Section: Introduction → What is JHotDraw?

**What:** A brief factual description of the JHotDraw project and why it was chosen.

**How to write it:**
- Describe JHotDraw as a Java GUI framework for structured/technical drawings, originally by Erich Gamma and Thomas Eggenschwiler as a design patterns showcase.
- Name the specific sample used: the SVG sample application (vector drawings saved as SVG).
- Justify the choice: rich enough architecture to refactor meaningfully, well-documented enough to understand in the course timeframe.

---

## Section: Introduction → Feature Work Area and Rationale

**What:** Explains *which* change request was chosen and *why* that area of the codebase is appropriate for maintenance work.

**Source:** Lab 2 ChangeReqLab — change request summary and rationale.

**How to write it:**
- Describe the existing unsaved-changes dialog (offers Save, Cancel, Don't Save).
- State the selected change: adding a fourth "Save as PNG" button.
- Give the rationale: this area sits at the intersection of the application lifecycle, the user-interaction layer, and the output layer — exactly where responsibilities tend to leak, making it fertile ground for SOLID and Clean Code work.

---

## Section: Initiation → User Story

**What:** The formal user story in the standard "As a / I want / So that" template, followed by acceptance criteria.

**Source:** Lab 2 ChangeReqLab — Portfolio Artifact.

**How to write it:**
- Quote the user story verbatim in the template format.
- List all acceptance criteria as bullets (the report lists six: dialog shows the button only for capable views, PNG save dialog opens pre-filtered, `.png` is appended if missing, PNG is not treated as the saved SVG, view closes after export, existing buttons unchanged).

---

## Section: Initiation → Team Pipeline: Backlog and CI

**What:** Evidence that the work was managed with GitHub flow (backlog card + feature branch) and that a CI pipeline validates every pull request.

**Source:** Lab 2 ChangeReqLab (backlog card) + Lab 3 CILab (workflow file + maven-settings.xml).

**How to write it:**
- One paragraph: name the GitHub Projects backlog card ("Add 'Save as PNG' option to unsaved-changes close dialog"), the feature branch (`Alex-feature`), and the GitHub flow (feature branch → PR).
- One paragraph explaining the CI workflow:
  - Triggered on every PR to `main`/`master`/`develop`.
  - Steps: `actions/checkout@v3`, `setup-java@v3` (JDK 11 Temurin, Maven cache), `mvn clean install -s maven-settings.xml`.
  - What it guarantees: all modules compile and the test suite runs before merge.
- Include the YAML workflow snippet as a code block (as shown in the report's table).
- **Insert screenshot** of a green CI run on a PR (from `Lab3_CILab_Checklist.md` — `Screenshot 2026-06-11 151036.png`).

---

## Section: Concept Location

**What:** Explains how the IDE debugger was used to find the exact classes responsible for the unsaved-changes feature, and presents the initial set of domain classes.

**Source:** Lab 2 CLLab — full checklist and portfolio artifact table.

**How to write it:**
- Opening paragraph: state that concept location finds the precise place in code where the feature lives. The trigger is closing a view with unsaved changes. Entry point is the close/exit action (controller classes first).
- Describe the debugger steps: breakpoints set at dialog construction and user-choice dispatch; stepped through to trace the concept.
- Present the **Domain Class | Responsibility table** (the lab's portfolio artifact) — six rows: `AbstractSaveUnsavedChangesAction`, `View` interface, `SVGView`, `SVGApplicationModel`, `ImageOutputFormat`, `JSheet`/`JFileURIChooser`.
- Add the note about `Labels.properties` being a resource, not a class, but still participating in the concept (button labels).

---

## Section: Impact Analysis → Static Impact Analysis

**What:** Results of reading the code with "Find Usages" / "Go to Implementation" to map the change ripple, with each class classified as CHANGED / UNCHANGED / PROPAGATES.

**Source:** Lab 3 AnalysisLab — static analysis section and Estimated Impact Set table.

**How to write it:**
- One paragraph explaining the approach: start from the located concept (`AbstractSaveUnsavedChangesAction` = CHANGED), mark neighbours NEXT, then classify.
- Key finding: `CloseFileAction` inherits from the controller so the parent change flows through automatically; `View` and `SVGView` are CHANGED because of the new hook; image writer and UI helpers are reused unchanged.
- Present the **Estimated Impact Set table** (8 rows: the four CHANGED items + UNCHANGED + PROPAGATES).
- Reference the UML class diagram (Figure 2 in the report) — note this is from `diagrams/01_class_diagram.puml`.

---

## Section: Impact Analysis → Dynamic Impact Analysis

**What:** Runtime proof that the identified path is correct and the change produces the expected behaviour.

**Source:** Lab 3 AnalysisLab — dynamic analysis section.

**How to write it:**
- Explain the setup: breakpoint in `actionPerformed()`, JHotDraw launched in debug mode, drew a shape and clicked Close.
- Describe the result: execution halted at the breakpoint, confirming this is the exact moment the dialog spawns.
- Show the injected modification code snippet:
  ```java
  if (option == JOptionPane.YES_OPTION) {
      saveView(view);        // standard save routine
      exportToPNG(view);     // MODIFICATION: also export as PNG
  }
  ```
- State the outcome: the trace confirmed the drawing is routed through `ImageOutputFormat` and a PNG file is produced.
- Reference Figure 3 (sequence diagram from `diagrams/02_sequence_diagram.puml`).
- **Insert screenshots**: the unsaved-changes prompt on canvas + the generated `.png` file in the file explorer (Figure 4 placeholder in report).

---

## Section: Impact Analysis → Package List

**What:** A structured table of all packages visited during impact analysis, how many classes were inspected in each, and what each contributed.

**Source:** Lab 3 AnalysisLab — Portfolio Artifact Table 1.

**How to write it:**
- Use the exact table from the lab (6 packages, columns: package name, # classes, comments).
- Follow with the summary sentence: the change bridges the application-lifecycle system and the I/O/export system; CHANGED footprint is small but required traversing five packages.

---

## Section: Prefactoring → The Code Smell

**What:** Identifies the specific bad code smell found in the class that needs to be changed, with tooling evidence.

**Source:** Lab 4 RefactoringLab — Portfolio Work sections 1 and 2.

**How to write it:**
- Name the smell: *Long Method* and *High Cognitive Complexity* (Fowler / Kerievsky [Ker05]).
- Describe where it was found: `actionPerformed()` in `AbstractSaveUnsavedChangesAction` — checked for unsaved changes, constructed `JOptionPane`, AND handled user choice inside an anonymous `SheetListener` with nested `if/else`.
- State the measurement: SonarLint flagged **cognitive complexity of 17**, exceeding the limit of 15.
- Explain why it matters: deep nesting forces the reader to hold too much context; risky to extend — exactly the method the Save as PNG change needs to touch.
- **Insert screenshot** of the SonarLint warning (`Screenshot 2026-06-11 160651.png`).

---

## Section: Prefactoring → The Refactoring Strategy

**What:** Documents which refactoring patterns were applied, why, and the before/after evidence.

**Source:** Lab 4 RefactoringLab — Portfolio Work sections 3 and 4, plus Before/After code blocks.

**How to write it:**
- Explain the strategy: a sequence of small, behaviour-preserving transformations, each verified by re-running tests.
- Present the three refactorings in a table (as in the lab):
  - **Extract Method** → `showUnsavedChangesDialog`, `createSaveOptionPane`, `handleSaveOptionSelected` pulled out of `actionPerformed`. Fixes Long Method; each does one thing; complexity drops below threshold.
  - **Replace Nested Conditional with Guard Clause** → `if (view == null) return;` at the top. Removes one level of nesting.
  - **Replace Anonymous Class with Lambda** → the `SheetListener` passed to `JSheet.showSheet`. Removes boilerplate; lowers complexity further.
- Show the **before** code snippet (deeply nested, complexity 17) and **after** code snippet (flat, extracted, lambda-based) as the report does.
- **Insert screenshot** of the IDE showing the before/after or the cleared SonarLint warning (`Screenshot 2026-06-11 221509 1.png` and `Screenshot 2026-06-11 221517.png`).

---

## Section: Prefactoring → Clean Code Touches

**What:** Specific Clean Code practices applied during the refactoring (beyond just Extract Method).

**Source:** Lab 4 RefactoringLab + Lab 5 ActualizationLab — Clean Code touches sections.

**How to write it (four bullet/paragraph points):**
- **Meaningful names** (Clean Code Ch. 2): `savePNGView()`, `exportViewToPNG()`, `getPNGChooser()` say exactly what they do.
- **Functions / Stepdown Rule**: dialog construction (`createSaveOptionPane`) and dispatch (`handleSaveOptionSelected`) are separate; `actionPerformed` reads at one level of abstraction.
- **No magic numbers**: destructive-button index is `options.length - 1`, not a hard-coded `2`.
- **Comments**: public methods got JavaDoc; inline comments added only where intent is non-obvious; noise comments deleted.

---

## Section: Actualization → Each SOLID Principle

**What:** Five sub-sections, one per SOLID principle, each showing how the feature implementation honours that principle with a concrete code example.

**Source:** Lab 5 ActualizationLab — Portfolio Work Part 1 (all five principles with code snippets).

**How to write it (one sub-section per principle):**

**SRP:** `AbstractSaveUnsavedChangesAction` orchestrates only — no encoding code. Show the `exportViewToPNG` snippet where it delegates `v.exportToPNG(uri)` to the view.

**OCP:** `View` extended with two `default` methods instead of being broken. Show the `View.java` snippet (`exportToPNG` throwing `UnsupportedOperationException` by default, `canExportToPNG` returning false) and the `SVGView.java` override.

**LSP:** Dialog only ever uses `View`, never downcasts. Show the capability-gate snippet: `if (view.canExportToPNG()) { optionList.add(...) }`.

**ISP:** Two small optional methods added instead of a heavy mandatory interface. Show the dedicated `getPNGChooser` method — a narrowly-scoped chooser not bolted onto the SVG one.

**DIP:** `jhotdraw-app` depends on `jhotdraw-api` abstraction, not on `SVGView`. Show the `SwingWorker` snippet where `v` is declared as `View` and resolved at runtime.

---

## Section: Postfactoring → Clean Architecture Layer Mapping

**What:** Maps the changed code onto the Clean Architecture concentric layers and explains why the dependency rule was never violated.

**Source:** Lab 5 ActualizationLab — Portfolio Work Part 2 + Lab 5 BeforeChanges — Section 2.

**How to write it:**
- Show the module dependency text diagram (as in the report):
  ```
  jhotdraw-app         → depends on → jhotdraw-api   (controller knows View)
  jhotdraw-samples.svg → depends on → jhotdraw-api + jhotdraw-core
  Nothing in jhotdraw-api depends on app, svg or draw.io
  ```
- Explain the key move: the inner controller never reached outward to grab concrete behaviour; instead, the abstraction was defined on `View` inside the boundary and the outer ring implemented it. Control flows outward; source-code dependencies point inward.
- Explain the payoff: only four artifacts changed across three modules — small blast radius because the architecture is well-layered.
- **Insert the Clean Architecture / Mermaid diagram** (`mermaid-diagram-2026-06-12-010114 1.png` from Lab 5).

---

## Section: Postfactoring → Verification that Nothing Else Broke

**What:** Confirms statically that subclasses were not broken by the refactoring.

**Source:** Lab 5 ActualizationLab — "Honest limitation" paragraph + Lab 4 RefactoringLab verification step.

**How to write it:**
- Name the three concrete subclasses of `AbstractSaveUnsavedChangesAction`: `ClearFileAction`, `CloseFileAction`, `ExitAction`. None override `actionPerformed`; they only override the abstract `doIt(View)` hook.
- State that the refactoring preserved the exact visibility of `public void actionPerformed` and kept the internal call to `doIt(view)` intact — contract between superclass and subclasses is unbroken.
- Acknowledge the pre-existing limitation: `SVGView.write()` still always writes SVG regardless of the chosen filter; this is out of scope and the Save-as-PNG path sidesteps it by calling `exportToPNG()` directly.

---

## Section: Verification → Unit Testing (JUnit 4)

**What:** Explains the unit test setup, what is tested, and provides the boundary-case table.

**Source:** Lab 6 TestLab — all sections.

**How to write it:**
- Explain what is tested: `ensurePngExtension(URI)` (the filename normalisation rule) and the `View` PNG extension point via a hand-written `StubView`.
- Briefly describe the stub pattern: `StubView` implements `View` with no-op bodies and deliberately leaves the two PNG methods at their defaults, so the default methods themselves are exercised without Swing or a real `SVGView`.
- Explain the assertion/invariant distinction: Java `assert` guards internal invariants (halts), JUnit assertions verify observable behaviour (continues).
- Show the happy-path test (`appendsPngWhenNoExtension`).
- Show one boundary-case test (`treatsExtensionCaseInsensitively` with `assertSame`).
- Present the **boundary-case table** (7 rows from the lab).
- State the result: all 10 unit tests pass, BUILD SUCCESS across all modules.
- **Insert screenshot** of `mvn test` output (`Screenshot 2026-06-11 221147 1.png`).

---

## Section: Verification → BDD with JGiven

**What:** Maps the user story to four Given-When-Then scenarios and shows how they are automated with JGiven + AssertJ + Mockito.

**Source:** Lab 7 BDDLab — all sections.

**How to write it:**
- Restate the user story (same as the Initiation section).
- Present the **four-scenario table** (happy path, time-saver, boundary, negative).
- Explain the three JGiven stage classes: `GivenDrawing` (setup), `WhenClose` (drives real `ensurePngExtension`), `ThenSaved` (AssertJ + Mockito).
- Show the `WhenClose` stage snippet (calls the actual production method, not a re-implementation).
- Show the `ThenSaved` snippet (AssertJ `assertThat` + Mockito `verify`).
- Show the scenario test class snippet (`given().a_drawing...when()...then()`).
- Show the generated JGiven plain-text output (the two "Scenario:" blocks).
- State the result: 18 tests total (10 JUnit + 8 BDD / Mockito), all green.
- **Insert two screenshots**: the JGiven HTML report (`Screenshot 2026-06-11 221147 2.png`) and the `mvn test` output showing 18 tests passing (`Pasted image 20260612011408.png`).

---

## Section: Discussion

**What:** Honest reflection on what could have been done better and what alternatives were considered and rejected.

**Source:** Lab 5 ActualizationLab — "Honest limitation" + Lab 7 BDDLab — coverage note.

**How to write it (two paragraphs):**

**What could have been better:**
- Dynamic impact analysis relied partly on a temporary injected log/export line; a more rigorous approach would use the debugger and a coverage tool exclusively.
- BDD covers the happy path most thoroughly; edge cases (cancelling the file chooser, write failure on a read-only directory) are only partially exercised.
- The `SVGView.write()` wart (always writes SVG regardless of filter) was left out of scope.

**What was harder / alternatives rejected:**
- Early approach added a heavyweight "Exporter" interface → rejected because it forces unrelated views to carry image-export machinery (violates ISP).
- Downcasting to `SVGView` inside the dialog → rejected because it violates LSP and couples the controller to a concrete view; `canExportToPNG()` query on the abstraction avoids this entirely.
- Adding `default` methods to `View` was chosen because it keeps existing views untouched (OCP) and makes the capability opt-in.

---

## Screenshots Summary — Where Each Goes

| Screenshot file | Report location |
|---|---|
| `Screenshot 2026-06-11 151036.png` | Figure 1 — Green CI pipeline run (Initiation → CI section) |
| `Screenshot 2026-06-11 160651.png` | Figure 5 — SonarLint cognitive-complexity warning (Prefactoring → Code Smell) |
| `Screenshot 2026-06-11 221509 1.png` | Figure 5 — Before/after `actionPerformed` in the IDE (Prefactoring → Strategy) |
| `Screenshot 2026-06-11 221517.png` | Figure 5 (second part) — SonarLint warning cleared after refactoring |
| `mermaid-diagram-2026-06-12-010114 1.png` | Figure 6 — Clean Architecture / module dependency diagram (Postfactoring) |
| `Screenshot 2026-06-11 221147 1.png` | Figure 8 — `mvn test` output, 10 JUnit tests green (Verification → Unit Testing) |
| `Screenshot 2026-06-11 221147 2.png` | Figure 7 — JGiven HTML report (Verification → BDD section) |
| `Pasted image 20260612011408.png` | Figure 8 — `mvn test` output, 18 tests total green (Verification → BDD section) |
| `Screenshot 2026-06-11 221059.png` | Can accompany the backlog card description (Initiation → Team Pipeline) |
| `Pasted image 20260612003849.png` | Can show the running JHotDraw GUI (Introduction or Concept Location) |

> **Tip:** The report currently uses `[ INSERT SCREENSHOT HERE ]` placeholders for Figures 1, 4, 5, 6, 7, and 8. Match each screenshot file above to its placeholder and replace in the `.docx`.

---

## What the Report Does NOT Yet Have (gaps to fill)

- **Figures 2, 3, 6 (UML/sequence diagrams):** These reference PlantUML files (`01_class_diagram.puml`, `02_sequence_diagram.puml`, `03_impact_set_graph.puml`, `04_package_dependency.puml`). Render them at plantuml.com and insert the resulting images into the report. Figure 2 appears in both Impact Analysis and Postfactoring.
- **Figure 4:** Two runtime screenshots — the unsaved-changes dialog shown on a canvas with a drawing, and the file explorer showing the generated `.png` file.
- **References section:** Must include Fowler (2018), Kerievsky (2005), Martin (2017) *Clean Architecture*, Martin (2008) *Clean Code*, JUnit4, JGiven, AssertJ, and Mockito URLs. The lab checklists list the exact citations.
