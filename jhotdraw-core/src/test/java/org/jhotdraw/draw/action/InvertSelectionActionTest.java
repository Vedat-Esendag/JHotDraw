/*
 * @(#)InvertSelectionActionTest.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link InvertSelectionAction}.
 * <p>
 * {@code InvertSelectionAction} selects every <em>selectable</em> figure that
 * is currently not selected and deselects the rest (an empty selection inverts
 * to "select all"). These tests exercise only that logic: every collaborator
 * ({@link DrawingEditor}, {@link DrawingView}, {@link Drawing} and
 * {@link Figure}) is a Mockito mock, so no real Swing component, drawing model
 * or rendering is involved.
 * <p>
 * The action resolves its view through
 * {@code AbstractSelectedAction.getView()}, which returns
 * {@code editor.getActiveView()}; it then reads
 * {@code view.getSelectedFigures()} and {@code view.getDrawing().getChildren()},
 * calls {@code view.clearSelection()} and finally
 * {@code view.addToSelection(Collection)} with the inverted figures.
 *
 * @see InvertSelectionAction
 */
public class InvertSelectionActionTest {

    private DrawingEditor editor;
    private DrawingView view;
    private Drawing drawing;

    @Before
    public void setUp() {
        editor = mock(DrawingEditor.class);
        view = mock(DrawingView.class);
        drawing = mock(Drawing.class);
        // AbstractSelectedAction.getView() resolves through editor.getActiveView().
        when(editor.getActiveView()).thenReturn(view);
        when(view.getDrawing()).thenReturn(drawing);
    }

    /** Creates a named mock figure with the given selectable flag. */
    private Figure figure(String name, boolean selectable) {
        Figure f = mock(Figure.class, name);
        when(f.isSelectable()).thenReturn(selectable);
        return f;
    }

    /** Stubs the drawing's children and the view's current selection. */
    private void scene(List<Figure> children, Set<Figure> selected) {
        when(drawing.getChildren()).thenReturn(children);
        when(view.getSelectedFigures()).thenReturn(selected);
    }

    /** Captures the single {@code addToSelection(Collection)} argument. */
    @SuppressWarnings("unchecked")
    private Collection<Figure> capturedAddToSelection() {
        ArgumentCaptor<Collection<Figure>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(view).addToSelection(captor.capture());
        return captor.getValue();
    }

    /**
     * Best case: 4 selectable figures, 2 of them selected. Inverting selects
     * exactly the other 2 and clears the old selection first.
     */
    @Test
    public void bestCase_someSelected_invertsToComplement() {
        Figure a = figure("a", true);
        Figure b = figure("b", true);
        Figure c = figure("c", true);
        Figure d = figure("d", true);
        scene(Arrays.asList(a, b, c, d), new LinkedHashSet<>(Arrays.asList(a, b)));

        new InvertSelectionAction(editor).invertSelection();

        verify(view).clearSelection();
        Collection<Figure> added = capturedAddToSelection();
        assertEquals(2, added.size());
        assertTrue("c was unselected, so it must become selected", added.contains(c));
        assertTrue("d was unselected, so it must become selected", added.contains(d));
        assertFalse("a was selected, so it must be deselected", added.contains(a));
        assertFalse("b was selected, so it must be deselected", added.contains(b));
    }

    /** Boundary: nothing selected -> inverting behaves like "select all". */
    @Test
    public void boundary_noneSelected_selectsAll() {
        Figure a = figure("a", true);
        Figure b = figure("b", true);
        Figure c = figure("c", true);
        scene(Arrays.asList(a, b, c), new LinkedHashSet<Figure>());

        new InvertSelectionAction(editor).invertSelection();

        verify(view).clearSelection();
        Collection<Figure> added = capturedAddToSelection();
        assertEquals(3, added.size());
        assertTrue(added.contains(a));
        assertTrue(added.contains(b));
        assertTrue(added.contains(c));
    }

    /** Boundary: everything selected -> nothing remains to select. */
    @Test
    public void boundary_allSelected_selectsNone() {
        Figure a = figure("a", true);
        Figure b = figure("b", true);
        List<Figure> all = Arrays.asList(a, b);
        scene(all, new LinkedHashSet<>(all));

        new InvertSelectionAction(editor).invertSelection();

        verify(view).clearSelection();
        Collection<Figure> added = capturedAddToSelection();
        assertTrue("no figure should be added when all were already selected", added.isEmpty());
    }

    /**
     * Boundary: a non-selectable figure is never added to the selection, even
     * though it is not currently selected.
     */
    @Test
    public void boundary_nonSelectableExcluded() {
        Figure selectable = figure("selectable", true);
        Figure locked = figure("locked", false);
        scene(Arrays.asList(selectable, locked), new LinkedHashSet<Figure>());

        new InvertSelectionAction(editor).invertSelection();

        verify(view).clearSelection();
        Collection<Figure> added = capturedAddToSelection();
        assertEquals(1, added.size());
        assertTrue(added.contains(selectable));
        assertFalse("a non-selectable figure must never be selected", added.contains(locked));
    }

    /**
     * Boundary: no active view -> {@code actionPerformed} is a no-op. It must
     * not throw and must not touch the view at all.
     */
    @Test
    public void boundary_noView_doesNothing() {
        when(editor.getActiveView()).thenReturn(null);

        InvertSelectionAction action = new InvertSelectionAction(editor);
        action.actionPerformed(null); // must not throw

        verify(view, never()).getSelectedFigures();
        verify(view, never()).clearSelection();
        verify(view, never()).addToSelection(anyCollection());
    }
}
