# Lab 2 — ChangeReqLab (CASE Study Lab): Checklist & Portfolio Artifact

**Course:** Software Maintenance (5 ECTS) · **CASE study:** JHotDraw
**Fork:** https://github.com/Vedat-Esendag/JHotDraw · **My branch:** `Alex-feature`

---

## Objective
A software change starts with a **change request** (new feature, bug fix, or improvement).
This lab's objective is to select an existing JHotDraw feature, write a **user story** for it from
the user's perspective, and register it as a backlog card. The user story is the mandatory
portfolio artifact and becomes the feature carried through every later lab.

---

## Checklist (tick as you go)

- [x] **1. Fork created** — https://github.com/Vedat-Esendag/JHotDraw (GitHub flow, branch `Alex-feature`)
- [x] **2. Selected an existing feature** to base the change request on — the unsaved-changes close dialog in the SVG sample
- [x] **3. Defined the change request** — add a fourth option, *Save as PNG*, to that dialog
- [x] **4. Wrote the user story** in the standard template (below)
- [x] **5. Wrote acceptance criteria** for the story
- [x] **6. Created a GitHub Projects card** and placed it in the TODO / Backlog column

---

## Change Request (summary)
When a user closes a drawing that has unsaved changes, the application shows a dialog offering
**Save**, **Cancel**, and **Don't Save**. This change request adds a fourth option, **Save as PNG**,
that lets the user export the drawing as a PNG raster image on the way out, then proceeds to close
the view.

**Selected feature & rationale.** JHotDraw's SVG sample lets a user create vector drawings and saves
them in SVG format. When closing with unsaved work, the application warns the user via an
unsaved-changes dialog. Many users, however, want a quick raster (PNG) copy of their drawing — for
sharing, embedding in documents, or previewing — without going through the separate Export menu.
This change request surfaces that capability directly in the close-time dialog.

---

## Portfolio Artifact — User Story

> **As a** JHotDraw user who is closing a drawing with unsaved changes,
> **I want** an additional "Save as PNG" button in the unsaved-changes dialog (alongside Save,
> Cancel, and Don't Save),
> **so that** I can export my drawing as a PNG image at the moment I close it, instead of losing my
> work or having to cancel and hunt for a separate export command.

### Acceptance criteria
- Given a view with unsaved changes, when I trigger close, the dialog shows a fourth button
  labelled **Save as PNG** — but only for views that can produce a raster image.
- When I choose **Save as PNG**, a save dialog opens pre-filtered to the `.png` extension.
- If I confirm a location, the drawing is written as a valid PNG and the view then closes.
- If the chosen filename has no extension, `.png` is appended automatically.
- Choosing **Save as PNG** does not mark the native document as saved (PNG is an export, not the
  document's SVG format), but it still allows the close to proceed.
- The existing **Save**, **Cancel**, and **Don't Save** behaviours are unchanged.

---

## Backlog card (GitHub Projects)

| Field | Value |
|-------|-------|
| Card title | Add "Save as PNG" option to unsaved-changes close dialog |
| Type | New feature |
| Column | TODO / Backlog |
| Repository | Fork of JHotDraw (GitHub flow: feature branch → PR) |
| Assignee / branch | `Alex-feature` |

---

## Screenshots to capture (evidence)

![[Screenshot 2026-06-11 221059.png]]