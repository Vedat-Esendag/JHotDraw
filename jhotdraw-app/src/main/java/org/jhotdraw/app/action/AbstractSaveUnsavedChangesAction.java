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

        Object[] options = {
                labels.getString("file.saveBefore.saveOption.text"),
                labels.getString("file.saveBefore.cancelOption.text"),
                labels.getString("file.saveBefore.dontSaveOption.text")
        };

        pane.setOptions(options);
        pane.setInitialValue(options[0]);
        pane.putClientProperty("Quaqua.OptionPane.destructiveOption", 2);

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

    protected abstract void doIt(View p);
}
