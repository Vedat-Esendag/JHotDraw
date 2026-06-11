/*
 * @(#)InvertSelectionBddTest.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * BDD acceptance tests for {@link InvertSelectionAction}.
 * <p>
 * These scenarios automate the Lab&nbsp;2 "Invert Selection" user-story
 * acceptance criteria as Given/When/Then scenarios using
 * <a href="https://jgiven.org">JGiven</a> (JUnit&nbsp;4 integration) and
 * AssertJ for the domain assertions:
 * <ol>
 *   <li>Given some figures are selected, When I invert, Then those become
 *       deselected and every <em>other</em> selectable figure becomes
 *       selected.</li>
 *   <li>Given nothing is selected, When I invert, Then all selectable figures
 *       become selected.</li>
 *   <li>Given all figures are selected, When I invert, Then nothing is
 *       selected.</li>
 * </ol>
 * <p>
 * The collaborators ({@link DrawingEditor}, {@link DrawingView},
 * {@link Drawing} and {@link Figure}) are Mockito mocks, wired exactly like
 * {@link InvertSelectionActionTest}: the action resolves its view through
 * {@code editor.getActiveView()}, reads {@code view.getSelectedFigures()} and
 * {@code view.getDrawing().getChildren()}, then calls
 * {@code view.clearSelection()} followed by {@code view.addToSelection(...)}.
 * We assert on that captured {@code addToSelection} argument -- the action's
 * domain behaviour -- rather than by driving a live Swing GUI (which is why
 * AssertJ-Swing is intentionally not used).
 *
 * @see InvertSelectionAction
 * @see InvertSelectionActionTest
 */
public class InvertSelectionBddTest extends ScenarioTest<
        InvertSelectionBddTest.GivenADrawing,
        InvertSelectionBddTest.WhenTheUserInverts,
        InvertSelectionBddTest.ThenTheSelection> {

    /** Criterion 1: some selected -> invert selects exactly the complement. */
    @Test
    public void some_selected_inverts_to_the_complement() {
        given().a_drawing_with_$_selectable_figures(4)
                .and().the_first_$_figures_are_selected(2);

        when().the_user_invokes_invert_selection();

        then().the_previous_selection_is_cleared()
                .and().the_originally_selected_figures_are_deselected()
                .and().every_other_selectable_figure_becomes_selected();
    }

    /** Criterion 2: nothing selected -> invert behaves like "select all". */
    @Test
    public void nothing_selected_selects_all() {
        given().a_drawing_with_$_selectable_figures(3)
                .and().nothing_is_selected();

        when().the_user_invokes_invert_selection();

        then().the_previous_selection_is_cleared()
                .and().all_selectable_figures_become_selected();
    }

    /** Criterion 3: all selected -> invert leaves nothing selected. */
    @Test
    public void all_selected_selects_none() {
        given().a_drawing_with_$_selectable_figures(2)
                .and().all_figures_are_selected();

        when().the_user_invokes_invert_selection();

        then().the_previous_selection_is_cleared()
                .and().nothing_becomes_selected();
    }

    // ---- GIVEN ------------------------------------------------------------

    /** Builds a mocked drawing/view and a configurable current selection. */
    public static class GivenADrawing extends Stage<GivenADrawing> {

        @ScenarioState DrawingEditor editor;
        @ScenarioState DrawingView view;
        @ScenarioState List<Figure> allFigures;
        @ScenarioState Set<Figure> selectedFigures;

        private Drawing drawing;

        @BeforeStage
        public void setUpMocks() {
            editor = mock(DrawingEditor.class);
            view = mock(DrawingView.class);
            drawing = mock(Drawing.class);
            // AbstractSelectedAction.getView() resolves through editor.getActiveView().
            Mockito.when(editor.getActiveView()).thenReturn(view);
            Mockito.when(view.getDrawing()).thenReturn(drawing);
            allFigures = new ArrayList<>();
            selectedFigures = new LinkedHashSet<>();
            // The action reads the selection at WHEN time, so stubbing it to the
            // (initially empty) set once and mutating that set in the steps below
            // is enough -- no re-stubbing needed.
            Mockito.when(view.getSelectedFigures()).thenReturn(selectedFigures);
        }

        public GivenADrawing a_drawing_with_$_selectable_figures(int count) {
            for (int i = 0; i < count; i++) {
                Figure f = mock(Figure.class, "figure-" + i);
                Mockito.when(f.isSelectable()).thenReturn(true);
                allFigures.add(f);
            }
            Mockito.when(drawing.getChildren()).thenReturn(allFigures);
            return self();
        }

        public GivenADrawing the_first_$_figures_are_selected(int n) {
            selectedFigures.addAll(allFigures.subList(0, n));
            return self();
        }

        public GivenADrawing nothing_is_selected() {
            selectedFigures.clear();
            return self();
        }

        public GivenADrawing all_figures_are_selected() {
            selectedFigures.addAll(allFigures);
            return self();
        }
    }

    // ---- WHEN -------------------------------------------------------------

    /** Invokes Invert Selection through the action's {@code actionPerformed}. */
    public static class WhenTheUserInverts extends Stage<WhenTheUserInverts> {

        @ScenarioState DrawingEditor editor;

        public WhenTheUserInverts the_user_invokes_invert_selection() {
            // actionPerformed delegates to invertSelection(); the event is unused.
            new InvertSelectionAction(editor).actionPerformed(null);
            return self();
        }
    }

    // ---- THEN -------------------------------------------------------------

    /** Asserts on the captured selection and that the old one was cleared. */
    public static class ThenTheSelection extends Stage<ThenTheSelection> {

        @ScenarioState DrawingView view;
        @ScenarioState List<Figure> allFigures;
        @ScenarioState Set<Figure> selectedFigures;

        private Collection<Figure> added;

        /** Captures the single {@code addToSelection(Collection)} argument. */
        @BeforeStage
        @SuppressWarnings("unchecked")
        public void captureAddToSelection() {
            ArgumentCaptor<Collection<Figure>> captor = ArgumentCaptor.forClass(Collection.class);
            verify(view).addToSelection(captor.capture());
            added = captor.getValue();
        }

        public ThenTheSelection the_previous_selection_is_cleared() {
            verify(view).clearSelection();
            return self();
        }

        public ThenTheSelection the_originally_selected_figures_are_deselected() {
            assertThat(added).doesNotContainAnyElementsOf(selectedFigures);
            return self();
        }

        public ThenTheSelection every_other_selectable_figure_becomes_selected() {
            assertThat(added).containsExactlyInAnyOrderElementsOf(complement());
            return self();
        }

        public ThenTheSelection all_selectable_figures_become_selected() {
            assertThat(added).containsExactlyInAnyOrderElementsOf(allFigures);
            return self();
        }

        public ThenTheSelection nothing_becomes_selected() {
            assertThat(added).isEmpty();
            return self();
        }

        /** Every selectable figure that was not selected before inverting. */
        private List<Figure> complement() {
            List<Figure> complement = new ArrayList<>();
            for (Figure f : allFigures) {
                if (!selectedFigures.contains(f)) {
                    complement.add(f);
                }
            }
            return complement;
        }
    }
}
