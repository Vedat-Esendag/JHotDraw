/*
 * JGiven THEN stage: assert the outcome using AssertJ (domain-specific, fluent
 * assertions) and Mockito interaction verification.
 */
package org.jhotdraw.app.action.bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import java.io.IOException;
import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ThenSaved extends Stage<ThenSaved> {

    @ProvidedScenarioState
    ScenarioContext context;

    @As("the Save as PNG option is offered")
    public ThenSaved the_save_as_PNG_option_is_offered() {
        assertThat(context.pngOptionOffered).as("PNG option offered").isTrue();
        return this;
    }

    @As("the Save as PNG option is not offered")
    public ThenSaved the_save_as_PNG_option_is_not_offered() {
        assertThat(context.pngOptionOffered).as("PNG option offered").isFalse();
        return this;
    }

    @As("the drawing is exported to a file named $")
    public ThenSaved the_drawing_is_exported_to(@Quoted String expectedName) throws IOException {
        assertThat(context.exportPerformed).as("export performed").isTrue();
        assertThat(context.exportedFile).isNotNull();
        assertThat(context.exportedFile.getPath()).endsWith(expectedName);
        // The view really was asked to write that exact file.
        verify(context.view).exportToPNG(context.exportedFile);
        return this;
    }

    @As("the exported file ends with the .png extension")
    public ThenSaved the_exported_file_is_a_PNG() {
        assertThat(context.exportedFile).isNotNull();
        assertThat(context.exportedFile.getPath().toLowerCase()).endsWith(".png");
        return this;
    }

    @As("no PNG export happens")
    public ThenSaved no_PNG_export_happens() throws IOException {
        assertThat(context.exportPerformed).as("export performed").isFalse();
        verify(context.view, never()).exportToPNG(any(URI.class));
        return this;
    }
}
