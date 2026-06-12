# Lab 4 — RefactoringLab: Checklist & Portfolio Artifact

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw · Lecturer: Jan Corfixen Sørensen
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`
**Feature / change request:** Add a *Save as PNG* option to the unsaved-changes close dialog.
**Refactored class:** `org.jhotdraw.app.action.AbstractSaveUnsavedChangesAction`

---

## Objective
Identify and understand **Bad Code Smells**, then apply **behaviour-preserving refactorings** to
remove them. Refactoring restructures internal code without changing external behaviour, through a
sequence of small transformations that keep the system working after each step.

---

## Checklist (tick as you go)

- [x] **1. Feature branch** — `git checkout -b Alex-feature development`, GitHub flow followed
- [x] **2. Installed SonarLint** in the IDE
- [x] **3. Found code smells** from the change request — SonarLint flagged the close-action method
- [x] **4. Identified the smell** — Long Method + high Cognitive Complexity (17, over the limit of 15)
- [x] **5. Applied refactoring patterns** — Extract Method, Guard Clause, Replace Anonymous Class with Lambda
- [x] **6. Verified behaviour preserved** — build + tests still green; SonarLint warning cleared

---

## Portfolio Work

### 1. The code smell that triggered the refactoring
**Smell:** *Long Method* and *high Cognitive Complexity* (Kerievsky, *Refactoring to Patterns*
[Ker05], Ch. 4 — "Composing Methods" / bloated methods; the smell originates as Fowler's *Long
Method*).

**Description.** The original `actionPerformed()` in `AbstractSaveUnsavedChangesAction` did too many
things at once: it checked for unsaved changes, constructed a detailed `JOptionPane` with localised
button strings, **and** processed the user's choice inside an anonymous inline `SheetListener` with
nested `if/else` branches. **SonarLint** flagged the method with a **Cognitive Complexity of 23**,
exceeding the standard limit of **15**. The deep nesting forced a reader to hold too much context at
once and made the method risky to extend — exactly the method the *Save as PNG* change request needed
to touch.

> *Evidence:* SonarLint "Cognitive Complexity" warning on `actionPerformed()` — see screenshot !slot below.
![[Screenshot 2026-06-11 160651.png]]
### 2. What I plan to change by refactoring
Break the one monolithic method into small, single-responsibility pieces so that `actionPerformed()`
reads as a high-level summary of the flow, with the dialog construction and the choice-handling
logic pulled into their own well-named methods. This flattens the nesting, clears the SonarLint
warning, and makes it safe to later inject the new *Save as PNG* branch — **without changing any
observable behaviour** (the same dialog, the same Save/Cancel/Don't Save outcomes).

### 3. Strategy of the refactoring
A sequence of **small, behaviour-preserving transformations**, each verified by re-running the build
and tests before the next:
1. Add a **guard clause** to exit early when there is no active view, removing one level of nesting.
2. **Extract** the dialog setup into `showUnsavedChangesDialog(view)` and `createSaveOptionPane(...)`.
3. **Extract** the choice dispatch into `handleSaveOptionSelected(...)`.
4. **Replace** the anonymous `SheetListener` with a **lambda**, removing boilerplate.
Using the IDE's automated *Extract Method* (right-click → Refactor, or `Ctrl+Alt+M`) guarantees the
compiler passes variable scopes, `final` modifiers and references correctly into the new signatures —
no manual copy-paste errors, a truly behaviour-preserving move (Fowler's *behaviour-preserving
transformations*).

### 4. Which refactorings I applied, and why
| Refactoring ([Ker05] / Fowler catalog) | Where | Reasoning |
|----------------------------------------|-------|-----------|
| **Extract Method** | `showUnsavedChangesDialog`, `createSaveOptionPane`, `handleSaveOptionSelected` pulled out of `actionPerformed` | The core fix for Long Method. Each extracted method does one thing, so `actionPerformed` reads top-down at one level of abstraction (Stepdown Rule) and cognitive complexity drops below the threshold. |
| **Replace Nested Conditional with Guard Clauses** | `if (view == null) return;` at the top | Removes a level of nesting and states the precondition up front, so the main flow isn't indented inside an `if`. |
| **Replace Anonymous Class with Lambda** | the `SheetListener` passed to `JSheet.showSheet` | The listener has a single method; a lambda removes the anonymous-class boilerplate and makes the dispatch call read clearly, further lowering complexity. |

The reasoning throughout is the same: each pattern reduces **cognitive load** and **cyclomatic /
cognitive complexity** by flattening branches and naming intent, so future changes (like the PNG
option) can be made confidently inside one small method without side effects on the others.

---

## Before / After (evidence)

**Before — one long, deeply nested method (cognitive complexity 17):**
```java
@Override
public void actionPerformed(ActionEvent evt) {
    final View view = getActiveView();
    if (view != null) {
        if (view.hasUnsavedChanges()) {
            ResourceBundleUtil labels =
                ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
            JOptionPane pane = new JOptionPane(/* ...lots of dialog setup... */);
            JSheet.showSheet(pane, view.getComponent(), new SheetListener() {
                @Override
                public void optionSelected(SheetEvent evt) {
                    Object value = evt.getValue();
                    if (value == null || /* ...nested checks... */) {
                        // cancel branch
                    } else if (value.equals(labels.getString("...saveOption.text"))) {
                        saveView(view);
                    } else if (value.equals(labels.getString("...dontSaveOption.text"))) {
                        doIt(view);
                    }
                }
            });
        } else {
            doIt(view);
        }
    }
}
```

**After — flat, extracted, lambda-based (complexity within limit):**
```java
@Override
public void actionPerformed(ActionEvent evt) {
    final View view = getActiveView();
    if (view == null) return;                 // 1. guard clause
    if (view.hasUnsavedChanges()) {           // 2. high-level flow only
        showUnsavedChangesDialog(view);
    } else {
        doIt(view);
    }
}

/** EXTRACTED: builds and shows the unsaved-changes dialog sheet. */
private void showUnsavedChangesDialog(final View view) {
    final ResourceBundleUtil labels =
        ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
    JOptionPane pane = createSaveOptionPane(view, labels);
    JSheet.showSheet(pane, view.getComponent(),
        evt -> handleSaveOptionSelected(evt, view, labels));   // 3. lambda
}

/** EXTRACTED: maps the user's choice to save / close / cancel. */
private void handleSaveOptionSelected(SheetEvent evt, View view,
                                      ResourceBundleUtil labels) {
    // flat dispatch — one level of abstraction
}
```

> The two functions (`showUnsavedChangesDialog`, `handleSaveOptionSelected`) carry the logic that was
> previously crammed inline; `actionPerformed` is now a short, readable summary.

![[Screenshot 2026-06-11 221509 1.png]]
![[Screenshot 2026-06-11 221517.png]]

---
## References
- [Ker05] Kerievsky, J. (2005). *Refactoring to Patterns.* Addison-Wesley. (Ch. 4 — code smells.)
- Fowler, M. (2018). *Refactoring: Improving the Design of Existing Code* (2nd ed.). Addison-Wesley.
  (Extract Method; Replace Nested Conditional with Guard Clauses.)
- Refactoring catalog — http://refactoring.com/catalog/
