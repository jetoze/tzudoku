package jetoze.tzudoku.ui;

import static java.util.Objects.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.model.CellColor;
import jetoze.tzudoku.model.PencilMarks;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

final class UiConstants {

    static final int CELL_SIZE = 50;

    static final int THICK_BORDER_WIDTH = 4;

    static final int THIN_BORDER_WIDTH = 1;

    static final int BOARD_SIZE = 9/* cells */ * CELL_SIZE +
    // TODO: No idea why this is necessary
            ((int) (3.5/* thick borders */ * THICK_BORDER_WIDTH)) + 8/* thin borders */ * THIN_BORDER_WIDTH;

    private static final Color SELECTION_COLOR = new Color(0xff, 0xea, 0x97);

    private static final Color BORDER_COLOR = Color.BLACK;

    private static final int VALUE_FONT_SIZE = (2 * CELL_SIZE) / 3;

    private static final Font VALUE_FONT = new Font("Tahoma", Font.PLAIN, VALUE_FONT_SIZE);

    private static final int LARGE_BUTTON_FONT_SIZE = 20;

    private static final Font LARGE_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, LARGE_BUTTON_FONT_SIZE);
    
    private static final int SMALL_BUTTON_FONT_SIZE = 12;

    private static final Font SMALL_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, SMALL_BUTTON_FONT_SIZE);
    
    private static final int VALUE_BUTTON_SIZE = 40;

    private static final Color GIVEN_VALUE_COLOR = Color.BLACK;

    private static final Color ENTERED_VALUE_COLOR = new Color(0x24, 0x6e, 0xe2);

    private static final int PENCIL_MARK_FONT_SIZE = CELL_SIZE / 4;

    private static final Font PENCIL_MARK_FONT = new Font("Tahoma", Font.PLAIN, PENCIL_MARK_FONT_SIZE);

    private static final Color PENCIL_MARK_COLOR = ENTERED_VALUE_COLOR;
    
    private static final int COLOR_SELECTION_ICON_SIZE = 16;
    
    private static final ImmutableMap<CellColor, Color> CELL_COLOR_MAP = ImmutableMap.<CellColor, Color>builder()
            .put(CellColor.BLACK, Color.BLACK)
            .put(CellColor.WHITE, Color.WHITE)
            .put(CellColor.GRAY, new Color(0xcf, 0xcf, 0xcf))
            .put(CellColor.YELLOW, new Color(0xfb, 0xe7, 0xa4))
            .put(CellColor.GREEN, new Color(0xd2, 0xef, 0xa9))
            .put(CellColor.ORANGE, new Color(0xf4, 0xba, 0x9d))
            .put(CellColor.PURPLE, new Color(0xe8, 0xa2, 0xf2))
            .put(CellColor.RED, new Color(0xf1, 0x94, 0x94))
            .put(CellColor.BLUE, new Color(0x9e, 0xdd, 0xf1))
            .build();

    static Rectangle getCellBounds(Position pos) {
        Point upperLeft = getUpperLeftCellCorner(pos.getRow(), pos.getColumn());
        return new Rectangle(upperLeft.x, upperLeft.y, CELL_SIZE, CELL_SIZE);
    }

    private static Point getUpperLeftCellCorner(int row, int col) {
        int numOfPrecedingThickBordersToTheLeft = (col - 1) / 3;
        int numOfPrecedingThinBordersToTheLeft = (col - 1) - numOfPrecedingThickBordersToTheLeft;
        int x = THICK_BORDER_WIDTH /* left edge */ + (col - 1) * CELL_SIZE /* preceding cells */
                + numOfPrecedingThickBordersToTheLeft * THICK_BORDER_WIDTH
                + numOfPrecedingThinBordersToTheLeft * THIN_BORDER_WIDTH;

        int numOfPrecedingThickBordersAbove = (row - 1) / 3;
        int numOfPrecedingThinBordersAbove = (row - 1) - numOfPrecedingThickBordersAbove;
        int y = THICK_BORDER_WIDTH /* left edge */ + (row - 1) * CELL_SIZE /* preceding cells */
                + numOfPrecedingThickBordersAbove * THICK_BORDER_WIDTH
                + numOfPrecedingThinBordersAbove * THIN_BORDER_WIDTH;

        return new Point(x, y);
    }

    static Border getBoardBorder() {
        return new LineBorder(BORDER_COLOR, THICK_BORDER_WIDTH);
    }

    static void drawGrid(Graphics2D g) {
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        g.setColor(BORDER_COLOR);

        Stroke thinStroke = new BasicStroke(THIN_BORDER_WIDTH * 2);
        Stroke thickStroke = new BasicStroke(THICK_BORDER_WIDTH * 2);

        // The horizontal lines.
        int y = THICK_BORDER_WIDTH + CELL_SIZE;
        for (int n = 1; n < 9; ++n) {
            boolean thick = (n % 3) == 0;
            g.setStroke(thick ? thickStroke : thinStroke);
            g.drawLine(0, y, BOARD_SIZE, y);
            y += CELL_SIZE + (thick ? THICK_BORDER_WIDTH : THIN_BORDER_WIDTH);
        }

        // The vertical lines
        int x = THICK_BORDER_WIDTH + CELL_SIZE;
        for (int n = 1; n < 9; ++n) {
            boolean thick = (n % 3) == 0;
            g.setStroke(thick ? thickStroke : thinStroke);
            g.drawLine(x, 0, x, BOARD_SIZE);
            x += CELL_SIZE + (thick ? THICK_BORDER_WIDTH : THIN_BORDER_WIDTH);
        }

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    static void fillCellBackground(Graphics2D g, CellColor cellColor, boolean selected) {
        Color originalColor = g.getColor();

        Color bg = selected ? SELECTION_COLOR : getColorOfCell(cellColor);
        g.setColor(bg);
        g.fillRect(0, 0, CELL_SIZE, CELL_SIZE);

        g.setColor(originalColor);
    }

    static void drawValue(Graphics2D g, Value value, boolean isGiven) {
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();

        g.setFont(VALUE_FONT);
        g.setColor(isGiven ? GIVEN_VALUE_COLOR : ENTERED_VALUE_COLOR);

        String text = Integer.toString(value.toInt());
        drawTextCentered(g, VALUE_FONT, text);

        g.setFont(originalFont);
        g.setColor(originalColor);
    }

    private static void drawTextCentered(Graphics2D g, Font font, String text) {
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = (CELL_SIZE - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java
        // 2d 0 is top of the screen)
        int y = ((CELL_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        // Draw the String
        g.drawString(text, x, y);
    }

    static void drawPencilMarks(Graphics2D g, PencilMarks pencilMarks) {
        if (pencilMarks.isEmpty()) {
            return;
        }

        Font originalFont = g.getFont();
        Color originalColor = g.getColor();

        g.setFont(PENCIL_MARK_FONT);
        g.setColor(PENCIL_MARK_COLOR);

        if (pencilMarks.hasCornerMarks()) {
            int num = 1;
            for (Value pencilMark : pencilMarks.iterateOverCornerMarks()) {
                String text = pencilMark.toString();
                Point p = getCornerPencilMarkLocation(g, text, num);
                g.drawString(text, p.x, p.y);
                ++num;
            }
        }
        if (pencilMarks.hasCenterMarks()) {
            String text = pencilMarks.centerAsString();
            drawTextCentered(g, PENCIL_MARK_FONT, text);
        }

        g.setFont(originalFont);
        g.setColor(originalColor);
    }

    private static Point getCornerPencilMarkLocation(Graphics2D g, String text, int pencilMarkNo) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        switch (pencilMarkNo) {
        case 1:
        case 9: // yeah, the 9th pencil mark overwrites the first one...
            return new Point(4, metrics.getHeight());
        case 2:
            return new Point(CELL_SIZE - 4 - metrics.stringWidth(text), metrics.getHeight());
        case 3:
            return new Point(4, CELL_SIZE - 4 - metrics.getHeight() + metrics.getAscent());
        case 4:
            return new Point(CELL_SIZE - 4 - metrics.stringWidth(text),
                    CELL_SIZE - 4 - metrics.getHeight() + metrics.getAscent());
        case 5:
            return new Point((CELL_SIZE - metrics.stringWidth(text)) / 2, metrics.getHeight());
        case 6:
            return new Point((CELL_SIZE - metrics.stringWidth(text)) / 2,
                    CELL_SIZE - 4 - metrics.getHeight() + metrics.getAscent());
        case 7:
            return new Point(4, ((CELL_SIZE - metrics.getHeight()) / 2) + metrics.getAscent());
        case 8:
            return new Point(CELL_SIZE - 4 - metrics.stringWidth(text),
                    ((CELL_SIZE - metrics.getHeight()) / 2) + metrics.getAscent());
        default:
            throw new RuntimeException("Unexpected number of pencil marks: " + pencilMarkNo);
        }
    }

    static void makeOverLarge(AbstractButton button) {
        button.setFont(LARGE_BUTTON_FONT);
    }
    
    static JButton createValueButton(Action action) {
        requireNonNull(action);
        JButton btn = new JButton(action) {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(VALUE_BUTTON_SIZE, VALUE_BUTTON_SIZE);
            }
        };
        btn.setMargin(null);
        btn.setFont(SMALL_BUTTON_FONT);
        return btn;
    }
    
    static Color getColorOfCell(CellColor cellColor) {
        requireNonNull(cellColor);
        return CELL_COLOR_MAP.get(cellColor);
    }
    
    static Icon getCellColorSelectionIcon(CellColor cellColor) {
        return new CellColorIcon(getColorOfCell(cellColor));
    }
    
    private static class CellColorIcon implements Icon {
        private final Color color;

        public CellColorIcon(Color color) {
            this.color = requireNonNull(color);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color originalColor = g.getColor();
            g.setColor(color);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(originalColor);
        }

        @Override
        public int getIconWidth() {
            return COLOR_SELECTION_ICON_SIZE;
        }

        @Override
        public int getIconHeight() {
            return COLOR_SELECTION_ICON_SIZE;
        }
    }
    
    

    private UiConstants() {/**/}

}
