/*
 * @(#)InvertSelectionAction.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import java.util.*;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * InvertSelectionAction.
 * <p>
 * Selects every selectable figure that is currently <em>not</em> selected and
 * deselects the figures that are currently selected. Inverting an empty
 * selection therefore behaves like "Select All".
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class InvertSelectionAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.invertSelection";

    /**
     * Creates a new instance.
     */
    public InvertSelectionAction(DrawingEditor editor) {
        super(editor);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, ID);
        updateEnabledState();
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        invertSelection();
    }

    public void invertSelection() {
        DrawingView view = getView();
        if (view == null) {
            return;
        }
        Set<Figure> selected = new HashSet<>(view.getSelectedFigures());
        List<Figure> inverted = new ArrayList<>();
        for (Figure f : view.getDrawing().getChildren()) {
            if (f.isSelectable() && !selected.contains(f)) {
                inverted.add(f);
            }
        }
        view.clearSelection();
        view.addToSelection(inverted);
    }

    /**
     * Inverting the selection is meaningful even when nothing is selected (it
     * then behaves like "Select All"), so this action stays enabled whenever an
     * enabled view is present, regardless of the current selection count.
     */
    @Override
    protected void updateEnabledState() {
        setEnabled(getView() != null && getView().isEnabled());
    }
}
