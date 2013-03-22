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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.actions.wrappers.PerformWrapper.PerformDescription;


import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

/**
 * Custom renderer for PerformDescriptions.
 *
 * @since 0.6.4
 */
public class PerformRenderer extends DMDircListCellRenderer {

    /**
     * A version number for this class.
     */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new instance of this renderer.
     *
     * @param renderer RendereParent renderer
     */
    public PerformRenderer(final ListCellRenderer renderer) {
        super(renderer);
    }

    /** {@inheritDoc} */
    @Override
    protected void renderValue(final JLabel label, final Object value,
            final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final PerformDescription perform = (PerformDescription) value;
        final String target = perform.getTarget();
        final String profile = perform.getProfile();
        final String type = perform.getType().toString();
        String friendlyText = type + " perform (" + target + ") ";

        if (profile != null) {
            friendlyText += "This profile (" + profile + ")";
        } else {
            friendlyText += "Any profile";
        }
        label.setText(friendlyText);
    }

}