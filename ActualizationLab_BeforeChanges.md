# Actualization Lab — SOLID & Clean Architecture in JHotDraw

**Scope:** This analysis looks at the JHotDraw code **as it was before** the
"Save as PNG on close" feature was added. It identifies where the *existing*
design already demonstrates the SOLID principles, Clean Architecture, and Clean
Code — the foundation the later change was built on. No code is modified here;
every example refers to classes that were already in the project.

The feature touches the close/save flow, so the analysis centres on the classes
on that path: the `AbstractSaveUnsavedChangesAction` controller, the `View`
abstraction, the concrete `SVGView`, the `ApplicationModel` factory, and the
drawing `OutputFormat` family.

---

## 1. SOLID principles in the existing design

### 1.1 Single Responsibility Principle (SRP)

> *A class should have one reason to change.*

The close/save flow is split so each class owns exactly one concern:

- **`AbstractSaveUnsavedChangesAction`** owns *the workflow* of "ask to save
  before a destructive action." It detects unsaved changes, shows the dialog,
  and dispatches Save / Cancel / Don't Save. Notably, the original code already
  extracted the dialog construction and the choice-handling into separate
  private methods (`createSaveOptionPane`, `handleSaveOptionSelected`,
  `showUnsavedChangesDialog`) — each doing one thing.
- **`SVGView`** owns *how an SVG document reads, writes and renders itself* — its
  `write()` serialises the drawing through `SVGOutputFormat`.
- **`SVGApplicationModel`** owns *wiring*: building the save/open choosers and
  registering which output formats a view supports.
- **`Labels.properties`** owns *user-facing text* — so a wording or translation
  change never touches Java code.

Each of these would change for a different reason, which is exactly what SRP asks
for.

### 1.2 Open/Closed Principle (OCP)

> *Open for extension, closed for modification.*

JHotDraw's action framework is the textbook example. `AbstractViewAction` (and
its subclass `AbstractSaveUnsavedChangesAction`) defines the skeleton of an
action; concrete actions such as `SaveFileAction`, `ExportFileAction`,
`PrintFileAction` and `OpenFileAction` **extend** the behaviour without modifying
the base classes. New actions are added by subclassing, not by editing existing
ones.

The same is true of the drawing I/O layer: the `OutputFormat` interface lets new
file formats (`SVGOutputFormat`, `ImageOutputFormat`, …) be plugged in as new
implementations. The save/open machinery iterates over `OutputFormat` objects and
never needs to change when a new format appears.

### 1.3 Liskov Substitution Principle (LSP)

> *Subtypes must be substitutable for their base type.*

The dialog and the application work purely against the `View` **interface** and
never downcast to a specific view. `SVGView`, the Teddy text view, the ODG view —
any `View` implementation can be substituted wherever a `View` is expected,
because they all honour the same contract (`write`, `canSaveTo`,
`hasUnsavedChanges`, `getURI`/`setURI`, …). Likewise, every concrete `Action`
substitutes cleanly for `javax.swing.Action`, so Swing can drive them
uniformly through `ActionMap`.

### 1.4 Interface Segregation Principle (ISP)

> *Clients should not depend on methods they do not use.*

Responsibilities are split across **focused interfaces** rather than one giant
one:

- `View` — an open document.
- `Application` — the running application shell.
- `ApplicationModel` — a factory for actions/choosers/menus.
- `OutputFormat` / `InputFormat` — serialisation, each a small interface
  (`OutputFormat` is essentially `getFileFilter()`, `getFileExtension()`,
  `write(...)`).
- `Disposable` — a single `dispose()` method.

A class that only needs to write a drawing depends on `OutputFormat`, not on the
whole application. The interfaces are kept small and role-specific.

### 1.5 Dependency Inversion Principle (DIP)

> *High-level modules depend on abstractions, not on concretions.*

This is enforced by the **module layout**. The high-level policy
(`AbstractSaveUnsavedChangesAction` in `jhotdraw-app`) depends on the `View` and
`Application` **abstractions** in `jhotdraw-api`, never on a concrete view in the
`jhotdraw-samples` module. Concrete views and formats are supplied at runtime
through the `ApplicationModel` factory. The dependency arrow points inward,
toward the stable abstractions — the essence of DIP.

---

## 2. Clean Architecture in the existing design

JHotDraw is split into Maven modules whose dependencies point inward toward
stable abstractions:

| Layer | Module | Role |
|---|---|---|
| Entities / abstractions | `jhotdraw-api` | The most stable contracts: `View`, `Application`, `ApplicationModel`, `OutputFormat`. Knows nothing about Swing, SVG, or any concrete app. |
| Use cases / controllers | `jhotdraw-app`, `jhotdraw-actions` | Workflow logic — the "save before closing" action and the file actions. Depends inward on the api abstractions. |
| Interface adapters | `jhotdraw-samples.svg` | Concrete views/models (`SVGView`, `SVGApplicationModel`) that adapt the abstractions to a specific document type. |
| Frameworks & details | `jhotdraw-core` (`draw.io`), `jhotdraw-gui` | The volatile details: image/SVG encoders (`ImageOutputFormat`, `SVGOutputFormat`) and the Swing widgets (`JSheet`, `JFileURIChooser`). |

**The dependency rule holds:** nothing in `jhotdraw-api` depends on `app`,
`samples`, or `draw.io`. Control flows outward (an action calls a view, which
calls an output format) while source-code dependencies point inward. This is why
a new document type or a new file format can be added in an outer module without
the inner abstractions changing — and, later, why the Save-as-PNG feature could
be added with such a small blast radius.

---

## 3. Clean Code touches already present

The original code already follows several Clean Code habits:

- **Intention-revealing names** — `hasUnsavedChanges()`, `markChangesAsSaved()`,
  `canSaveTo(uri)`, `createSaveChooser(...)` read like sentences.
- **Small, extracted methods** — the unsaved-changes action separates dialog
  construction (`createSaveOptionPane`), display (`showUnsavedChangesDialog`) and
  choice handling (`handleSaveOptionSelected`) instead of one long method.
- **Guard clauses** — `actionPerformed` returns early when there is no active
  view (`if (view == null) return;`), keeping the main path unindented.
- **Externalised configuration** — all dialog text lives in `Labels.properties`,
  not hard-coded strings, supporting i18n.
- **Template Method** — `AbstractSaveUnsavedChangesAction` defines the
  save-then-`doIt()` skeleton and leaves the destructive step (`doIt()`) abstract
  for subclasses to fill in.

---

## 4. Summary

| Principle | Where it already shows up (before the change) |
|---|---|
| SRP | Action = workflow; `SVGView` = document I/O; `ApplicationModel` = wiring; `Labels.properties` = text |
| OCP | Action hierarchy and `OutputFormat` family extended by subclassing, not editing |
| LSP | Dialog/app work against `View` and `Action`; any implementation substitutes |
| ISP | Small role interfaces: `View`, `Application`, `ApplicationModel`, `OutputFormat`, `Disposable` |
| DIP | `jhotdraw-app` depends on `jhotdraw-api` abstractions, not on concrete views/formats |
| Clean Architecture | Inward-pointing module dependencies: api ← app/actions ← samples ← core/gui |
| Clean Code | Intention-revealing names, extracted methods, guard clauses, externalised text, Template Method |

These existing qualities are what made the later "Save as PNG on close" feature a
small, localised change rather than a sprawling one.
