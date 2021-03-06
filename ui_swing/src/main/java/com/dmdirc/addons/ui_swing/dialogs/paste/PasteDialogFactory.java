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

package com.dmdirc.addons.ui_swing.dialogs.paste;

import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.frames.InputTextFrame;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.input.TabCompleterUtils;
import com.dmdirc.ui.messages.ColourManagerFactory;
import java.awt.Window;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link PasteDialog}s.
 */
@Singleton
public class PasteDialogFactory {

    private final IconManager iconManager;
    private final AggregateConfigProvider config;
    private final ServiceManager serviceManager;
    private final CommandController commandController;
    private final EventBus eventBus;
    private final ColourManagerFactory colourManagerFactory;
    private final TabCompleterUtils tabCompleterUtils;

    @Inject
    public PasteDialogFactory(
            final IconManager iconManager,
            @GlobalConfig final AggregateConfigProvider config,
            final ServiceManager serviceManager,
            final CommandController commandController,
            final EventBus eventBus,
            final ColourManagerFactory colourManagerFactory,
            final TabCompleterUtils tabCompleterUtils) {
        this.iconManager = iconManager;
        this.config = config;
        this.serviceManager = serviceManager;
        this.commandController = commandController;
        this.eventBus = eventBus;
        this.colourManagerFactory = colourManagerFactory;
        this.tabCompleterUtils = tabCompleterUtils;
    }

    public PasteDialog getPasteDialog(final InputTextFrame newParent, final String text,
            final Window parentWindow) {
        return new PasteDialog(iconManager, config, serviceManager, commandController, eventBus,
                newParent, text, parentWindow, colourManagerFactory, tabCompleterUtils);
    }

}
