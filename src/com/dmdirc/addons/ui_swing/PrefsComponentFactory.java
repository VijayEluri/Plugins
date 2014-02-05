/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.addons.ui_swing.components.FileBrowser;
import com.dmdirc.addons.ui_swing.components.FontPicker;
import com.dmdirc.addons.ui_swing.components.OptionalJSpinner;
import com.dmdirc.addons.ui_swing.components.colours.OptionalColourChooser;
import com.dmdirc.addons.ui_swing.components.durationeditor.DurationDisplay;
import com.dmdirc.addons.ui_swing.components.durationeditor.DurationListener;
import com.dmdirc.addons.ui_swing.components.renderers.MapEntryRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.OptionalValidator;
import com.dmdirc.util.validators.Validator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.jxlayer.JXLayer;

/**
 * Provides methods for constructing a JComponent from a PreferencesSetting.
 */
@Singleton
public final class PrefsComponentFactory {

    /** Swing Controller. */
    private final SwingController controller;

    /**
     * Creates a new instance of PrefsComponentFactory.
     *
     * @param controller Swing Controller
     */
    @Inject
    public PrefsComponentFactory(final SwingController controller) {
        this.controller = controller;
    }

    /**
     * Retrieves the component for the specified setting. Components are
     * initialised with the current value(s) of the setting, and have listeners
     * added to update the setting whenever the components are changed.
     *
     * @param setting The setting whose component is being requested
     * @return An appropriate JComponent descendant
     */
    public JComponent getComponent(final PreferencesSetting setting) {
        JComponent option;

        switch (setting.getType()) {
            case TEXT:
                option = getTextOption(setting);
                break;
            case BOOLEAN:
                option = getBooleanOption(setting);
                break;
            case MULTICHOICE:
                option = getComboOption(setting);
                break;
            case INTEGER:
                option = getIntegerOption(setting);
                break;
            case OPTIONALINTEGER:
                option = getOptionalIntegerOption(setting);
                break;
            case DURATION:
                option = getDurationOption(setting);
                break;
            case COLOUR:
                option = getColourOption(setting);
                break;
            case OPTIONALCOLOUR:
                option = getOptionalColourOption(setting);
                break;
            case FONT:
                option = getFontOption(setting);
                break;
            case FILE:
                option = getFileBrowseOption(setting, JFileChooser.FILES_ONLY);
                break;
            case DIRECTORY:
                option = getFileBrowseOption(setting,
                        JFileChooser.DIRECTORIES_ONLY);
                break;
            case FILES_AND_DIRECTORIES:
                option = getFileBrowseOption(setting,
                        JFileChooser.FILES_AND_DIRECTORIES);
                break;
            case LABEL:
                option = getLabelOption(setting);
                break;
            default:
                throw new IllegalArgumentException(setting.getType()
                        + " is not a valid option type");
        }

        option.setPreferredSize(new Dimension(Short.MAX_VALUE, option.getFont().
                getSize()));

        return new JXLayer<>(option);
    }

    /**
     * Initialises and returns a ValidatingJTextField for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getTextOption(final PreferencesSetting setting) {
        final ValidatingJTextField option = new ValidatingJTextField(
                controller.getIconManager(), setting.getValidator());
        option.setText(setting.getValue());

        option.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                setting.setValue(((JTextField) e.getSource()).getText());
            }
        });

        return option;
    }

    /**
     * Initialises and returns a JCheckBox for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getBooleanOption(
            final PreferencesSetting setting) {
        final JCheckBox option = new JCheckBox();
        option.setSelected(Boolean.parseBoolean(setting.getValue()));
        option.setOpaque(false);
        option.addChangeListener(new ChangeListener() {

            /** {@inheritDoc} */
            @Override
            public void stateChanged(final ChangeEvent e) {
                setting.setValue(String.valueOf(((JCheckBox) e.getSource())
                        .isSelected()));
            }
        });

        return option;
    }

    /**
     * Initialises and returns a JComboBox for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getComboOption(final PreferencesSetting setting) {
        final JComboBox option = new JComboBox(setting.getComboOptions()
                .entrySet().toArray());
        option.setRenderer(new MapEntryRenderer(option.getRenderer()));
        option.setEditable(false);

        for (final Map.Entry<String, String> entry : setting.getComboOptions()
                .entrySet()) {
            if (entry.getKey().equals(setting.getValue())) {
                option.setSelectedItem(entry);
                break;
            }
        }

        option.addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(final ActionEvent e) {
                final Object selected = ((JComboBox) e.getSource())
                        .getSelectedItem();
                if (selected != null) {
                    setting.setValue(((Map.Entry<String, String>) selected).getKey());
                }
            }
        });

        return option;
    }

    /**
     * Initialises and returns a JSpinner for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getIntegerOption(
            final PreferencesSetting setting) {
        JSpinner option;

        try {
            if (setting.getValidator() instanceof NumericalValidator) {
                final int min = ((NumericalValidator) setting.getValidator())
                        .getMin();
                final int max = ((NumericalValidator) setting.getValidator())
                        .getMax();
                int value = Integer.parseInt(setting.getValue());
                if (value < min) {
                    value = min;
                }
                if (value > max) {
                    value = max;
                }
                option = new JSpinner(new SpinnerNumberModel(value, min, max,
                        1));
            } else {
                option = new JSpinner(new SpinnerNumberModel());
                option.setValue(Integer.parseInt(setting.getValue()));
            }
        } catch (final NumberFormatException ex) {
            option = new JSpinner(new SpinnerNumberModel());
        }

        option.addChangeListener(new ChangeListener() {

            /** {@inheritDoc} */
            @Override
            public void stateChanged(final ChangeEvent e) {
                setting.setValue(((JSpinner) e.getSource()).getValue().
                        toString());
            }
        });

        return option;
    }

    /**
     * Initialises and returns a JSpinner for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getOptionalIntegerOption(
            final PreferencesSetting setting) {
        final boolean state = setting.getValue() != null
                && !setting.getValue().startsWith("false:");
        final String integer = setting.getValue() == null ? "0" : setting
                .getValue().substring(1 + setting.getValue().indexOf(':'));

        OptionalJSpinner option;
        final Validator<?> optionalValidator = setting.getValidator();
        Validator<?> numericalValidator = null;
        if (optionalValidator instanceof OptionalValidator) {
            numericalValidator = ((OptionalValidator) setting.getValidator()).
                    getValidator();
            if (!(numericalValidator instanceof NumericalValidator)) {
                numericalValidator = null;
            }
        }

        try {
            if (numericalValidator == null) {
                option = new OptionalJSpinner(new SpinnerNumberModel());
                option.setValue(Integer.parseInt(integer));
                option.setSelected(state);
            } else {
                option = new OptionalJSpinner(
                        new SpinnerNumberModel(Integer.parseInt(integer),
                        ((NumericalValidator) numericalValidator).getMin(),
                        ((NumericalValidator) numericalValidator).getMax(),
                        1), state);
            }
        } catch (final NumberFormatException ex) {
            option = new OptionalJSpinner(new SpinnerNumberModel(), state);
        }

        option.addChangeListener(new ChangeListener() {

            /** {@inheritDoc} */
            @Override
            public void stateChanged(final ChangeEvent e) {
                setting.setValue(((OptionalJSpinner) e.getSource()).isSelected()
                        + ":" + ((OptionalJSpinner) e.getSource()).getValue().
                        toString());
            }
        });

        return option;
    }

    /**
     * Initialises and returns a DurationDisplay for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getDurationOption(
            final PreferencesSetting setting) {
        DurationDisplay option;

        try {
            option = new DurationDisplay(Integer.parseInt(setting.getValue()));
        } catch (final NumberFormatException ex) {
            option = new DurationDisplay();
        }

        option.addDurationListener(new DurationListener() {

            /** {@inheritDoc} */
            @Override
            public void durationUpdated(final int newDuration) {
                setting.setValue(String.valueOf(newDuration));
            }
        });

        return option;
    }

    /**
     * Initialises and returns a ColourChooser for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getColourOption(
            final PreferencesSetting setting) {
        final OptionalColourChooser option = new OptionalColourChooser(
                controller.getIconManager(),
                controller.getColourManager(),
                setting.getValue(), true, true, true);

        option.addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                final OptionalColourChooser chooser
                        = ((OptionalColourChooser) e.getSource());
                setting.setValue(chooser.isEnabled() + ":"
                        + chooser.getColour());
            }
        });

        return option;
    }

    /**
     * Initialises and returns an OptionalColourChooser for the specified
     * setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getOptionalColourOption(
            final PreferencesSetting setting) {
        final boolean state = setting.getValue() != null
                && !setting.getValue().startsWith("false:");
        final String colour = setting.getValue() == null ? "0" : setting
                .getValue().substring(1 + setting.getValue().indexOf(':'));

        final OptionalColourChooser option = new OptionalColourChooser(
                controller.getIconManager(), controller.getColourManager(),
                colour, state, true, true);

        option.addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                setting.setValue(((OptionalColourChooser) e.getSource())
                        .isEnabled() + ":" + ((OptionalColourChooser) e
                        .getSource()).getColour());
            }
        });

        return option;
    }

    /**
     * Initialises and returns an Font Chooser for the specified setting.
     *
     * @param setting The setting to create the component for
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getFontOption(final PreferencesSetting setting) {
        final String value = setting.getValue();

        final FontPicker option = new FontPicker(value);

        option.addActionListener(new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Object value = ((FontPicker) e.getSource())
                        .getSelectedItem();
                if (value instanceof Font) {
                    setting.setValue(((Font) value).getFamily());
                } else {
                    setting.setValue(null);
                }
            }
        });

        return option;
    }

    /**
     * Initialises and returns a FileBrowser for the specified setting.
     *
     * @param setting The setting to create the component for
     * @param type The type of filechooser we want (Files/Directories/Both)
     *
     * @return A JComponent descendent for the specified setting
     */
    private JComponent getFileBrowseOption(
            final PreferencesSetting setting, final int type) {
        final FileBrowser option = new FileBrowser(controller.getIconManager(),
                setting, type);

        option.addActionListener(new ActionListener() {
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                setting.setValue(option.getPath());
            }
        });

        option.addKeyListener(new KeyAdapter() {
            /** {@inheritDoc} */
            @Override
            public void keyReleased(final KeyEvent e) {
                setting.setValue(((JTextField) e.getSource()).getText());
            }
        });

        return option;
    }

    /**
     * Initialises and returns a Label for the specified setting.
     *
     * @param setting The setting to create the component for
     *
     * @return A JComponent descendent for the specified setting
     */
    private static JComponent getLabelOption(final PreferencesSetting setting) {
        final JPanel panel = new JPanel(new MigLayout("fill"));
        panel.add(new TextLabel(setting.getValue()));
        panel.setBorder(BorderFactory.createTitledBorder(panel.getBorder(),
                setting.getTitle()));
        return panel;
    }

}
