# Lab 2 — CLLab (Concept Location Lab): Checklist & Portfolio Artifact

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`
**Feature / change request:** Add a *Save as PNG* option to the unsaved-changes close dialog.

---

## Objective
Apply the IDE debugger to locate the feature concept at runtime, and produce the **initial set of
classes** for the change request as a `Domain Class | Responsibility` table. Features typically start
from controller classes; where many classes are involved, only the domain classes tied to the
concept are localized.

---

## Checklist (tick as you go)

- [x] **1. Identified the trigger** — closing/exiting a view with unsaved changes
- [x] **2. Set breakpoints** in the controller where the unsaved-changes dialog is built and where the user's choice is dispatched
- [x] **3. Ran in debug mode** and stepped through to follow the concept from the controller into the view and the save infrastructure
- [x] **4. Localized the domain classes** that own the concept (the close-time save workflow)
- [x] **5. Recorded the initial set of classes** in the table below (the portfolio artifact)

> **Scope note:** this is the *baseline* concept location — the existing dialog offers only
> **Save / Cancel / Don't Save** and there is no PNG capability yet. It establishes where the
> *Save as PNG* change will later be introduced.

---

## How the concept was located
The feature is triggered when the user closes a view with unsaved changes. Following the guideline
that features start from controller classes, the entry point is the close/exit action. Breakpoints
were set where the unsaved-changes dialog is constructed and where the user's button choice is
dispatched. Stepping through the debugger, the destructive action is gated by
`AbstractSaveUnsavedChangesAction`; document writing is delegated to the concrete `View`
(here `SVGView`), the save dialog is built by `SVGApplicationModel`, and the dialog's button text
comes from `Labels.properties`. `JSheet` / `JFileURIChooser` provide the modal sheet and file
chooser. These are the domain classes that own the concept.

---

## Portfolio Artifact — Initial Set of Classes

| Domain Class                                                      | Responsibility                                                                                                                                                                                                                                                    |
| ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **AbstractSaveUnsavedChangesAction**<br>`org.jhotdraw.app.action` | Controller for closing/exiting. On a destructive action it checks `hasUnsavedChanges()` and shows the dialog with three options — Save, Cancel, Don't Save — then dispatches the choice: Save → `saveView()`, Don't Save → `doIt()`, Cancel → re-enable the view. |
| **View (interface)**<br>`org.jhotdraw.api.app`                    | Abstraction of an open document. Declares `write(uri, chooser)`, `canSaveTo(uri)`, `getURI()` / `setURI()`, `hasUnsavedChanges()`, `markChangesAsSaved()` — the contract the dialog calls into. No PNG capability.                                                |
| **SVGView**<br>`org.jhotdraw.samples.svg`                         | Concrete view for SVG drawings. `write()` always serialises the drawing to SVG via `SVGOutputFormat`; `canSaveTo()` accepts only `.svg` / `.svgz`.                                                                                                                |
| **SVGApplicationModel**<br>`org.jhotdraw.samples.svg`             | Application factory. `createSaveChooser()` builds the save dialog and registers the available output format(s) for the view.                                                                                                                                      |
| **Labels.properties**<br>`org.jhotdraw.app` (resource)            | Resource file holding the dialog's button text (`file.saveBefore.saveOption.text`, `...cancelOption.text`, `...dontSaveOption.text`).                                                                                                                             |
| **JSheet / JFileURIChooser**<br>`org.jhotdraw.gui`                | GUI infrastructure — `JSheet` shows the modal dialog / save sheet; `JFileURIChooser` is the file chooser used when saving.                                                                                                                                        |

> `Labels.properties` is a resource, not a class, but it participates in the concept by supplying the
> dialog's button labels and is included for completeness.

---

