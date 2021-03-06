/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.components.inputfields;

import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.ui.InputField;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.input.TabCompleterUtils;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
 * Swing input handler.
 */
public class SwingInputHandler extends InputHandler implements KeyListener {

    /**
     * Creates a new instance of InputHandler. Adds listeners to the target that we need to operate.
     *
     * @param serviceManager    Manager to use to look up tab completion services.
     * @param target            The text field this input handler is dealing with.
     * @param commandController The controller to use to retrieve command information.
     * @param commandParser     The command parser to use for this text field.
     * @param parentWindow      The window that owns this input handler
     * @param eventBus          The event bus to use to dispatch input events.
     */
    public SwingInputHandler(
            final ServiceManager serviceManager,
            final InputField target,
            final CommandController commandController,
            final CommandParser commandParser,
            final WindowModel parentWindow,
            final TabCompleterUtils tabCompleterUtils,
            final EventBus eventBus) {
        super(serviceManager, target, commandController, commandParser, parentWindow,
                tabCompleterUtils, eventBus);
    }

    @Override
    protected void addUpHandler() {
        final JTextComponent localTarget;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        } else {
            throw new IllegalArgumentException("Unknown target");
        }

        localTarget.getActionMap().put("upArrow", new AbstractAction() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1;

            @Override
            public void actionPerformed(final ActionEvent e) {
                doBufferUp();
            }
        });
        if (Apple.isAppleUI()) {
            localTarget.getInputMap(JComponent.WHEN_FOCUSED).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upArrow");
        } else {
            localTarget.getInputMap(
                    JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upArrow");
        }
    }

    @Override
    protected void addDownHandler() {
        final JTextComponent localTarget;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        } else {
            throw new IllegalArgumentException("Unknown target");
        }

        localTarget.getActionMap().put("downArrow", new AbstractAction() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1;

            @Override
            public void actionPerformed(final ActionEvent e) {
                doBufferDown();
            }
        });
        if (Apple.isAppleUI()) {
            localTarget.getInputMap(JComponent.WHEN_FOCUSED).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                            "downArrow");
        } else {
            localTarget.getInputMap(
                    JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                    put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                            "downArrow");
        }
    }

    @Override
    protected void addTabHandler() {
        final JTextComponent localTarget;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        } else {
            throw new IllegalArgumentException("Unknown target");
        }

        localTarget.getActionMap().put("insert-tab", new AbstractAction() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1;

            @Override
            public void actionPerformed(final ActionEvent e) {
                localTarget.setEditable(false);
                UIUtilities.invokeOffEDT(() -> doTabCompletion(false),
                        value -> localTarget.setEditable(true));
            }
        });
        localTarget.getActionMap().put("insert-shift-tab",
                new AbstractAction() {
                    /** Serial version UID. */
                    private static final long serialVersionUID = 1;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        localTarget.setEditable(false);
                        UIUtilities.invokeOffEDT(() -> doTabCompletion(true),
                                value -> localTarget.setEditable(true));
                    }
                });
        localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "insert-tab");
        localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK),
                        "insert-shift-tab");
    }

    @Override
    protected void addEnterHandler() {
        final JTextComponent localTarget;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        } else {
            throw new IllegalArgumentException("Unknown target");
        }

        localTarget.getActionMap().put("enterButton", new AbstractAction() {
            /** Serial version UID. */
            private static final long serialVersionUID = 1;

            @Override
            public void actionPerformed(final ActionEvent e) {
                final String line = target.getText();
                target.setText("");
                UIUtilities.invokeLater(() -> {
                    final JTextField source;
                    if (e.getSource() instanceof SwingInputField) {
                        source = ((SwingInputField) e.getSource())
                                .getTextField();
                    } else if (e.getSource() instanceof JTextField) {
                        source = (JTextField) e.getSource();
                    } else {
                        throw new IllegalArgumentException(
                                        "Event is not from known source.");
                    }
                    if (source.isEditable()) {
                        UIUtilities.invokeOffEDT(() -> enterPressed(line));
                    }
                });
            }
        });
        localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                        "enterButton");
    }

    @Override
    protected void addKeyHandler() {
        target.addKeyListener(this);
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        //Ignore
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_TAB && e.getKeyCode()
                != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
            final String line = target.getText();
            if (UIUtilities.isCtrlDown(e) && e.getKeyCode() == KeyEvent.VK_ENTER
                    && (flags & HANDLE_RETURN) == HANDLE_RETURN) {
                target.setText("");
            }
            handleKeyPressed(line, target.getCaretPosition(), e.getKeyCode(),
                    e.isShiftDown(), UIUtilities.isCtrlDown(e));
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        validateText();
    }

}
