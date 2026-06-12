/*
 * @(#)BringForwardAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.*;
import javax.swing.undo.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * ToFrontAction.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class BringForwardAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.bringForward";

    /**
     * Creates a new instance.
     */
    public BringForwardAction(DrawingEditor editor) {
        super(editor);
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        final DrawingView view = getView();
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
        bringForward(view, figures);
        fireUndoableEditHappened(new AbstractUndoableEdit() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getPresentationName() {
                ResourceBundleUtil labels
                        = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                return labels.getTextProperty(ID);
            }

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
                BringForwardAction.bringForward(view, figures);
            }

            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                SendBackwardAction.sendBackward(view, figures);
            }
        });
    }

    public static void bringForward(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        LinkedList<Figure> sorted = new LinkedList<>(drawing.sort(figures));
        for (java.util.Iterator<Figure> i = sorted.descendingIterator(); i.hasNext();) {
            drawing.bringForward(i.next());
        }
    }
}
