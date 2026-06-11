/*
 * JGiven WHEN stage: the user closes the app and selects "Save as PNG".
 */
package org.jhotdraw.app.action.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import java.io.IOException;
import org.jhotdraw.app.action.AbstractSaveUnsavedChangesAction;

/**
 * Exercises the production behaviour: decide whether the "Save as PNG" option
 * is offered, normalise the chosen file name, and (if applicable) ask the view
 * to export the PNG.
 */
public class WhenClose extends Stage<WhenClose> {

    @ProvidedScenarioState
    ScenarioContext context;

    @As("the user closes the app and selects Save as PNG")
    public WhenClose the_user_closes_the_app_and_selects_save_as_PNG() throws IOException {
        // The dialog only offers the PNG option for views that can export it.
        context.pngOptionOffered = context.view.canExportToPNG();

        if (context.pngOptionOffered && context.chosenFile != null) {
            // Domain rule under test: force the .png extension before writing.
            context.exportedFile =
                    AbstractSaveUnsavedChangesAction.ensurePngExtension(context.chosenFile);
            context.view.exportToPNG(context.exportedFile);
            context.exportPerformed = true;
        }
        return this;
    }
}
