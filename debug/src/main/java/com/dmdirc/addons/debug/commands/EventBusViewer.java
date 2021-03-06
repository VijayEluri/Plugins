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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.CustomWindow;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.events.ClientLineAddedEvent;
import com.dmdirc.events.CommandOutputEvent;
import com.dmdirc.events.eventbus.BaseEvent;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.IRCControlCodes;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

/**
 * Displays events passed on an event bus.
 */
public class EventBusViewer extends DebugCommand {

    private final AggregateConfigProvider globalConfig;
    private final WindowManager windowManager;
    private final EventBus globalEventBus;
    private final BackBufferFactory backBufferFactory;

    /**
     * Creates a new instance of the command.
     */
    @Inject
    public EventBusViewer(
            final Provider<Debug> commandProvider,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final WindowManager windowManager,
            final EventBus globalEventBus,
            final BackBufferFactory backBufferFactory) {
        super(commandProvider);
        this.globalConfig = globalConfig;
        this.windowManager = windowManager;
        this.globalEventBus = globalEventBus;
        this.backBufferFactory = backBufferFactory;
    }

    @Override
    public String getName() {
        return "eventbus";
    }

    @Override
    public String getUsage() {
        return "[--global] - Shows events being sent on an event bus";
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final boolean isGlobal = args.getArguments().length > 0
                && "--global".equals(args.getArguments()[0]);

        final CustomWindow window;
        if (isGlobal) {
            window = new CustomWindow("Event bus", "Event bus", globalConfig,
                    globalEventBus, backBufferFactory);
            windowManager.addWindow(window);
        } else {
            window = new CustomWindow("Event bus", "Event bus", origin, backBufferFactory);
            windowManager.addWindow(origin, window);
        }

        final EventBus eventBus = isGlobal ? globalEventBus : origin.getEventBus();
        final WindowUpdater updater = new WindowUpdater(eventBus, window);
        eventBus.subscribe(updater);
    }

    /**
     * Updates a custom window with details of each event received on an event bus.
     */
    @Listener(references = References.Strong)
    private static class WindowUpdater {

        private final EventBus eventBus;
        private final WindowModel target;

        WindowUpdater(final EventBus eventBus, final WindowModel target) {
            this.eventBus = eventBus;
            this.target = target;
        }

        @Handler
        public void handleFrameClosing(final FrameClosingEvent event) {
            if (event.getSource().equals(target)) {
                eventBus.unsubscribe(this);
            }
        }

        @Handler
        public void handleEvent(final BaseEvent event) {
            if (event instanceof ClientLineAddedEvent
                    && ((ClientLineAddedEvent) event).getFrameContainer() == target
                    || event instanceof CommandOutputEvent
                    && ((DisplayableEvent) event).getSource() == target) {
                // Don't add a line every time we add a line to our output window.
                // Things will explode otherwise.
                return;
            }

            final StringBuilder output = new StringBuilder();
            output.append(IRCControlCodes.BOLD)
                    .append(event.getClass().getSimpleName())
                    .append(IRCControlCodes.BOLD);

            for (Method method : event.getClass().getMethods()) {
                if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                    try {
                        output.append(' ')
                                .append(IRCControlCodes.UNDERLINE)
                                .append(method.getName().substring(3))
                                .append(IRCControlCodes.UNDERLINE)
                                .append('=')
                                .append(method.invoke(event));
                    } catch (ReflectiveOperationException ex) {
                        // Ignore.
                    }
                }
            }

            target.getEventBus().publishAsync(new CommandOutputEvent(target, output.toString()));
        }

    }
}
