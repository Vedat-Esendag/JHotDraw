/*
 * JGiven GIVEN stage for the "choose save type (PNG) on close" feature.
 *
 * User story:
 *   As a user, I want to choose what type to save the drawing as (e.g. PNG)
 *   when I close the app, so that I do not have to export and then close —
 *   saving time.
 */
package org.jhotdraw.app.action.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import org.jhotdraw.api.app.View;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;

/** Sets up the drawing/view and the file name the user typed. */
public class GivenDrawing extends Stage<GivenDrawing> {

    @ProvidedScenarioState
    ScenarioContext context = new ScenarioContext();

    @As("a drawing with unsaved changes that can be exported as PNG")
    public GivenDrawing a_drawing_that_supports_PNG_export() {
        View view = mock(View.class);
        Mockito.when(view.canExportToPNG()).thenReturn(true);
        context.view = view;
        return this;
    }

    @As("a drawing whose view cannot export PNG")
    public GivenDrawing a_drawing_that_cannot_export_PNG() {
        View view = mock(View.class);
        Mockito.when(view.canExportToPNG()).thenReturn(false);
        context.view = view;
        return this;
    }

    @As("the user chooses the file name $")
    public GivenDrawing the_user_chooses_the_file_name(@Quoted String fileName) {
        context.chosenFile = new java.io.File(fileName).toURI();
        return this;
    }
}
