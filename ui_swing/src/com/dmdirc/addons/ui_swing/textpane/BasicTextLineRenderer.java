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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.ui.messages.LinePosition;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Renders basic text, line wrapping where appropriate.
 */
public class BasicTextLineRenderer implements LineRenderer {

    /** Padding to add to line height. */
    private static final double LINE_PADDING = 0.2;
    /** Single Side padding for textpane. */
    private static final int SINGLE_SIDE_PADDING = 3;
    /** Both Side padding for textpane. */
    private static final int DOUBLE_SIDE_PADDING = SINGLE_SIDE_PADDING * 2;

    /** Render result to use. This instance is recycled for each render call. */
    private final RenderResult result = new RenderResult();

    private final TextPane textPane;
    private final TextPaneCanvas textPaneCanvas;
    private final IRCDocument document;

    private final Color highlightForeground;
    private final Color highlightBackground;

    /** Reused in each render pass to save creating a new list. */
    private final List<TextLayout> wrappedLines = new ArrayList<>();

    public BasicTextLineRenderer(final TextPane textPane, final TextPaneCanvas textPaneCanvas,
            final IRCDocument document) {
        this.textPane = textPane;
        this.textPaneCanvas = textPaneCanvas;
        this.document = document;

        highlightForeground = UIManager.getColor("TextArea.selectionForeground");
        highlightBackground = UIManager.getColor("TextArea.selectionBackground");
    }

    @Override
    public RenderResult render(final Graphics2D graphics, final float canvasWidth,
            final float canvasHeight, final float drawPosY, final int line,
            final boolean bottomLine) {
        result.drawnAreas.clear();
        result.textLayouts.clear();
        result.totalHeight = 0;

        final AttributedCharacterIterator iterator = document.getStyledLine(line);
        final int lineHeight = (int) (document.getLineHeight(line) * (LINE_PADDING + 1));
        final int paragraphStart = iterator.getBeginIndex();
        final int paragraphEnd = iterator.getEndIndex();
        final LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(iterator,
                graphics.getFontRenderContext());
        lineMeasurer.setPosition(paragraphStart);

        float newDrawPosY = drawPosY;

        if (bottomLine) {
            newDrawPosY += DOUBLE_SIDE_PADDING;
        }

        // Calculate layouts for each wrapped line.
        wrappedLines.clear();
        int chars = 0;
        while (lineMeasurer.getPosition() < paragraphEnd) {
            final TextLayout layout = checkNotNull(lineMeasurer.nextLayout(canvasWidth));
            chars += layout.getCharacterCount();
            wrappedLines.add(layout);
        }

        // Loop through each wrapped line
        for (int i = wrappedLines.size() - 1; i >= 0; i--) {
            final TextLayout layout = wrappedLines.get(i);

            // Calculate the Y offset
            newDrawPosY -= lineHeight;

            // Calculate the initial X position
            final float drawPosX;
            if (layout.isLeftToRight()) {
                drawPosX = SINGLE_SIDE_PADDING;
            } else {
                drawPosX = canvasWidth - layout.getAdvance();
            }

            chars -= layout.getCharacterCount();

            // Check if the target is in range
            if (newDrawPosY >= 0 || newDrawPosY <= canvasHeight) {
                renderLine(graphics, (int) canvasWidth, line, drawPosX, newDrawPosY, i, chars,
                        layout);
            }
        }

        result.totalHeight = drawPosY - newDrawPosY;
        return result;
    }

    protected void renderLine(final Graphics2D graphics, final int canvasWidth, final int line,
            final float drawPosX, final float drawPosY, final int numberOfWraps, final int chars,
            final TextLayout layout) {
        graphics.setColor(textPane.getForeground());
        layout.draw(graphics, drawPosX, drawPosY + layout.getDescent());
        doHighlight(line, chars, layout, graphics, drawPosX, drawPosY);
        result.firstVisibleLine = line;
        final LineInfo lineInfo = new LineInfo(line, numberOfWraps);
        result.textLayouts.put(lineInfo, layout);
        result.drawnAreas.put(lineInfo,
                new Rectangle(0,
                        (int) (drawPosY + 1.5 - layout.getAscent() + layout.getDescent()),
                        canvasWidth + DOUBLE_SIDE_PADDING,
                        (int) (layout.getAscent() + layout.getDescent())));
    }

    /**
     * Redraws the text that has been highlighted.
     *
     * @param line     Line number
     * @param chars    Number of characters already handled in a wrapped line
     * @param layout   Current wrapped line's textlayout
     * @param g        Graphics surface to draw highlight on
     * @param drawPosX current x location of the line
     * @param drawPosY current y location of the line
     */
    protected void doHighlight(final int line, final int chars,
            final TextLayout layout, final Graphics2D g,
            final float drawPosX, final float drawPosY) {
        final LinePosition selectedRange = textPaneCanvas.getSelectedRange();
        final int selectionStartLine = selectedRange.getStartLine();
        final int selectionStartChar = selectedRange.getStartPos();
        final int selectionEndLine = selectedRange.getEndLine();
        final int selectionEndChar = selectedRange.getEndPos();

        // Does this line need highlighting?
        if (selectionStartLine <= line && selectionEndLine >= line) {
            final int firstChar;

            // Determine the first char we care about
            if (selectionStartLine < line || selectionStartChar < chars) {
                firstChar = 0;
            } else {
                firstChar = selectionStartChar - chars;
            }

            // ... And the last
            final int lastChar;
            if (selectionEndLine > line || selectionEndChar > chars + layout.getCharacterCount()) {
                lastChar = layout.getCharacterCount();
            } else {
                lastChar = selectionEndChar - chars;
            }

            // If the selection includes the chars we're showing
            if (lastChar > 0 && firstChar < layout.getCharacterCount() && lastChar > firstChar) {
                doHighlight(line,
                        layout.getLogicalHighlightShape(firstChar, lastChar), g,
                        drawPosY, drawPosX, chars + firstChar, chars + lastChar);
            }
        }
    }

    private void doHighlight(final int line, final Shape logicalHighlightShape, final Graphics2D g,
            final float drawPosY, final float drawPosX, final int firstChar, final int lastChar) {
        final AttributedCharacterIterator iterator = document.getStyledLine(line);
        final AttributedString as = new AttributedString(iterator, firstChar, lastChar);

        as.addAttribute(TextAttribute.FOREGROUND, highlightForeground);
        as.addAttribute(TextAttribute.BACKGROUND, highlightBackground);
        final TextLayout newLayout = new TextLayout(as.getIterator(),
                g.getFontRenderContext());
        final int trans = (int) (newLayout.getDescent() + drawPosY);

        g.translate(logicalHighlightShape.getBounds().getX(), 0);
        newLayout.draw(g, drawPosX, trans);
        g.translate(-1 * logicalHighlightShape.getBounds().getX(), 0);
    }

}
