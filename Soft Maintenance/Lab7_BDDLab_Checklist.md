# Lab 7 — BDDLab (Behavior Driven Testing): Checklist & Portfolio Artifact

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`
**Feature / change request:** Add a *Save as PNG* option to the unsaved-changes close dialog.
**Tools:** JGiven (BDD) · AssertJ (assertions) · Mockito (mocking)

---

## Objective
Map the feature's **user story** to **Given-When-Then** BDD scenarios, automate them with **JGiven**,
and use **AssertJ** for domain-specific assertions. BDD turns each user story into concrete, testable
behaviour that reads as plain English, so the tests double as living documentation.

---

## Checklist (tick as you go)

- [x] **1. User story** restated in the standard template
- [x] **2. Mapped to Given-When-Then** scenarios (happy path, variation, boundary, negative)
- [x] **3. Automated with JGiven** — three stage classes + a shared `ScenarioContext`
- [x] **4. AssertJ** used for fluent domain assertions; **Mockito** verifies the export call
- [x] **5. When stage drives the real production rule** (`ensurePngExtension`) — not a re-implementation
- [x] **6. Generated report** — JGiven prints plain Given-When-Then English + an HTML report
- [x] **7. Verified** — `mvn test`, all 18 tests (JUnit + JGiven) green

---

## 1. User story
> **As a** user,
> **I want** to choose what type to save the drawing as (e.g. PNG) when I close the app,
> **so that** I don't have to export and then close — saving me time.

BDD maps each user story to one or more Given-When-Then scenarios describing concrete, testable
behaviour; those scenarios are then automated with JGiven using AssertJ for the assertions.

---

## 2. Portfolio Work — Mapping the user story to BDD scenarios
Four scenarios cover the happy path, the time-saving variation, an awkward-input boundary, and the
negative case:

| Scenario | Given → When → Then |
|----------|---------------------|
| **Save as PNG on close** | **Given** a drawing that can export PNG and the name `my-drawing.png` · **When** the user closes and selects Save as PNG · **Then** the option is offered and the drawing is saved as `my-drawing.png` |
| **Time-saver: no extension typed** | **Given** a PNG-capable drawing and the name `my-drawing` · **When** closing & Save as PNG · **Then** the file is saved as `my-drawing.png` automatically |
| **Boundary: uppercase extension** | **Given** the name `DRAWING.PNG` · **When** closing & Save as PNG · **Then** it is kept as-is (no `DRAWING.PNG.png`) |
| **Negative: incapable view** | **Given** a view that cannot export PNG · **When** closing & Save as PNG · **Then** the option is NOT offered and nothing is exported |

---

## 3. Automating with JGiven
JGiven structures each scenario into **three stage classes** — one each for Given, When, Then — whose
step-method names form the readable report. A small shared `ScenarioContext` carries state between
stages:

| Stage class | Responsibility |
|-------------|----------------|
| `GivenDrawing` | Sets up a (Mockito-mocked) `View` — PNG-capable or not — and records the file name the user chose. |
| `WhenClose` | Runs the production rule: offers the option only if `canExportToPNG()`, normalises the name via `ensurePngExtension()`, and calls `exportToPNG()`. |
| `ThenSaved` | Asserts the outcome with AssertJ and verifies the export call with Mockito. |

### The When stage — driving the real production logic
The When stage calls the **actual** production method `ensurePngExtension` — the scenario tests real
behaviour, not a re-implementation:

```java
public class WhenClose extends Stage<WhenClose> {
    @ProvidedScenarioState ScenarioContext context;

    @As("the user closes the app and selects Save as PNG")
    public WhenClose the_user_closes_the_app_and_selects_save_as_PNG() throws IOException {
        context.pngOptionOffered = context.view.canExportToPNG();
        if (context.pngOptionOffered && context.chosenFile != null) {
            context.exportedFile = AbstractSaveUnsavedChangesAction
                    .ensurePngExtension(context.chosenFile);   // real rule
            context.view.exportToPNG(context.exportedFile);
            context.exportPerformed = true;
        }
        return this;
    }
}
```

### The Then stage — AssertJ + Mockito
AssertJ gives fluent, readable assertions; Mockito verifies the view really was asked to write that
exact file:

```java
@As("the drawing is exported to a file named $")
public ThenSaved the_drawing_is_exported_to(@Quoted String expectedName) throws IOException {
    assertThat(context.exportPerformed).as("export performed").isTrue();      // AssertJ
    assertThat(context.exportedFile.getPath()).endsWith(expectedName);        // AssertJ
    verify(context.view).exportToPNG(context.exportedFile);                   // Mockito
    return this;
}
```

### The scenario class
```java
public class SaveAsPngOnCloseScenarioTest
        extends ScenarioTest<GivenDrawing, WhenClose, ThenSaved> {

    @Test
    public void user_can_save_the_drawing_as_PNG_when_closing() throws Exception {
        given().a_drawing_that_supports_PNG_export()
               .and().the_user_chooses_the_file_name("my-drawing.png");
        when().the_user_closes_the_app_and_selects_save_as_PNG();
        then().the_save_as_PNG_option_is_offered()
              .and().the_drawing_is_exported_to("my-drawing.png")
              .and().the_exported_file_is_a_PNG();
    }
    // ... three more scenarios ...
}
```

---

## 4. The generated JGiven report
Because the step methods are written in business language, JGiven prints each scenario as plain
Given-When-Then English (and writes an HTML report under `target/jgiven-reports/`). Actual output:

```text
Scenario: User can save the drawing as PNG when closing
    Given a drawing with unsaved changes that can be exported as PNG
      And the user chooses the file name "my-drawing.png"
    When the user closes the app and selects Save as PNG
    Then the Save as PNG option is offered
      And the drawing is exported to a file named "my-drawing.png"
      And the exported file ends with the .png extension

Scenario: A name without extension is saved as PNG automatically
    Given a drawing with unsaved changes that can be exported as PNG
      And the user chooses the file name "my-drawing"
    When the user closes the app and selects Save as PNG
    Then the drawing is exported to a file named "my-drawing.png"
      And the exported file ends with the .png extension
```

Each scenario is self-documenting — a non-developer can read it and confirm the behaviour matches the
user story.

---

## 5. Verification — results
Running `mvn test` runs the BDD scenarios alongside the JUnit 4 and Mockito unit tests. **All 18
tests pass.** The four BDD scenarios prove the user-visible behaviour (option offered only when
capable, filename always normalised to `.png`, nothing exported for incapable views), while the
unit tests prove the rule in isolation — together giving end-to-end confidence the feature behaves
as the user story promises.

![[Screenshot 2026-06-11 221147 2.png]]

> Tip: generate the HTML report with `mvn test`, then open
> `jhotdraw-app/target/jgiven-reports/html/index.html` — it renders the scenarios as a polished,
> clickable page that screenshots well.

![[Pasted image 20260612011408.png]]
---

## Screenshots to capture (evidence)
1. **The JGiven HTML report** (`target/jgiven-reports/html/index.html`) — all scenarios passing.
2. *(Optional)* **The scenario / stage classes in the IDE.**

## References
- [JGiven] https://jgiven.org · [AssertJ] https://assertj.github.io/doc/
- [AssertJ-Swing] https://joel-costigliola.github.io/assertj/assertj-swing.html · [Mockito] https://site.mockito.org/
