/*
 * Copyright (c) 2006-2013 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.Invite;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.InviteListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.ui.StatusBarComponent;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * A status bar component to show invites to the user and enable them to accept
 * or dismiss them.
 */
public class InviteLabel extends StatusbarPopupPanel<JLabel> implements
        StatusBarComponent, InviteListener, ActionListener,
        java.awt.event.ActionListener, SelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Invite popup menu. */
    private final JPopupMenu menu;
    /** Dismiss invites menu item. */
    private final JMenuItem dismiss;
    /** Accept invites menu item. */
    private final JMenuItem accept;
    /** Main frame. */
    private final MainFrame mainFrame;
    /** Swing controller. */
    private final SwingController controller;
    /** Active server. */
    private Server activeServer;

    /**
     * Instantiates a new invite label.
     *
     * @param controller Swing controller
     * @param mainFrame Main frame
     */
    public InviteLabel(final SwingController controller,
            final MainFrame mainFrame) {
        super(new JLabel());

        this.controller = controller;
        this.mainFrame = mainFrame;

        setBorder(BorderFactory.createEtchedBorder());
        label.setIcon(new IconManager(controller.getGlobalConfig())
                .getIcon("invite"));

        menu = new JPopupMenu();
        dismiss = new JMenuItem("Dismiss all invites");
        dismiss.setActionCommand("dismissAll");
        dismiss.addActionListener(this);
        accept = new JMenuItem("Accept all invites");
        accept.setActionCommand("acceptAll");
        accept.addActionListener(this);

        for (final Server server : controller.getServerManager().getServers()) {
            server.addInviteListener(this);
        }

        mainFrame.addSelectionListener(this);
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.SERVER_CONNECTED);
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.SERVER_DISCONNECTED);
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.SERVER_CONNECTERROR);

        update();
    }

    /** {@inheritDoc} */
    @Override
    protected StatusbarPopupWindow getWindow() {
        return new InvitePopup(controller, this, activeServer, mainFrame);
    }

    /**
     * Populates the menu.
     */
    private void popuplateMenu() {
        menu.removeAll();

        final Collection<Invite> invites = activeServer.getInvites();
        for (final Invite invite : invites) {
            menu.add(new JMenuItem(new InviteAction(invite)));
        }
        menu.add(new JSeparator());
        menu.add(accept);
        menu.add(dismiss);
    }

    /**
     * Updates the invite label for the currently active server.
     */
    private void update() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (activeServer == null || activeServer.getInvites().isEmpty()) {
                    setVisible(false);
                    closeDialog();
                } else {
                    refreshDialog();
                    setVisible(true);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void inviteReceived(final Server server, final Invite invite) {
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void inviteExpired(final Server server, final Invite invite) {
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.SERVER_CONNECTED) {
            if (arguments[0] instanceof Server) {
                ((Server) arguments[0]).addInviteListener(this);
            }
        } else {
            if (arguments[0] instanceof Server) {
                ((Server) arguments[0]).removeInviteListener(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        super.mouseClicked(e);
        popuplateMenu();
        if (menu.getComponentCount() > 0) {
            menu.show(this, e.getX(), e.getY());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        switch (e.getActionCommand()) {
            case "acceptAll":
                activeServer.acceptInvites();
                break;
            case "dismissAll":
                activeServer.removeInvites();
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final TextFrame window) {
        activeServer = window == null ? null : window.getContainer().getServer();
        update();
    }
}
