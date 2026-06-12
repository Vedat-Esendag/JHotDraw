# Lab 3 — AnalysisLab (Impact Analysis Lab): Checklist & Portfolio Artifact

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`
**Feature / change request:** Add a *Save as PNG* option to the unsaved-changes close dialog.

---

## Objective
Apply **static** and **dynamic** analysis to find the **Estimated Impact Set** of classes for the
change request, following the activities in Raj13 Figure 7.9 (start from the located concept, mark
neighbours NEXT, then classify as CHANGED / UNCHANGED / PROPAGATES). The portfolio artifact is
**Table 1** — the packages visited, how many classes, and what each contributes.

---

## Checklist (tick as you go)

- [x] **1. Start from the located concept** — `AbstractSaveUnsavedChangesAction` (from Concept Location), mark **CHANGED**
- [x] **2. Mark BLANK neighbours NEXT** — callers, the View abstraction, the image writer, choosers
- [x] **3. Classify each neighbour** — CHANGED / UNCHANGED / PROPAGATES
- [x] **4. Static analysis** — "Find Usages" / "Go to Implementation" to map class hierarchy & dependencies
- [x] **5. Dynamic analysis** — breakpoint in `actionPerformed()`, run in debug, confirm the close path
- [x] **6. Fill Table 1** — packages visited + # classes + comments (the artifact)

---

## Static Impact Analysis
Using "Find Usages" and "Go to Implementation," the change ripples from the controller outward.
`CloseFileAction` inherits from `AbstractSaveUnsavedChangesAction`, so a change to the parent applies
to the close button without editing the subclass. The controller forms a new dependency on the
`View` abstraction (a new hook), which propagates to the concrete `SVGView`. The image writer and
UI/chooser classes are visited but reused as-is.

**See:** `diagrams/01_class_diagram` (static structure) and `diagrams/04_package_dependency`.

### Estimated Impact Set (Raj13 classification)
| Class | Mark | Reason |
|-------|------|--------|
| AbstractSaveUnsavedChangesAction | **CHANGED** | New button added to dialog; new handler branch; new `exportViewToPNG` logic. |
| View (interface) | **CHANGED** | Two new default methods: `exportToPNG()` and `canExportToPNG()`. |
| SVGView | **CHANGED** | Overrides the new hooks to write a real PNG via the image format. |
| Labels.properties | **CHANGED** | New label key for the "Save as PNG" button text. |
| CloseFileAction / ExitAction | UNCHANGED | Inherit the parent; behaviourally impacted but no code change. |
| ImageOutputFormat | UNCHANGED | Reused for PNG rasterising; no modification needed. |
| SVGApplicationModel | UNCHANGED | Already registers SVG + PNG output formats; visited, not modified. |
| JSheet / JFileURIChooser | PROPAGATES | UI infra reused; the change propagates into how the PNG-filtered save sheet is built. |

**See:** `diagrams/03_impact_set_graph` (the ripple, colour-coded by mark).

---

## Dynamic Impact Analysis
Breakpoints were placed in `AbstractSaveUnsavedChangesAction.actionPerformed()` and JHotDraw was run
in debug mode. Drawing a shape and clicking **Close** halted execution at the breakpoint, confirming
this is the exact moment the unsaved-changes dialog is spawned. A modification was injected into the
Save branch to also export a PNG; after recompiling, the trace confirmed the drawing is routed
through `ImageOutputFormat` and a PNG file is produced.

**See:** `diagrams/02_sequence_diagram` (runtime flow).

```java
if (option == JOptionPane.YES_OPTION) {
    saveView(view);        // standard save routine
    exportToPNG(view);     // MODIFICATION: also export as PNG
}
```

---

## Portfolio Artifact — Table 1: Packages visited during impact analysis

| Package name                   | # of classes | Comments                                                                                                                                                                          |
| ------------------------------ | :----------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `org.jhotdraw.app.action`      |      1       | Hosts the unsaved-changes controller — the heart of the change. Showed where JHotDraw centralises "save before destructive action" logic shared by all apps.                      |
| `org.jhotdraw.app.action.file` |      3       | `CloseFileAction`, `ExitAction`, `ExportFileAction`. `CloseFileAction` delegates entirely to the parent; `ExportFileAction` showed the correct way to invoke `ImageOutputFormat`. |
| `org.jhotdraw.api.app`         |      2       | The `View` abstraction (and `ApplicationModel`). Java default methods let the feature be added generically without breaking existing views.                                       |
| `org.jhotdraw.draw.io`         |      2       | `ImageOutputFormat` and `OutputFormat`. JHotDraw separates standard saving (XML serialisation) from raster export; the feature reuses the existing PNG rasteriser.                |
| `org.jhotdraw.samples.svg`     |      2       | `SVGView` + `SVGApplicationModel` — where the abstract hook becomes a real PNG write and output formats are registered.                                                           |
| `org.jhotdraw.gui`             |      2       | Swing helpers (`JSheet`, `JFileURIChooser`) reused to present the dialog and a PNG-filtered save sheet.                                                                           |

**Summary.** Fulfilling the feature bridges two previously separate systems in JHotDraw: the
**application-lifecycle** system (closing/saving) and the **I/O/export** system (rendering PNGs).
The CHANGED footprint is small — the controller, the `View` interface, one concrete view, and a
resource file — but understanding it required traversing five packages.

---

## Diagrams for this lab (in `diagrams/`, editable PlantUML `.puml`)
Render at https://www.plantuml.com/plantuml or with the PlantUML IDE plugin — no install needed.
1. `01_class_diagram.puml` — static UML class diagram of the work area (CHANGED/UNCHANGED marks).
2. `02_sequence_diagram.puml` — dynamic runtime flow of the close-and-export path.
3. `03_impact_set_graph.puml` — the Raj13 impact-set ripple, colour-coded by mark.
4. `04_package_dependency.puml` — the packages and how they depend on the stable `api` abstraction.

## Screenshots to capture (evidence)
Diagrams?


In the fourth column, mention if the class is related to the concept. Use one of the following terms:

●       Use **“Unchanged”** if the class has no relation to the concept but you have visited it.  

●       Use **“Propagating”** if you read the source code of the class and it guides you to the location of the concept, but you will not change it. 

● 
	Use **“Changed”** if the class will be changed.