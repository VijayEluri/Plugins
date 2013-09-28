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
package com.dmdirc.addons.notifications;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Notification Manager plugin, aggregates notification sources exposing them
 * via a single command.
 */
public class NotificationsPlugin extends BaseCommandPlugin implements ActionListener {

    /** The notification methods that we know of. */
    private final List<String> methods = new ArrayList<>();
    /** The user's preferred order for method usage. */
    private List<String> order;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** The controller to read and write settings with. */
    private final IdentityController identityController;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     * @param commandController Command controller to register commands
     * @param identityController The controller to read and write settings with.
     */
    public NotificationsPlugin(
            final PluginInfo pluginInfo,
            final CommandController commandController,
            final IdentityController identityController) {
        super(commandController);
        this.pluginInfo = pluginInfo;
        this.identityController = identityController;
        registerCommand(new NotificationCommand(this),
                NotificationCommand.INFO);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        methods.clear();
        loadSettings();
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.PLUGIN_LOADED, CoreActionType.PLUGIN_UNLOADED);
        for (PluginInfo target : pluginInfo.getMetaData().getManager()
                .getPluginInfos()) {
            if (target.isLoaded()) {
                addPlugin(target);
            }
        }
        super.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        methods.clear();
        ActionManager.getActionManager().unregisterListener(this);
        super.onUnload();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final NotificationConfig configPanel = UIUtilities.invokeAndWait(
                new Callable<NotificationConfig>() {

            /** {@inheritDoc} */
            @Override
            public NotificationConfig call() {
                return new NotificationConfig(NotificationsPlugin.this,
                        order);
            }
        });

        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Notifications", "", "category-notifications",
                configPanel);
        manager.getCategory("Plugins").addSubCategory(category);
    }

    /** Loads the plugins settings. */
    private void loadSettings() {
        if (identityController.getGlobalConfiguration().hasOptionString(getDomain(), "methodOrder")) {
            order = identityController.getGlobalConfiguration().getOptionList(getDomain(), "methodOrder");
        } else {
            order = new ArrayList<>();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.PLUGIN_LOADED) {
            addPlugin((PluginInfo) arguments[0]);
        } else if (type == CoreActionType.PLUGIN_UNLOADED) {
            removePlugin((PluginInfo) arguments[0]);
        }
    }

    /**
     * Checks to see if a plugin implements the notification method interface
     * and if it does, adds the method to our list.
     *
     * @param target The plugin to be tested
     */
    private void addPlugin(final PluginInfo target) {
        if (target.hasExportedService("showNotification")) {
            methods.add(target.getMetaData().getName());
            addMethodToOrder(target);
        }
    }

    /**
     * Checks to see if the specified notification method needs to be added to
     * our order list, and adds it if neccessary.
     *
     * @param source The notification method to be tested
     */
    private void addMethodToOrder(final PluginInfo source) {
        if (!order.contains(source.getMetaData().getName())) {
            order.add(source.getMetaData().getName());
        }
    }

    /**
     * Checks to see if a plugin implements the notification method interface
     * and if it does, removes the method from our list.
     *
     * @param target The plugin to be tested
     */
    private void removePlugin(final PluginInfo target) {
        methods.remove(target.getMetaData().getName());
    }

    /**
     * Retrieves a method based on its name.
     *
     * @param name The name to search for
     * @return The method with the specified name or null if none were found.
     */
    public PluginInfo getMethod(final String name) {
        return pluginInfo.getMetaData().getManager().getPluginInfoByName(name);
    }

    /**
     * Retrieves all the methods registered with this plugin.
     *
     * @return All known notification sources
     */
    public List<PluginInfo> getMethods() {
        final List<PluginInfo> plugins = new ArrayList<>();
        for (String method : methods) {
            plugins.add(pluginInfo.getMetaData().getManager()
                    .getPluginInfoByName(method));
        }
        return plugins;
    }

    /**
     * Does this plugin have any active notification methods?
     *
     * @return true iif active notification methods are registered
     */
    public boolean hasActiveMethod() {
        return !methods.isEmpty();
    }

    /**
     * Returns the user's preferred method if loaded, or null if none loaded.
     *
     * @return Preferred notification method
     */
    public PluginInfo getPreferredMethod() {
        if (methods.isEmpty()) {
            return null;
        }
        for (String method : order) {
            if (methods.contains(method)) {
                return pluginInfo.getMetaData().getManager().getPluginInfoByName(
                    method);
            }
        }
        return null;
    }

    /**
     * Saves the plugins settings.
     *
     * @param newOrder The new order for methods
     */
    protected void saveSettings(final List<String> newOrder) {
        order = newOrder;
        identityController.getGlobalConfigIdentity()
                .setOption(getDomain(), "methodOrder", order);
    }
}
