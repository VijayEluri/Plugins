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

package com.dmdirc.addons.parser_xmpp;

import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.ProtocolDescription;
import com.dmdirc.parser.xmpp.XmppParser;
import com.dmdirc.parser.xmpp.XmppProtocolDescription;
import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.implementations.BasePlugin;

import java.net.URI;

/**
 * Plugin that provides a parser to connect to XMPP services.
 */
public class XmppPlugin extends BasePlugin {

    /**
     * Creates a new XMPP parser for the specified client info and address.
     *
     * @param myInfo  The settings for the local client
     * @param address The address to connect to
     *
     * @return An appropriately configured parser
     */
    @Exported
    public Parser getParser(final MyInfo myInfo, final URI address) {
        return new XmppParser(address);
    }

    /**
     * Retrieves an object describing the properties of the XMPP protocol.
     *
     * @return A relevant protocol description object
     */
    @Exported
    public ProtocolDescription getDescription() {
        return new XmppProtocolDescription();
    }

}
