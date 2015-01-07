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

package com.dmdirc.addons.ui_swing.dialogs.sslcertificate;

import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.events.ServerCertificateProblemEncounteredEvent;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;

import java.awt.Window;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Factory for {@link SSLCertificateDialog}s.
 */
@Singleton
public class SSLCertificateDialogFactory {

    private final IconManager iconManager;
    private final Provider<Window> mainWindowProvider;

    @Inject
    public SSLCertificateDialogFactory(
            final IconManager iconManager,
            @MainWindow final Provider<Window> mainWindowProvider) {
        this.iconManager = iconManager;
        this.mainWindowProvider = mainWindowProvider;
    }

    public SSLCertificateDialog create(final ServerCertificateProblemEncounteredEvent event) {
        return new SSLCertificateDialog(iconManager, mainWindowProvider.get(),
                new SSLCertificateDialogModel(event.getCertificateChain(), event.getProblems(),
                        event.getCertificateManager()));
    }

}