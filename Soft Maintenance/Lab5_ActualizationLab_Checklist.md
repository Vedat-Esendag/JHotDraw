# Lab 5 — ActualizationLab: Checklist & Portfolio Artifact

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw · Lecturer: Jan Corfixen Sørensen
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`
**Feature / change request:** Add a *Save as PNG* option to the unsaved-changes close dialog.

---

## Objective
The actualization phase implements the new functionality, incorporates it into the old code, and
propagates the change to every place that needs a secondary modification. This lab's goal is to
**understand and explain Clean Architecture and Clean Code principles** in the context of that
actualization — using the *Save as PNG* feature as the worked example.

---

## Checklist (tick as you go)

- [x] **1. Implemented the new functionality** — `exportToPNG` path added to the close workflow
- [x] **2. Incorporated it into the old code** — new dialog option + handler branch in the controller
- [x] **3. Propagated the change** — `View` interface, `SVGView`, `Labels.properties` updated
- [x] **4. SOLID examples written** — all five principles, grounded in the actual code (below)
- [x] **5. Clean Architecture explained** — layer mapping + the dependency rule (below)

---

## What was actually changed (change propagation)
Each artifact owns one concern, so the change stayed local:
- **`AbstractSaveUnsavedChangesAction`** (app) — adds the *Save as PNG* dialog option and the
  `exportViewToPNG` orchestration; no image-encoding code.
- **`View`** (api) — gains two `default` methods, `exportToPNG(URI)` and `canExportToPNG()`.
- **`SVGView`** (samples.svg) — overrides the new hooks to write a real PNG via `ImageOutputFormat`.
- **`Labels.properties`** (app resource) — new button-label key.
- **`ImageOutputFormat`**, **`SVGApplicationModel`**, **`JSheet/JFileURIChooser`** — reused unchanged.

---

## Portfolio Work — Part 1: SOLID principles in the CASE study

### Single Responsibility Principle (SRP)
*A class should have one, and only one, reason to change.* `AbstractSaveUnsavedChangesAction` owns
only the close-time save workflow (detect unsaved changes, show the dialog, dispatch the choice). It
does **not** know how to encode an image — it delegates to the view, which delegates to the
rasteriser. `Labels.properties` owns user-facing text, `ImageOutputFormat` owns "turn a drawing into
a PNG file," and `JFileURIChooser` owns "ask the user for a file location."

```java
// Controller orchestrates only — no image-encoding code:
protected void exportViewToPNG(final View v, final URI rawUri) {
    final URI uri = ensurePngExtension(rawUri);        // fix extension
    new SwingWorker<Void,Void>() {
        protected Void doInBackground() { v.exportToPNG(uri); return null; }
        protected void done() { get(); doIt(v); }      // close after success
    }.execute();
}
```

### Open/Closed Principle (OCP)
*Open for extension, closed for modification.* The `View` interface was extended with two **default**
methods rather than broken. Every existing view keeps compiling and behaving as before; a view that
wants the capability simply overrides the defaults. A new exportable type could be added later the
same way, with zero edits to the dialog or the interface.

```java
// View.java (jhotdraw-api) — new behaviour without breaking anyone
public default void exportToPNG(URI uri) throws IOException {
    throw new UnsupportedOperationException("This view does not support PNG export");
}
public default boolean canExportToPNG() { return false; }

// SVGView.java — opts in by overriding
@Override public boolean canExportToPNG() { return true; }
@Override public void exportToPNG(URI uri) throws IOException {
    new ImageOutputFormat().write(new File(uri), svgPanel.getDrawing());
}
```

### Liskov Substitution Principle (LSP)
*Subtypes must be substitutable for their base type.* The dialog operates purely on the `View` type
and never downcasts to `SVGView`. A view that cannot export PNG inherits the safe default
(`canExportToPNG()` returns false), so the button is never shown and `exportToPNG()` is never invoked
on it. The capability is **queried before it is used**, so no subtype can be placed in a position
that violates the contract.

```java
// The dialog asks the abstraction; it never assumes a concrete view
if (view.canExportToPNG()) {
    optionList.add(labels.getString("file.saveBefore.savePngOption.text"));
}
```

### Interface Segregation Principle (ISP)
*Clients should not depend on methods they do not use.* Rather than a heavy "Exporter" interface
every view would be forced to implement, two small focused methods were added whose defaults make
them effectively optional. A view that only saves SVG inherits a no-op/false answer instead of
carrying image-export machinery. The separate `canExportToPNG()` query lets a client learn exactly
the one fact it needs without calling the heavier operation, and the PNG chooser is kept separate
from the normal save chooser so neither concern pollutes the other.

```java
// A dedicated, narrowly-scoped chooser for PNG — not bolted onto the SVG one
protected URIChooser getPNGChooser(View view) {
    JFileURIChooser fc = new JFileURIChooser();
    fc.setDialogType(JFileChooser.SAVE_DIALOG);
    fc.setFileFilter(new FileNameExtensionFilter(
        "Portable Network Graphics (PNG)", "png"));
    return fc;
}
```

### Dependency Inversion Principle (DIP)
*High-level modules should depend on abstractions, not concretions.* The high-level policy —
`AbstractSaveUnsavedChangesAction` in module **jhotdraw-app** — depends on the abstraction `View`
(module **jhotdraw-api**), not on the concrete `SVGView` (module **jhotdraw-samples**). The concrete
view is plugged in polymorphically at runtime, which is exactly why the close dialog can trigger a
PNG export while staying ignorant of SVG and PNG.

```java
// High-level controller calls the abstraction, not SVGView
new SwingWorker<Void,Void>() {
    protected Void doInBackground() {
        v.exportToPNG(uri);   // v is declared as View, resolved at runtime
        return null;
    }
}.execute();
```

### SOLID coverage summary
| Principle | How the change honours it |
|-----------|---------------------------|
| SRP | Controller orchestrates; encoding, text and file-choosing live in separate classes. |
| OCP | `View` extended via default methods; existing views untouched. |
| LSP | Dialog uses `View` only, gated by `canExportToPNG()`; no downcasts. |
| ISP | Two small optional methods instead of a heavy mandatory interface. |
| DIP | app depends on the `api` abstraction, not the concrete `SVGView`. |

---

## Portfolio Work — Part 2: Clean Architecture in the CASE study

Clean Architecture organises code into concentric layers where **source-code dependencies point
inward**, toward stable abstractions, and inner layers know nothing about outer ones. JHotDraw's
module structure maps onto this directly, and the feature respected the dependency rule throughout.

**Mapping the feature onto the layers:**
- **Inner / abstraction:** `org.jhotdraw.api.app.View` — the stable contract the dialog depends on.
- **Policy:** `org.jhotdraw.app.action.AbstractSaveUnsavedChangesAction` — the close/save workflow.
- **Outer / detail:** `org.jhotdraw.samples.svg.SVGView` and `org.jhotdraw.draw.io.ImageOutputFormat`
  — the concrete view and the image encoder that actually write bytes to disk.

**The dependency rule was never violated.** The feature needed outer-layer behaviour (writing image
bytes), but the inner controller never reached outward to grab it. Instead it used the classic Clean
Architecture move: **define the abstraction inside the boundary** (on `View`) and let the outer ring
implement it. So control flows outward (dialog → view → image format) while source-code dependencies
still point inward:

```text
jhotdraw-app          --depends on-->  jhotdraw-api      (controller knows the View abstraction)
jhotdraw-samples.svg  --depends on-->  jhotdraw-api + jhotdraw-core
                                       (concrete view implements the abstraction, uses the encoder)
Nothing in jhotdraw-api depends on app, svg or draw.io — the centre stays clean.
```

![[mermaid-diagram-2026-06-12-010114 1.png]]

**Why this paid off — a small blast radius.** Because the architecture is well-layered, the change
stayed local: impact analysis found only **four artifacts changed across three modules** (the
interface, the controller, one concrete view, and a resource file), while the image encoder, the
application model and the Swing helpers were reused unchanged. Swapping the encoder or adding another
exportable type would touch only the outer ring; the controller and the `View` interface would not
move. That locality is the practical payoff of obeying the dependency rule.

### Clean Code touches in the actualization
- **Intention-revealing names:** `savePNGView()`, `exportViewToPNG()`, `getPNGChooser()` say exactly
  what they do.
- **Small, extracted methods:** dialog construction (`createSaveOptionPane`) and dispatch
  (`handleSaveOptionSelected`) are separate, each doing one thing.
- **No magic numbers:** the destructive-button index is `options.length - 1`, not a hard-coded `2`,
  so adding a button can't silently mis-mark which option is destructive.
- **Fail loudly, then recover:** a failed export surfaces an error sheet and leaves the view open
  rather than silently closing.

### Honest limitation
One pre-existing wart remains: `SVGView.write()` still always writes SVG regardless of the filter
chosen in the normal Save dialog. That is outside this feature's scope; the Save-as-PNG path sidesteps
it cleanly by calling `exportToPNG()` directly rather than routing through `write()`. Making the
boundary of the change explicit is itself a Clean Code practice.

---

## Screenshots to capture (evidence)

## References
- Martin, R. C. (2017). *Clean Architecture.* Prentice Hall.
- Martin, R. C. (2008). *Clean Code.* Prentice Hall.
