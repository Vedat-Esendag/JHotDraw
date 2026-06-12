/*
 * Unit tests for the PNG-export extension point added to the View interface.
 *
 * The feature added two default methods to View:
 *   - canExportToPNG()  -> false by default
 *   - exportToPNG(uri)  -> throws UnsupportedOperationException by default
 *
 * To test these default methods in isolation we use a hand-written STUB
 * (StubView) that implements the View interface with do-nothing bodies and
 * deliberately does NOT override the two PNG methods. This avoids any
 * dependency on Swing, a concrete view, or the application (the lab's
 * "apply stubs to avoid the dependency" guidance). A second stub
 * (PngCapableStubView) overrides them to model a PNG-capable view.
 */
package org.jhotdraw.app.action;

import java.io.IOException;
import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.jhotdraw.api.app.View;

public class ViewPngExportDefaultsTest {

    // ----- Best-case: a view that opted IN to PNG export ----------------------

    @Test
    public void capableViewReportsTrueAndExports() throws IOException {
        PngCapableStubView v = new PngCapableStubView();
        assertTrue("capable view should report it can export PNG", v.canExportToPNG());
        v.exportToPNG(URI.create("file:/tmp/out.png"));
        assertEquals("exportToPNG should have been invoked once", 1, v.exportCalls);
    }

    // ----- Boundary: a view that did NOT opt in (inherits the defaults) -------

    @Test
    public void defaultCanExportToPngIsFalse() {
        View v = new StubView();
        assertFalse("default capability must be false for backward compatibility",
                v.canExportToPNG());
    }

    @Test
    public void defaultExportToPngThrowsUnsupported() {
        View v = new StubView();
        try {
            v.exportToPNG(URI.create("file:/tmp/out.png"));
            fail("default exportToPNG must throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // correct behaviour
        } catch (IOException e) {
            fail("expected UnsupportedOperationException, got IOException: " + e);
        }
    }

    // ========================================================================
    //  Stubs
    // ========================================================================

    /** A View that opts in to PNG export and records that it was called. */
    private static final class PngCapableStubView extends StubView {
        int exportCalls = 0;
        @Override public boolean canExportToPNG() { return true; }
        @Override public void exportToPNG(URI uri) throws IOException {
            assert uri != null : "uri must not be null"; // invariant
            exportCalls++;
        }
    }

    /**
     * Minimal stub implementing the View interface. Every method is a no-op or
     * returns a neutral value. It intentionally leaves canExportToPNG() and
     * exportToPNG() to the interface defaults so those can be tested.
     */
    private static class StubView implements View {
        @Override public org.jhotdraw.api.app.Application getApplication() { return null; }
        @Override public void setApplication(org.jhotdraw.api.app.Application newValue) { }
        @Override public javax.swing.JComponent getComponent() { return null; }
        @Override public boolean isEnabled() { return true; }
        @Override public void setEnabled(boolean newValue) { }
        @Override public void clear() { }
        @Override public boolean isEmpty() { return true; }
        @Override public boolean hasUnsavedChanges() { return false; }
        @Override public void markChangesAsSaved() { }
        @Override public void execute(Runnable worker) { }
        @Override public void init() { }
        @Override public void start() { }
        @Override public void activate() { }
        @Override public void deactivate() { }
        @Override public void stop() { }
        @Override public void dispose() { }
        @Override public javax.swing.ActionMap getActionMap() { return null; }
        @Override public void setActionMap(javax.swing.ActionMap m) { }
        @Override public void addPropertyChangeListener(java.beans.PropertyChangeListener l) { }
        @Override public void removePropertyChangeListener(java.beans.PropertyChangeListener l) { }
        @Override public void setMultipleOpenId(int newValue) { }
        @Override public int getMultipleOpenId() { return 0; }
        @Override public boolean isShowing() { return false; }
        @Override public void setShowing(boolean newValue) { }
        @Override public void setTitle(String newValue) { }
        @Override public String getTitle() { return ""; }
        @Override public void addDisposable(org.jhotdraw.api.app.Disposable disposable) { }
        @Override public void removeDisposable(org.jhotdraw.api.app.Disposable disposable) { }
        @Override public java.net.URI getURI() { return null; }
        @Override public void setURI(java.net.URI newValue) { }
        @Override public boolean canSaveTo(java.net.URI uri) { return false; }
        @Override public void write(java.net.URI uri, org.jhotdraw.api.gui.URIChooser chooser) throws IOException { }
        @Override public void read(java.net.URI uri, org.jhotdraw.api.gui.URIChooser chooser) throws IOException { }
    }
}
