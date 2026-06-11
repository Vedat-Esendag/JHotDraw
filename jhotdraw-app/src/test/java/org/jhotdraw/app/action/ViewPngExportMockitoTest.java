/*
 * Mockito-based unit tests for the "Save as PNG on close" feature.
 *
 * Where ViewPngExportDefaultsTest uses a hand-written stub, this class uses
 * Mockito to MOCK the View dependency. Mockito lets us:
 *   - stub return values        with when(...).thenReturn(...)
 *   - verify interactions        with verify(...)
 *   - capture method arguments   with ArgumentCaptor
 * so we can test the feature's behaviour without depending on a real View,
 * Swing, or the file system (the lab's "apply mocks to avoid the dependency").
 */
package org.jhotdraw.app.action;

import java.io.IOException;
import java.net.URI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.jhotdraw.api.app.View;

@RunWith(MockitoJUnitRunner.class)
public class ViewPngExportMockitoTest {

    /**
     * Stubbing return values: a mocked View can be told to advertise PNG
     * capability, and we can verify that the export method was invoked.
     * (A Mockito mock's void methods do nothing by default, so exportToPNG
     * needs no explicit stub.)
     */
    @Test
    public void mockedViewExportsWhenCapable() throws IOException {
        View view = mock(View.class);
        when(view.canExportToPNG()).thenReturn(true);

        // Simulate the controller's behaviour: only export if the view says it can.
        if (view.canExportToPNG()) {
            view.exportToPNG(URI.create("file:/tmp/out.png"));
        }

        assertTrue(view.canExportToPNG());
        verify(view, times(1)).exportToPNG(URI.create("file:/tmp/out.png"));
    }

    /**
     * Interaction test: a view that reports it CANNOT export must never have
     * exportToPNG() called on it. verify(..., never()) proves the guard works.
     */
    @Test
    public void incapableViewIsNeverAskedToExport() throws IOException {
        View view = mock(View.class);
        when(view.canExportToPNG()).thenReturn(false);

        if (view.canExportToPNG()) {
            view.exportToPNG(URI.create("file:/tmp/out.png"));
        }

        assertFalse(view.canExportToPNG());
        verify(view, never()).exportToPNG(any(URI.class));
    }

    /**
     * ArgumentCaptor: prove the URI handed to the view is the one produced by
     * ensurePngExtension — i.e. the extension was corrected before export.
     */
    @Test
    public void exportReceivesNormalisedPngUri() throws IOException {
        View view = mock(View.class);

        // The controller normalises the URI, then exports it.
        URI corrected = AbstractSaveUnsavedChangesAction.ensurePngExtension(
                new java.io.File("/tmp/drawing").toURI());   // no extension -> +.png
        view.exportToPNG(corrected);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        verify(view).exportToPNG(captor.capture());
        assertTrue("view must be asked to write a .png file",
                captor.getValue().getPath().endsWith(".png"));
    }

    /**
     * Spy on the real default methods: a partial mock with CALLS_REAL_METHODS
     * runs the actual interface defaults, confirming canExportToPNG() defaults
     * to false on an otherwise-mocked View.
     */
    @Test
    public void realDefaultCapabilityIsFalse() {
        View view = mock(View.class, CALLS_REAL_METHODS);
        assertFalse("real default canExportToPNG() must be false", view.canExportToPNG());
    }
}
