/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.parser_twitter;

import com.dmdirc.addons.parser_twitter.api.TwitterUser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelNickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelUserModeChangeListener;
import com.dmdirc.plugins.Plugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClientInfo class for the Twitter plugin.
 *
 * @author shane
 */
public class TwitterClientInfo implements LocalClientInfo {
    /** This Clients User */
    private String myUser;

    /** My Parser */
    private Twitter myParser;

    /** Is this a fake client created just for a callback? */
    private boolean isFake = false;

    /** Map of random objects. */
    final Map<Object, Object> myMap = new HashMap<Object, Object>();

    /** List of my channelclients. */
    final List<ChannelClientInfo> channelClients = new ArrayList<ChannelClientInfo>();

    /**
     * Parse an IRC Hostname into its separate parts.
     *
     * @param hostname Hostname to parse
     * @return String array of nick, ident and host.
     */
    static String[] parseHostFull(final String hostname) {
        return parseHostFull(hostname, null, null);
    }

    /**
     * Parse an IRC Hostname into its separate parts.
     *
     * @param hostname Hostname to parse.
     * @param plugin Plugin to use to get domain from.
     * @return String array of nick, ident and host.
     */
    static String[] parseHostFull(String hostname, final Plugin plugin, final Twitter parser) {
        boolean hadAt = false;
        if (plugin != null && parser != null && parser.getConfigManager().getOptionBool(plugin.getDomain(), "autoAt")) {
            if (!hostname.isEmpty() && hostname.charAt(0) == '@') {
                hostname = hostname.substring(1);
                hadAt = true;
            }
        }

        String[] temp = null;
        final String[] result = new String[3];
        if (!hostname.isEmpty() && hostname.charAt(0) == ':') { hostname = hostname.substring(1); }
        temp = hostname.split("@", 2);
        if (temp.length == 1) { result[2] = ""; } else { result[2] = temp[1]; }
        temp = temp[0].split("!", 2);
        if (temp.length == 1) { result[1] = ""; } else { result[1] = temp[1]; }
        result[0] = (hadAt ? "@" : "") + temp[0];

        return result;
    }

    /**
     * Return the nickname from an irc hostname.
     *
     * @param hostname host to parse
     * @return nickname
     */
    static String parseHost(final String hostname) {
        return parseHostFull(hostname)[0];
    }

    /**
     * Create a new TwitterClientInfo
     *
     * @param user User object for this client.
     * @param parser Parser that owns this client.
     */
    public TwitterClientInfo(final String user, final Twitter parser) {
        this.myUser = user;
        this.myParser = parser;
    }

    /** {@inheritDoc} */
    @Override
    public void setNickname(final String name) {
        if (this == myParser.getLocalClient()) {
            // TODO: throw new UnsupportedOperationException("Not supported yet.");
        } else {
            // TODO: throw new UnsupportedOperationException("Can not set nickname on non-local clients");
        }
    }

    /**
     * Get the user object for this client.
     *
     * @return User object for this client.
     */
    public TwitterUser getUser() {
        return myParser.getApi().getCachedUser(myUser);
    }

    /**
    * Check if this is a fake client.
    *
    * @return True if this is a fake client, else false
    */
    public boolean isFake() { return isFake; }
    
    /**
    * Check if this client is actually a server.
    *
    * @return True if this client is actually a server.
    */
    public boolean isServer() { return !(myUser.indexOf(':') == -1); }
    
    /**
    * Set if this is a fake client.
    * This returns "this" and thus can be used in the construction line.
    *
    * @param newValue new value for isFake - True if this is a fake client, else false
    * @return this Object
    */
    public TwitterClientInfo setFake(final boolean newValue) {
        isFake = newValue;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String getModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void setAway(final String reason) {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void setBack() {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void alterMode(final boolean add, final Character mode) {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void flushModes() {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public String getNickname() {
        return myUser;
    }

    /** {@inheritDoc} */
    @Override
    public String getUsername() {
        return "user";
    }

    /** {@inheritDoc} */
    @Override
    public String getHostname() {
        return "twitter.com";
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getNickname() + "!" + getUsername() + "@" + getHostname();
    }

    /** {@inheritDoc} */
    @Override
    public String getRealname() {
        return String.format("%s - http://%s/%s", getUser().getRealName(), getHostname(), getNickname());
    }

    /** {@inheritDoc} */
    @Override
    public int getChannelCount() {
        synchronized (channelClients) {
            return channelClients.size();
        }
    }

    /**
     * Get a list of all the channel clients associated with this user.
     * 
     * @return Channel Clients for this Client.
     */
    public List<ChannelClientInfo> getChannelClients() {
        synchronized (channelClients) {
            return new ArrayList<ChannelClientInfo>(channelClients);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<Object, Object> getMap() {
        return myMap;
    }

    /** {@inheritDoc} */
    @Override
    public Parser getParser() {
        return myParser;
    }

    /**
     * Add a channelClient to this Client.
     * 
     * @param channelClient channelClient to add as us.
     */
    public void addChannelClient(final TwitterChannelClientInfo channelClient) {
        synchronized (channelClients) {
            if (!channelClients.contains(channelClient)) {
                channelClients.add(channelClient);
            }
        }
    }

    /**
     * Remove a channelclient from this client..
     *
     * @param channelClient channelClient to remove.
     */
    public void delChannelClient(final TwitterChannelClientInfo channelClient) {
        synchronized (channelClients) {
            if (channelClients.contains(channelClient)) {
                channelClients.remove(channelClient);
            }
        }
    }

}