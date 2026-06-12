/*
 * Behaviour-Driven Development scenarios for the "Save as PNG on close" feature,
 * automated with JGiven (Given-When-Then) and AssertJ assertions.
 *
 * User story
 * ----------
 *   As a user, I want to choose what type to save the drawing as (e.g. PNG)
 *   when I close the app, so that I do not have to export and then close —
 *   saving time.
 *
 * Each @Test below reads as a Given-When-Then scenario. JGiven turns the
 * step-method names into a human-readable report (target/jgiven-reports).
 */
package org.jhotdraw.app.action.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

public class SaveAsPngOnCloseScenarioTest
        extends ScenarioTest<GivenDrawing, WhenClose, ThenSaved> {

    /**
     * Scenario: the happy path from the user story — pick PNG on close and the
     * drawing is saved as a .png without a separate Export step.
     */
    @Test
    public void user_can_save_the_drawing_as_PNG_when_closing() throws Exception {
        given().a_drawing_that_supports_PNG_export()
               .and().the_user_chooses_the_file_name("my-drawing.png");

        when().the_user_closes_the_app_and_selects_save_as_PNG();

        then().the_save_as_PNG_option_is_offered()
              .and().the_drawing_is_exported_to("my-drawing.png")
              .and().the_exported_file_is_a_PNG();
    }

    /**
     * Scenario: the variation that saves the user time — they type a name with
     * no extension, and the system still produces a valid .png file.
     */
    @Test
    public void a_name_without_extension_is_saved_as_PNG_automatically() throws Exception {
        given().a_drawing_that_supports_PNG_export()
               .and().the_user_chooses_the_file_name("my-drawing");

        when().the_user_closes_the_app_and_selects_save_as_PNG();

        then().the_drawing_is_exported_to("my-drawing.png")
              .and().the_exported_file_is_a_PNG();
    }

    /**
     * Scenario: a wrong-case extension is accepted as-is (no double extension),
     * so the user is not surprised by "drawing.PNG.png".
     */
    @Test
    public void an_uppercase_PNG_extension_is_kept_as_is() throws Exception {
        given().a_drawing_that_supports_PNG_export()
               .and().the_user_chooses_the_file_name("DRAWING.PNG");

        when().the_user_closes_the_app_and_selects_save_as_PNG();

        then().the_drawing_is_exported_to("DRAWING.PNG")
              .and().the_exported_file_is_a_PNG();
    }

    /**
     * Scenario: a view that cannot rasterise itself must not offer the option,
     * and nothing is exported.
     */
    @Test
    public void the_option_is_hidden_for_views_that_cannot_export_PNG() throws Exception {
        given().a_drawing_that_cannot_export_PNG()
               .and().the_user_chooses_the_file_name("my-drawing.png");

        when().the_user_closes_the_app_and_selects_save_as_PNG();

        then().the_save_as_PNG_option_is_not_offered()
              .and().no_PNG_export_happens();
    }
}
