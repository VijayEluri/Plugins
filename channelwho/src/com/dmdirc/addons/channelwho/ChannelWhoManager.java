/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.channelwho;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ServerConnectingEvent;
import com.dmdirc.events.ServerDisconnectedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.plugins.PluginDomain;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

/**
 * Provides channel who support in DMDirc.
 */
public class ChannelWhoManager {

    private final ConnectionManager connectionManager;
    private final DMDircMBassador eventBus;

    @Inject
    public ChannelWhoManager(
            @PluginDomain(ChannelWhoPlugin.class) final String domain,
            final ConnectionManager connectionManager,
            final DMDircMBassador eventBus) {
        this.connectionManager = connectionManager;
        this.eventBus = eventBus;
    }

    public void load() {
        eventBus.subscribe(this);
        connectionManager.getConnections().forEach(this::addConnectionHandler);
    }

    public void unload() {
        connectionManager.getConnections().forEach(this::removeConnectionHandler);
        eventBus.unsubscribe(this);
    }

    private void addConnectionHandler(final Connection connection) {
        // TODO: Create a handler class which will monitor settings + handle timers.
    }

    private void removeConnectionHandler(final Connection connection) {
        // TODO: Remove handlers
    }

    @Handler
    private void handleServerConnectingEvent(final ServerConnectingEvent event) {
        addConnectionHandler(event.getConnection());
    }

    @Handler
    private void handleServerDisconnectedEvent(final ServerDisconnectedEvent event) {
        removeConnectionHandler(event.getConnection());
    }

}
