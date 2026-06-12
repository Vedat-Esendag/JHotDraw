# Lab 6 — TestLab (Testing): Checklist & Portfolio Artifact

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`
**Feature / change request:** Add a *Save as PNG* option to the unsaved-changes close dialog.
**Unit under test:** `org.jhotdraw.app.action.AbstractSaveUnsavedChangesAction` (+ the `View` PNG hooks)

---

## Objective
Understand the importance of testing and implement **JUnit 4** unit tests for the most important
domain-logic of the feature: best-case and boundary cases, with stubs to break dependencies and Java
`assert` statements to guard invariants.

---

## Checklist (tick as you go)

- [x] **1. JUnit 4 dependency** declared (4.13.2) — once in the parent POM, test-scoped in `jhotdraw-app`
- [x] **2. JUnit 4 tests** created for the key domain method (Swing + JUnit extensions work best with JUnit 4)
- [x] **3. Best-case test** written (typical happy path)
- [x] **4. Boundary-case tests** written (the awkward inputs most likely to hide a bug)
- [x] **5. Stub used** to avoid dependencies (a hand-written `StubView`, no Swing / no real `SVGView`)
- [x] **6. Java assertions** used for invariants (assert halts; exception continues)
- [x] **7. Verified** — `mvn test`, all 10 unit tests green, BUILD SUCCESS across all modules

---

## 1. Test setup (JUnit 4 with Maven)
JUnit 4 (4.13.2) is declared once in the parent POM under `dependencyManagement` so every module
shares one version, and the module under test declares it test-scoped:

```xml
<!-- parent pom.xml -->
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>junit</groupId><artifactId>junit</artifactId>
      <version>4.13.2</version><scope>test</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<!-- jhotdraw-app/pom.xml -->
<dependency>
  <groupId>junit</groupId><artifactId>junit</artifactId><scope>test</scope>
</dependency>
```

Test sources live in `jhotdraw-app/src/test/java/org/jhotdraw/app/action/` and run via Maven Surefire
(`mvn test`). Surefire enables Java assertions by default, so the `assert` invariants are active
during the run.

---

## 2. What is tested, and why
A unit test should exercise a **single code-path through a single method**, with external
dependencies replaced by stubs. The Save-as-PNG flow mixes pure domain logic with Swing (dialogs,
`SwingWorker`, file choosers), so the core rule was **extracted into a pure, side-effect-free,
package-visible method** — the ideal unit-test target and the feature's most important business rule
(an export must always produce a `.png` file):

```java
// AbstractSaveUnsavedChangesAction.java
static URI ensurePngExtension(final URI rawUri) {
    assert rawUri != null : "rawUri must not be null";          // invariant
    if (rawUri.getPath() == null)                       return rawUri;
    if (rawUri.getPath().toLowerCase().endsWith(".png")) return rawUri;
    return new File(rawUri.getPath() + ".png").toURI();
}
```

Two units are tested:
1. `ensurePngExtension(URI)` — the filename-normalisation rule (best case + boundary cases).
2. The `View` PNG extension point — `canExportToPNG()` / `exportToPNG()`, via a hand-written stub.

---

## 3. Best-case test (happy path)
```java
@Test
public void appendsPngWhenNoExtension() {
    URI in  = new File("/tmp/drawing").toURI();
    URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(in);
    assertTrue(out.getPath().endsWith("/drawing.png"));
}

@Test
public void capableViewReportsTrueAndExports() throws IOException {
    PngCapableStubView v = new PngCapableStubView();
    assertTrue(v.canExportToPNG());
    v.exportToPNG(URI.create("file:/tmp/out.png"));
    assertEquals(1, v.exportCalls);   // export actually happened
}
```

---

## 4. Boundary-case tests
Boundary cases probe the edges of the rule — where bugs hide:

| Boundary input | Expected result | Test method |
|----------------|-----------------|-------------|
| `/tmp/drawing.png` | Returned unchanged (no double extension) | `leavesUnchangedWhenAlreadyPng` |
| `/tmp/DRAWING.PNG` | Unchanged — match is case-insensitive | `treatsExtensionCaseInsensitively` |
| `/tmp/drawing.jpg` | `drawing.jpg.png` (append after other ext) | `appendsPngAfterDifferentExtension` |
| `/tmp/pngdrawing` | Contains "png" but isn't a suffix → append | `appendsPngWhenNameContainsPng` |
| opaque URI (no path) | Returned unchanged, no NullPointerException | `handlesUriWithNullPath` |
| default `View` | `canExportToPNG()` is false | `defaultCanExportToPngIsFalse` |
| default `View` | `exportToPNG()` throws `UnsupportedOperationException` | `defaultExportToPngThrows` |

```java
@Test
public void treatsExtensionCaseInsensitively() {
    URI in  = new File("/tmp/DRAWING.PNG").toURI();
    URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(in);
    assertSame(in, out);   // unchanged, same instance — no needless copy
}
```

---

## 5. Avoiding dependencies with a stub
The `View` default methods can't be reached without a `View` instance, but a real view drags in Swing
and the whole application. A minimal `StubView` implements the interface with neutral no-op bodies and
**deliberately leaves the two PNG methods at their defaults**, so the defaults themselves are
exercised:

```java
private static class StubView implements View {
    // ... all other View methods are no-op / return neutral values ...
    // canExportToPNG() and exportToPNG() are intentionally NOT overridden
    // so the interface DEFAULT methods are what we test.
}
```
The stub isolates the unit: no file system, no Swing, no concrete `SVGView` — exactly one code-path
under test. (For interaction checks, Mockito can verify the view was asked to write the exact file.)

---

## 6. Java assertions for invariants
Per the lab: an **assertion** checks something that should *never* happen and **halts** the program;
an **exception** is part of normal control flow and lets the program **continue**. Both are used
deliberately:
- `assert rawUri != null` guards an internal invariant inside `ensurePngExtension`.
- `UnsupportedOperationException` from the default `exportToPNG()` is normal control flow — it tells a
  caller "this view can't export," and the dialog avoids it via the `canExportToPNG()` gate.

The JUnit assertions (`assertEquals`, `assertTrue`, `assertSame`, and the try/fail/catch idiom for the
expected exception) verify **observable behaviour**, while the Java `assert` statements guard the
**internal invariants** during every test run.

---

## 7. Verification — results (how I verified the feature)
The feature was verified by running the full Maven build with `mvn test`. **All 10 unit tests pass**,
and **every module in the reactor builds successfully (BUILD SUCCESS)**.

How this verifies the feature:
- The **filename rule** is proven for the normal case and for every awkward filename a user might
  type, so an exported file is always a valid `.png`.
- The **capability gate** is proven: ordinary views report `false` (button never appears) while a
  capable view reports `true` and actually exports.
- The **safe default** is proven: an un-upgraded view calling `exportToPNG()` fails loudly rather than
  silently doing nothing.
- Because the build passes for the whole reactor, the feature **integrates cleanly** across modules
  without breaking anything else.

---
![[Screenshot 2026-06-11 221147 1.png]]
## Screenshots to capture (evidence)

## References
- [JUnit4] https://junit.org/junit4/ · [mockito.org] https://site.mockito.org/
- Java Assertions — https://docs.oracle.com/javase/8/docs/technotes/guides/language/assert.html
