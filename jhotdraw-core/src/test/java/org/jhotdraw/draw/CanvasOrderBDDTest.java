package org.jhotdraw.draw;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

public class CanvasOrderBDDTest extends ScenarioTest<GivenACanvas, WhenTheArtist, ThenTheFigure> {

    @Test
    public void artist_can_bring_a_figure_forward() {
        given().a_canvas_with_three_overlapping_figures();
        when().brings_the_bottom_figure_forward();
        then().should_appear_one_position_higher_in_the_stack();
    }

    @Test
    public void artist_can_send_a_figure_backward() {
        given().a_canvas_with_three_overlapping_figures();
        when().sends_the_top_figure_backward();
        then().should_appear_one_position_lower_in_the_stack();
    }

    @Test
    public void bring_forward_has_no_effect_at_the_front() {
        given().a_canvas_with_three_overlapping_figures();
        when().brings_the_frontmost_figure_forward();
        then().the_order_should_remain_unchanged();
    }

    @Test
    public void send_backward_has_no_effect_at_the_back() {
        given().a_canvas_with_three_overlapping_figures();
        when().sends_the_backmost_figure_backward();
        then().the_order_should_remain_unchanged();
    }
}