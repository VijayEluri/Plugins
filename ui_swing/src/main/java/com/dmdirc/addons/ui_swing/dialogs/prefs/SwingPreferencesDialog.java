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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.ListScroller;
import com.dmdirc.addons.ui_swing.components.SupplierLoggingSwingWorker;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.addons.ui_swing.injection.DialogModule.ForSettings;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Allows the user to modify global client preferences.
 */
public final class SwingPreferencesDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {

    private static final Logger LOG = LoggerFactory.getLogger(SwingPreferencesDialog.class);
    /** Serial version UID. */
    private static final long serialVersionUID = 9;
    /** Preferences tab list, used to switch option types. */
    private JList<PreferencesCategory> tabList;
    /** Main panel. */
    private CategoryPanel mainPanel;
    /** Previously selected category. */
    private PreferencesCategory selected;
    /** Preferences Manager. */
    private PreferencesDialogModel manager;
    /** Manager loading swing worker. */
    private final SwingWorker<PreferencesDialogModel, Void> worker;
    /** The provider to use for restart dialogs. */
    private final DialogProvider<SwingRestartDialog> restartDialogProvider;
    /** The provider to use to produce a category panel. */
    private final Provider<CategoryPanel> categoryPanelProvider;
    /** Icon manager to retrieve icons from. */
    private final IconManager iconManager;

    /**
     * Creates a new instance of SwingPreferencesDialog.
     *
     * @param parentWindow          Main window to parent dialogs on.
     * @param iconManager           Icon manager used to retrieve images
     * @param restartDialogProvider The provider to use for restart dialogs.
     * @param dialogModelProvider   The provider to use to get a dialog model.
     * @param categoryPanelProvider The provider to use to produce a category panel.
     */
    @Inject
    public SwingPreferencesDialog(
            @MainWindow final Window parentWindow,
            final IconManager iconManager,
            @ForSettings final DialogProvider<SwingRestartDialog> restartDialogProvider,
            final Provider<PreferencesDialogModel> dialogModelProvider,
            final Provider<CategoryPanel> categoryPanelProvider) {
        super(parentWindow, ModalityType.MODELESS);

        this.iconManager = iconManager;
        this.restartDialogProvider = restartDialogProvider;
        this.categoryPanelProvider = categoryPanelProvider;

        initComponents();

        worker = new SupplierLoggingSwingWorker<>(
                () -> getPrefsModel(dialogModelProvider),
                value -> {
                    if (value != null) {
                        setPrefsManager(value);
                    }
                });
        worker.execute();
    }

    private PreferencesDialogModel getPrefsModel(
            final Provider<PreferencesDialogModel> dialogModelProvider) {
        mainPanel.setWaiting(true);
        PreferencesDialogModel prefsManager = null;
        try {
            prefsManager = dialogModelProvider.get();
        } catch (IllegalArgumentException ex) {
            mainPanel.setError(ex.getMessage());
            LOG.error(USER_ERROR, "Unable to load the preferences dialog", ex);
        }
        return prefsManager;
    }

    private void setPrefsManager(final PreferencesDialogModel manager) {
        this.manager = manager;

        ((DefaultListModel<PreferencesCategory>) tabList.getModel()).clear();
        mainPanel.setCategory(null);

        final int count = countCategories(manager.getCategories());
        tabList.setCellRenderer(new PreferencesListCellRenderer(iconManager, count));

        addCategories(manager.getCategories());
    }

    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        mainPanel = categoryPanelProvider.get();

        tabList = new JList<>(new DefaultListModel<>());
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.addListSelectionListener(this);
        ListScroller.register(tabList);
        final JScrollPane tabListScrollPane = new JScrollPane(tabList);
        tabListScrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Preferences");
        setResizable(false);

        tabList.setBorder(BorderFactory.createEtchedBorder());

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        setLayout(new MigLayout("pack, hmin min(80sp, 700), " + "hmax min(700, 80sp)"));
        add(tabListScrollPane, "w 150!, growy, pushy");
        add(mainPanel, "wrap, w 480!, pushy, growy, pushy");
        add(getLeftButton(), "span, split, right");
        add(getRightButton(), "right");
    }

    /**
     * Adds the categories from the preferences manager, clearing existing categories first.
     */
    private void addCategories(final Iterable<PreferencesCategory> categories) {
        UIUtilities.invokeLater(() -> {
            tabList.removeListSelectionListener(this);
            for (PreferencesCategory category : categories) {
                if (!category.isInline()) {
                    ((DefaultListModel<PreferencesCategory>) tabList.getModel()).addElement(
                            category);
                }
                addCategories(category.getSubcats());
            }
            tabList.addListSelectionListener(this);
            tabList.setSelectedIndex(0);
        });
        mainPanel.setWaiting(false);
    }

    /**
     * Counts the number of categories that will be displayed in the list panel.
     *
     * @param categories The collection of categories to inspect
     *
     * @return The number of those categories (including children) that will be displayed
     *
     * @since 0.6.3m1rc3
     */
    private int countCategories(final Iterable<PreferencesCategory> categories) {
        int count = 0;

        for (PreferencesCategory cat : categories) {
            if (!cat.isInline()) {
                count += 1 + countCategories(cat.getSubcats());
            }
        }

        return count;
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (selected != null) {
            selected.fireCategoryDeselected();
            selected = null;
        }
        mainPanel.setCategory(null);

        if (actionEvent != null && getOkButton().equals(actionEvent.getSource())) {
            saveOptions();
        }

        UIUtilities.invokeOffEDT(() -> {
            if (manager != null) {
                manager.dismiss();
            }
        });
        dispose();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            PreferencesCategory node = null;
            try {
                node = tabList.getSelectedValue();
            } catch (ArrayIndexOutOfBoundsException ex) {
                //I hate the JVM
            }
            if (node == null) {
                tabList.setSelectedValue(selected, true);
                return;
            }

            if (node == selected) {
                return;
            }

            if (selected != null) {
                selected.fireCategoryDeselected();
            }
            final int index = tabList.getSelectedIndex();
            tabList.scrollRectToVisible(tabList.getCellBounds(index, index));
            selected = node;
            selected.fireCategorySelected();
            mainPanel.setCategory(selected);
        }
    }

    public void saveOptions() {
        if (manager != null && manager.save()) {
            dispose();
            restartDialogProvider.displayOrRequestFocus();
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
            if (manager != null) {
                manager.close();
            }
            super.dispose();
        }
    }

}
