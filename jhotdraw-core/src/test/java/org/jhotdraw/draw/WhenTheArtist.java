package org.jhotdraw.draw;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;
import org.jhotdraw.draw.figure.Figure;

public class WhenTheArtist extends Stage<WhenTheArtist> {

    @ExpectedScenarioState
    QuadTreeDrawing drawing;

    @ScenarioState(resolution = ScenarioState.Resolution.NAME)
    Figure figureA, figureB, figureC;

    public WhenTheArtist brings_the_bottom_figure_forward() {
        drawing.bringForward(figureA);
        return self();
    }

    public WhenTheArtist sends_the_top_figure_backward() {
        drawing.sendBackward(figureC);
        return self();
    }

    public WhenTheArtist brings_the_frontmost_figure_forward() {
        drawing.bringForward(figureC);
        return self();
    }

    public WhenTheArtist sends_the_backmost_figure_backward() {
        drawing.sendBackward(figureA);
        return self();
    }
}