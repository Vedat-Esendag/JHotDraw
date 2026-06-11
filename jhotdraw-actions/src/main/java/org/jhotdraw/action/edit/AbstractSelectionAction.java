/*
 * @(#)AbstractSelectionAction.java
 *
 * Copyright (c) 2010 The authors and contributors of JHotDraw.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.action.edit;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.jhotdraw.api.gui.EditableComponent;
import org.jhotdraw.beans.WeakPropertyChangeListener;

/**
 * {@code AbstractSelectionAction} acts on the selection of a target component.
 * <p>
 * By default, the action is disabled when the target component is disabled or has
 * no selection. If the target component is null, updateEnabled does nothing.
 * You can change this behavior by overriding method {@code updateEnabled()}.
 * <p>
 * This action registers a {@link WeakPropertyChangeListener} on the component.
 *
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p>
 * <em>Framework</em><br>
 * The interfaces and classes listed below work together:
 * <br>
 * Contract: {@link org.jhotdraw.gui.EditableComponent}, {@code JTextComponent}.<br>
 * Client: {@link org.jhotdraw.action.edit.AbstractSelectionAction},
 * {@link org.jhotdraw.action.edit.DeleteAction},
 * {@link org.jhotdraw.action.edit.DuplicateAction},
 * {@link org.jhotdraw.action.edit.SelectAllAction},
 * {@link org.jhotdraw.action.edit.ClearSelectionAction}.
 * <hr>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AbstractSelectionAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    /**
     * The target of the action or null if the action acts on the currently
     * focused component.
     */
    protected JComponent target;
    /**
     * This variable keeps a strong reference on the property change listener.
     */
    private PropertyChangeListener propertyHandler;

    /**
     * Creates a new instance which acts on the specified component.
     *
     * @param target The target of the action. Specify null for the currently
     * focused component.
     */
    public AbstractSelectionAction(JComponent target) {
        this.target = target;
        if (target != null) {
            // Register with a weak reference on the JComponent.
            propertyHandler = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String n = evt.getPropertyName();
                    if ("enabled".equals(n)) {
                        updateEnabled();
                    } else if (n.equals(EditableComponent.SELECTION_EMPTY_PROPERTY)) {
                        updateEnabled();
                    }
                }
            };
            target.addPropertyChangeListener(new WeakPropertyChangeListener(propertyHandler));
        }
    }

    /**
     * Performs this action's selection operation on the target component, or
     * on the permanently focused component if no target has been specified.
     * <p>
     * This is a template method: it locates the component to act on, checks
     * that it is enabled, and delegates the actual selection operation to the
     * hooks {@link #performOnEditableComponent} and
     * {@link #performOnTextComponent}. Beeps if the component is of neither
     * type.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        JComponent c = findTargetComponent();
        if (c != null && c.isEnabled()) {
            if (c instanceof EditableComponent) {
                performOnEditableComponent((EditableComponent) c);
            } else if (c instanceof JTextComponent) {
                performOnTextComponent((JTextComponent) c);
            } else {
                c.getToolkit().beep();
            }
        }
    }

    /**
     * Returns the component on which this action operates: the explicit
     * target if one was specified, otherwise the permanently focused
     * component if that is a {@code JComponent}.
     *
     * @return The component to act on, or null if there is none.
     */
    protected JComponent findTargetComponent() {
        if (target != null) {
            return target;
        }
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        return (focusOwner instanceof JComponent) ? (JComponent) focusOwner : null;
    }

    /**
     * Hook method: performs this action's selection operation on an
     * {@link EditableComponent}. The default implementation does nothing.
     */
    protected void performOnEditableComponent(EditableComponent c) {
        // hook - the default implementation does nothing
    }

    /**
     * Hook method: performs this action's selection operation on a
     * {@code JTextComponent}. The default implementation does nothing.
     */
    protected void performOnTextComponent(JTextComponent c) {
        // hook - the default implementation does nothing
    }

    protected void updateEnabled() {
        if (target instanceof EditableComponent && isSelectionRequired()) {
            setEnabled(target.isEnabled() && !((EditableComponent) target).isSelectionEmpty());
        } else if (target != null) {
            setEnabled(target.isEnabled());
        }
    }

    /**
     * Hook method: returns true if this action requires a non-empty selection
     * on an {@link EditableComponent} target in order to be enabled. Actions
     * which operate regardless of the current selection (such as select-all
     * and clear-selection) override this to return false.
     */
    protected boolean isSelectionRequired() {
        return true;
    }
}
