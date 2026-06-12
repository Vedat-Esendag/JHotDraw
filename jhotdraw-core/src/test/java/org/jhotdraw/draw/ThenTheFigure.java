package org.jhotdraw.draw;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;
import org.jhotdraw.draw.figure.Figure;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenTheFigure extends Stage<ThenTheFigure> {

    @ExpectedScenarioState
    QuadTreeDrawing drawing;

    @ScenarioState(resolution = ScenarioState.Resolution.NAME)
    Figure figureA, figureB, figureC;

    public ThenTheFigure should_appear_one_position_higher_in_the_stack() {
        assertThat(drawing.indexOf(figureA)).isEqualTo(1);
        return self();
    }

    public ThenTheFigure should_appear_one_position_lower_in_the_stack() {
        assertThat(drawing.indexOf(figureC)).isEqualTo(1);
        return self();
    }

    public ThenTheFigure the_order_should_remain_unchanged() {
        assertThat(drawing.indexOf(figureA)).isEqualTo(0);
        assertThat(drawing.indexOf(figureB)).isEqualTo(1);
        assertThat(drawing.indexOf(figureC)).isEqualTo(2);
        return self();
    }
}