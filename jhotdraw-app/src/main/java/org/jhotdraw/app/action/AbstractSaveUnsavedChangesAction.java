/*
 * @(#)AbstractSaveUnsavedChangesAction.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.app.action;

import java.awt.Component;
//import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
//import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import org.jhotdraw.action.AbstractViewAction;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.api.app.View;
import org.jhotdraw.api.gui.URIChooser;
import org.jhotdraw.gui.JSheet;
import org.jhotdraw.gui.event.SheetEvent;
//import org.jhotdraw.gui.event.SheetListener;
import org.jhotdraw.net.URIUtil;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * This abstract class can be extended to implement an {@code Action} that asks
 * to save unsaved changes of a {@link org.jhotdraw.api.app.View} before a destructive
 * action is performed.
 * <p>
 * If the view has no unsaved changes, method {@code doIt} is invoked immediately.
 * If unsaved changes are present, a dialog is shown asking whether the user
 * wants to discard the changes, cancel or save the changes before doing it.
 * If the user chooses to discard the changes, {@code doIt} is invoked immediately.
 * If the user chooses to cancel, the action is aborted.
 * If the user chooses to save the changes, the view is saved, and {@code doIt}
 * is only invoked after the view was successfully saved.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AbstractSaveUnsavedChangesAction extends AbstractViewAction {

    private static final long serialVersionUID = 1L;
    private Component oldFocusOwner;

    /**
     * Creates a new instance.
     */
    protected AbstractSaveUnsavedChangesAction(Application app, View view) {
        super(app, view);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        final View view = getActiveView();

        // 1. Guard clause to exit early
        if (view == null) {
            return;
        }

        // 2. High-level execution flow
        if (view.hasUnsavedChanges()) {
            showUnsavedChangesDialog(view);
        } else {
            doIt(view);
        }
    }

    /**
     * EXTRACTED METHOD: Handles the initialization of the dialog sheet.
     */
    private void showUnsavedChangesDialog(final View view) {
        final ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
        JOptionPane pane = createSaveOptionPane(view, labels);

        JSheet.showSheet(pane, view.getComponent(), evt -> handleSaveOptionSelected(evt, view, labels));
    }

    /**
     * EXTRACTED METHOD: Handles the UI construction of the JOptionPane.
     */
    private JOptionPane createSaveOptionPane(View view, ResourceBundleUtil labels) {
        URI unsavedURI = view.getURI();
        String title = (unsavedURI == null) ? labels.getString("unnamedFile") : URIUtil.getName(unsavedURI);

        JOptionPane pane = new JOptionPane(
                "<html>" + UIManager.getString("OptionPane.css") +
                        "<b>" + labels.getFormatted("file.saveBefore.doYouWantToSave.message", title) + "</b><p>" +
                        labels.getString("file.saveBefore.doYouWantToSave.details"),
                JOptionPane.WARNING_MESSAGE);

        java.util.List<Object> optionList = new java.util.ArrayList<>();
        optionList.add(labels.getString("file.saveBefore.saveOption.text"));
        // Only offer "Save as PNG" if the view is able to export a PNG image.
        if (view.canExportToPNG()) {
            optionList.add(labels.getString("file.saveBefore.savePngOption.text"));
        }
        optionList.add(labels.getString("file.saveBefore.cancelOption.text"));
        optionList.add(labels.getString("file.saveBefore.dontSaveOption.text"));
        Object[] options = optionList.toArray();

        pane.setOptions(options);
        pane.setInitialValue(options[0]);
        // The destructive ("Don't Save") option is always the last one.
        pane.putClientProperty("Quaqua.OptionPane.destructiveOption", options.length - 1);

        return pane;
    }

    /**
     * EXTRACTED METHOD: Flattens the nested conditional logic for user selection.
     */
    private void handleSaveOptionSelected(SheetEvent evt, View view, ResourceBundleUtil labels) {
        Object value = evt.getValue();

        if (value == null || value.equals(labels.getString("file.saveBefore.cancelOption.text"))) {
            view.setEnabled(true);
        } else if (value.equals(labels.getString("file.saveBefore.dontSaveOption.text"))) {
            doIt(view);
            view.setEnabled(true);
        } else if (value.equals(labels.getString("file.saveBefore.savePngOption.text"))) {
            savePNGView(view);
        } else if (value.equals(labels.getString("file.saveBefore.saveOption.text"))) {
            saveView(view);
        }
    }

    protected URIChooser getChooser(View view) {
        URIChooser chsr = (URIChooser) (view.getComponent()).getClientProperty("saveChooser");
        if (chsr == null) {
            chsr = getApplication().getModel().createSaveChooser(getApplication(), view);
            view.getComponent().putClientProperty("saveChooser", chsr);
        }
        return chsr;
    }

    protected void saveView(final View v) {
        if (v.getURI() == null) {
            URIChooser chooser = getChooser(v);
            //int option = fileChooser.showSaveDialog(this);
            JSheet.showSaveSheet(chooser, v.getComponent(), evt -> {
                if (evt.getOption() == JFileChooser.APPROVE_OPTION) {
                    saveViewToURI(v, evt.getChooser().getSelectedURI(), evt.getChooser());
                } else {
                    v.setEnabled(true);
                    if (oldFocusOwner != null) {
                        oldFocusOwner.requestFocus();
                    }
                }
            });
        } else {
            saveViewToURI(v, v.getURI(), null);
        }
    }

    protected void saveViewToURI(final View v, final URI uri, final URIChooser chooser) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                v.write(uri, chooser);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    v.setURI(uri);
                    v.markChangesAsSaved();
                    doIt(v);
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(AbstractSaveUnsavedChangesAction.class.getName()).log(Level.SEVERE, null, ex);
                    String message = (ex.getMessage() != null) ? ex.getMessage() : ex.toString();
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
                    JSheet.showMessageSheet(getActiveView().getComponent(),
                                            "<html>" + UIManager.getString("OptionPane.css")
                                            + "<b>" + labels.getFormatted("file.save.couldntSave.message", URIUtil.
                                                                          getName(uri)) + "</b><p>"
                                            + ((message == null) ? "" : message),
                                            JOptionPane.ERROR_MESSAGE);
                    Thread.currentThread().interrupt();
                }
                v.setEnabled(true);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
        }.execute();
    }

    /**
     * Asks the user for a location and exports the view as a PNG image, then
     * proceeds with the destructive action (e.g. closing the view).
     * <p>
     * Because a PNG is a raster export and not the view's native document
     * format, this does not mark the document's changes as saved or change the
     * view's URI; it simply writes the image and continues.
     */
    protected void savePNGView(final View v) {
        URIChooser chooser = getPNGChooser(v);
        JSheet.showSaveSheet(chooser, v.getComponent(), evt -> {
            if (evt.getOption() == JFileChooser.APPROVE_OPTION) {
                exportViewToPNG(v, evt.getChooser().getSelectedURI());
            } else {
                v.setEnabled(true);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
        });
    }

    /**
     * Returns a save chooser configured for PNG output. A fresh chooser is used
     * (and cached separately from the regular save chooser) so the PNG file
     * filter does not interfere with the view's native save chooser.
     */
    protected URIChooser getPNGChooser(View view) {
        URIChooser chsr = (URIChooser) (view.getComponent()).getClientProperty("savePNGChooser");
        if (chsr == null) {
            org.jhotdraw.gui.JFileURIChooser fc = new org.jhotdraw.gui.JFileURIChooser();
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Portable Network Graphics (PNG)", "png"));
            chsr = fc;
            view.getComponent().putClientProperty("savePNGChooser", chsr);
        }
        return chsr;
    }

    /**
     * Ensures that the given URI's path ends with the {@code .png} extension.
     * <p>
     * This is the core domain rule for PNG export: a user may type a filename
     * without an extension, or with a different-case extension; the export must
     * always produce a file whose name ends in {@code .png}. The method is pure
     * (no I/O, no Swing) and package-visible so it can be unit tested in
     * isolation.
     *
     * @param rawUri the URI selected by the user; must not be null
     * @return a URI whose path ends with {@code .png}; the same URI is returned
     * unchanged if it already ends with {@code .png} (case-insensitive)
     * @throws NullPointerException if {@code rawUri} is null
     */
    static URI ensurePngExtension(final URI rawUri) {
        // Invariant: this method must never be handed a null URI.
        assert rawUri != null : "rawUri must not be null";
        if (rawUri.getPath() == null) {
            return rawUri;
        }
        if (rawUri.getPath().toLowerCase().endsWith(".png")) {
            return rawUri;
        }
        return new java.io.File(rawUri.getPath() + ".png").toURI();
    }

    protected void exportViewToPNG(final View v, final URI rawUri) {
        // Ensure the target path ends with .png.
        final URI uri = ensurePngExtension(rawUri);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                v.exportToPNG(uri);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    doIt(v);
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(AbstractSaveUnsavedChangesAction.class.getName()).log(Level.SEVERE, null, ex);
                    String message = (ex.getMessage() != null) ? ex.getMessage() : ex.toString();
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
                    JSheet.showMessageSheet(getActiveView().getComponent(),
                                            "<html>" + UIManager.getString("OptionPane.css")
                                            + "<b>" + labels.getFormatted("file.save.couldntSave.message", URIUtil.
                                                                          getName(uri)) + "</b><p>"
                                            + ((message == null) ? "" : message),
                                            JOptionPane.ERROR_MESSAGE);
                    Thread.currentThread().interrupt();
                }
                v.setEnabled(true);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
        }.execute();
    }

    protected abstract void doIt(View p);
}
