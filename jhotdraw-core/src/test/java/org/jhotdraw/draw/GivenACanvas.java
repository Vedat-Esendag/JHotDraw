package org.jhotdraw.draw;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;
import org.jhotdraw.draw.figure.Figure;
import org.mockito.Mockito;

import java.awt.geom.Rectangle2D;

public class GivenACanvas extends Stage<GivenACanvas> {

    @ProvidedScenarioState
    QuadTreeDrawing drawing;

    // Force JGiven to share these fields by matching their names across stages
    @ScenarioState(resolution = ScenarioState.Resolution.NAME)
    Figure figureA, figureB, figureC;

    public GivenACanvas a_canvas_with_three_overlapping_figures() {
        drawing = new QuadTreeDrawing();

        figureA = Mockito.mock(Figure.class);
        figureB = Mockito.mock(Figure.class);
        figureC = Mockito.mock(Figure.class);

        Mockito.when(figureA.getDrawingArea()).thenReturn(new Rectangle2D.Double());
        Mockito.when(figureB.getDrawingArea()).thenReturn(new Rectangle2D.Double());
        Mockito.when(figureC.getDrawingArea()).thenReturn(new Rectangle2D.Double());

        drawing.add(figureA); // index 0 — back
        drawing.add(figureB); // index 1
        drawing.add(figureC); // index 2 — front
        return self();
    }
}