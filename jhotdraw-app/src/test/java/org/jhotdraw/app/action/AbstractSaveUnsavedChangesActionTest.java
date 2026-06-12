/*
 * Unit tests for the PNG-export domain logic of the "Save as PNG on close"
 * feature. Tests target a single code-path through a single pure method
 * (ensurePngExtension) so no Swing, file system, or concrete View is involved.
 */
package org.jhotdraw.app.action;

import java.io.File;
import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link AbstractSaveUnsavedChangesAction#ensurePngExtension(URI)}.
 *
 * <p>This is the most important piece of domain logic introduced by the
 * Save-as-PNG feature: whatever filename the user supplies, the exported file
 * must end with {@code .png}. The cases below cover the best-case scenario and
 * the identified boundary cases.</p>
 */
public class AbstractSaveUnsavedChangesActionTest {

    // ----- Best-case scenario -------------------------------------------------

    /**
     * Best case: the user types a plain name with no extension; the rule must
     * append {@code .png}.
     */
    @Test
    public void appendsPngWhenNoExtension() {
        URI in = new File("/tmp/drawing").toURI();
        URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(in);
        assertTrue("path should end with .png", out.getPath().endsWith("/drawing.png"));
    }

    // ----- Boundary cases -----------------------------------------------------

    /**
     * Boundary: the name already ends with {@code .png}. The URI must be
     * returned unchanged (no double extension, same instance).
     */
    @Test
    public void leavesUnchangedWhenAlreadyPng() {
        URI in = new File("/tmp/drawing.png").toURI();
        URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(in);
        assertSame("already-.png URI should be returned unchanged", in, out);
    }

    /**
     * Boundary: the extension is in a different case ({@code .PNG}). The
     * comparison must be case-insensitive, so no second extension is added.
     */
    @Test
    public void treatsExtensionCaseInsensitively() {
        URI in = new File("/tmp/DRAWING.PNG").toURI();
        URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(in);
        assertSame("uppercase .PNG should count as a png extension", in, out);
    }

    /**
     * Boundary: a different image-ish extension (e.g. {@code .jpg}) must NOT be
     * treated as png; {@code .png} is appended after it.
     */
    @Test
    public void appendsPngAfterDifferentExtension() {
        URI in = new File("/tmp/drawing.jpg").toURI();
        URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(in);
        assertTrue("should become drawing.jpg.png", out.getPath().endsWith("drawing.jpg.png"));
    }

    /**
     * Boundary: a filename that merely contains the substring "png" but does
     * not end with ".png" still needs the extension appended.
     */
    @Test
    public void appendsPngWhenNameContainsButDoesNotEndWithPng() {
        URI in = new File("/tmp/pngdrawing").toURI();
        URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(in);
        assertTrue(out.getPath().endsWith("pngdrawing.png"));
    }

    /**
     * Boundary / invariant: the result is never null and always carries the png
     * extension for a normal file URI.
     */
    @Test
    public void resultIsNeverNullAndAlwaysPng() {
        URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(
                new File("/tmp/x").toURI());
        assertNotNull(out);
        assertTrue(out.getPath().toLowerCase().endsWith(".png"));
    }

    /**
     * Boundary: an opaque URI with no path component (getPath() == null) must
     * be returned unchanged rather than causing a NullPointerException.
     */
    @Test
    public void handlesUriWithNullPath() {
        URI opaque = URI.create("mailto:nobody@example.com"); // opaque: getPath() == null
        URI out = AbstractSaveUnsavedChangesAction.ensurePngExtension(opaque);
        assertSame(opaque, out);
    }
}
