# JHotDraw — Technical Overview

*(repo: [sweat-tek/JHotDraw](https://github.com/sweat-tek/JHotDraw))*

## Summary

JHotDraw is a Java GUI framework for building **structured drawing editors** — applications where users create and manipulate graphical figures (shapes, connectors, text, images) on a canvas, similar to tools like Visio, a UML editor, or a vector-graphics program. It is not an end-user application by itself; it is a *framework* you extend to build your own drawing/editing app, and it ships with several sample editors that demonstrate this.

Its main claim to fame is pedagogical and architectural: JHotDraw was deliberately designed as a showcase of object-oriented design patterns (it grew out of work associated with Erich Gamma, one of the "Gang of Four" authors). It is widely cited as a reference for how to structure a non-trivial Swing application cleanly.

This particular repo (`sweat-tek/JHotDraw`) is a fork of the original SourceForge project. Its distinguishing changes are infrastructural rather than functional: it converts the build to **Maven** and **restructures the codebase into submodules**. It's Java (~99.9%), licensed under **LGPL v2.1** and **Creative Commons Attribution 2.5**.

## What it does

At its core, JHotDraw provides the machinery you'd otherwise have to build from scratch for any diagramming application:

- A **drawing canvas** with figures that can be selected, moved, resized, grouped, and connected.
- **Tools** for creating and editing figures (selection tool, creation tools, connection tools).
- **Handles** — the small interactive grips on a selected figure used to resize, rotate, or reshape it.
- **Connectors** — relationships between figures that stay attached as figures move (essential for flowcharts, UML, ER diagrams).
- **Undo/redo**, copy/paste/drag-and-drop, persistence (saving/loading drawings), and import/export to formats like SVG.
- A full **application shell** supporting single- and multiple-document interfaces, menus, and platform conventions (including macOS-specific behaviors).

## How it works (the core model)

The conceptual heart of the framework is a small set of collaborating abstractions:

**Drawing** is the document — a container model holding a collection of figures and notifying listeners when its contents change.

**Figure** is the fundamental visual element. Every shape, line, label, or group on the canvas is a Figure. Figures know how to draw themselves, report their bounds, expose handles, and provide connectors. Complex figures are built by composing simpler ones.

**DrawingView** renders a Drawing onto the screen (a Swing component), manages the current selection, zoom, and coordinate transforms, and translates raw mouse/keyboard input into something the tools can act on. A **DrawingEditor** coordinates one or more views and owns the currently active tool.

**Tool** encapsulates an editing mode. Only one tool is active at a time; it interprets user input on the view (e.g. the rectangle tool turns a click-drag into a new rectangle figure). This is a textbook **State pattern** — switching tools switches the editor's behavior without conditional logic scattered everywhere.

**Handle** objects are the interactive decorations on selected figures. Each handle owns a small slice of editing behavior (resize from this corner, change this endpoint), which keeps figures themselves free of editing logic.

**Connector / Liner** logic keeps connections anchored to figures so relationships survive moves and resizes.

When the user drags on the canvas, the flow is roughly: the view receives the Swing event → forwards it to the active tool → the tool mutates the drawing model (adds/moves/edits figures) → the model fires change events → the view repaints, and undo information is recorded along the way.

## Architecture and design patterns

JHotDraw is essentially a **Model–View–Controller** framework adapted for direct-manipulation graphics:

- **Model** = Drawing + Figures (the data and its structure).
- **View** = DrawingView (rendering + selection state).
- **Controller** = Tools, Handles, and the Editor (input interpretation and command dispatch).

It leans heavily and intentionally on classic design patterns, which is much of why it's studied:

- **Observer** — figures, drawings, and views communicate through change-notification listeners, decoupling the model from rendering.
- **State** — the active Tool determines how input is interpreted.
- **Command** — user actions are reified as command/action objects, which is also what makes undo/redo systematic.
- **Composite** — group figures contain other figures and are treated uniformly with leaf figures.
- **Strategy / Decorator** — pluggable behaviors for things like figure decoration, connection routing (liners), and locators.
- **Factory / Prototype** — tools often create new figures by cloning prototype instances.

The result is a framework where you build a new editor mostly by *configuring and subclassing* — defining your figure types, registering tools and actions, and assembling an application — rather than rewriting the event and rendering plumbing.

## What this fork changes

The functional framework is inherited from upstream JHotDraw. This fork's contribution is modernizing the project structure:

- **Maven build** replacing the original build setup, giving standard dependency management and a reproducible build lifecycle (`pom.xml` at the root, aggregating modules).
- **Modular layout** — the code is split into focused submodules, each a buildable unit:

| Module | Responsibility |
|---|---|
| `jhotdraw-core` | Core framework: Drawing, Figure, DrawingView, editor model |
| `jhotdraw-api` | API/interface surface separating contracts from implementation |
| `jhotdraw-actions` | Reusable editing actions/commands (undoable operations) |
| `jhotdraw-app` | Application shell: document model, menus, single/multi-document UI |
| `jhotdraw-gui` | Reusable Swing GUI widgets and supporting UI components |
| `jhotdraw-datatransfer` | Clipboard, copy/paste, drag-and-drop support |
| `jhotdraw-xml` | XML-based persistence (reading/writing drawings) |
| `jhotdraw-utils` | Shared utilities and helpers |
| `jhotdraw-samples` | Example editors (e.g. SVG editor, drawing apps) demonstrating the framework |

This separation makes dependencies between layers explicit — for example, sample apps depend on `core`, `app`, and `gui`; persistence lives apart from the figure model; and the API module helps keep contracts distinct from implementations.

## Technology stack

- **Language:** Java (the project is ~99.9% Java).
- **UI toolkit:** Java Swing / AWT (it predates and does not use JavaFX).
- **Build:** Maven, multi-module (reactor) build.
- **Persistence/interchange:** XML-based drawing format; SVG import/export in the sample editors.
- **Licensing:** LGPL v2.1 and Creative Commons Attribution 2.5 — meaning you can use it in your own (including commercial) applications under LGPL terms.

## When you'd use it

JHotDraw is a strong fit if you need to build a **2D structured-graphics editor** in the Java/Swing world — diagram tools, schematic editors, UML or workflow designers, annotation tools — and want a proven figure/tool/handle architecture instead of building one. It's equally valued as a **study reference** for applying design patterns to a real, sizable codebase. It's a less natural choice for greenfield apps that want a modern toolkit (JavaFX/web), since it's firmly rooted in Swing.
