/*
 * Mutable holder shared between the Given/When/Then JGiven stages.
 *
 * Using a single, uniquely-typed object as scenario state avoids any ambiguity
 * when several primitive/URI values need to be shared between stages.
 */
package org.jhotdraw.app.action.bdd;

import java.net.URI;
import org.jhotdraw.api.app.View;

class ScenarioContext {
    /** The (mocked) view being closed. */
    View view;
    /** The file name the user chose in the save dialog. */
    URI chosenFile;

    /** Whether the "Save as PNG" option was offered in the dialog. */
    boolean pngOptionOffered;
    /** The file the drawing was actually exported to (after .png normalisation). */
    URI exportedFile;
    /** Whether a PNG export was performed. */
    boolean exportPerformed;
}
