package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
import jetoze.tzudoku.model.PuzzleState;
import jetoze.tzudoku.model.Value;

final class UiConstants {

    static final int THICK_BORDER_WIDTH = 3;

    static final int THIN_BORDER_WIDTH = 1;

    private static final Color SELECTION_COLOR = new Color(0xff, 0xea, 0x97);
    
    private static final Color INVALID_CELL_COLOR = new Color(0xd8, 0x9d, 0x9e);

    private static final Color BORDER_COLOR = Color.BLACK;

    private static final int LARGE_BUTTON_FONT_SIZE = 20;

    private static final Font LARGE_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, LARGE_BUTTON_FONT_SIZE);
    
    private static final int SMALL_BUTTON_FONT_SIZE = 12;

    private static final Font SMALL_BUTTON_FONT = new Font("Tahoma", Font.PLAIN, SMALL_BUTTON_FONT_SIZE);
    
    private static final int VALUE_BUTTON_SIZE = 40;

    private static final Color GIVEN_VALUE_COLOR = Color.BLACK;

    private static final Color ENTERED_VALUE_COLOR = new Color(0x24, 0x6e, 0xe2);

    private static final Color PENCIL_MARK_COLOR = ENTERED_VALUE_COLOR;
    
    private static final int COLOR_SELECTION_ICON_SIZE = 16;
    
    private static final int PUZZLE_STATE_ICON_SIZE = 12;
    
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

    static Border getBoardBorder() {
        return new LineBorder(BORDER_COLOR, THICK_BORDER_WIDTH);
    }

    static void drawGrid(Graphics2D g, GridSize gridSize) {
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        g.setColor(BORDER_COLOR);

        Stroke thinStroke = new BasicStroke(THIN_BORDER_WIDTH * 2);
        Stroke thickStroke = new BasicStroke(THICK_BORDER_WIDTH * 2);

        // The horizontal lines.
        int y = THICK_BORDER_WIDTH + gridSize.getCellSize();
        for (int n = 1; n < 9; ++n) {
            boolean thick = (n % 3) == 0;
            g.setStroke(thick ? thickStroke : thinStroke);
            g.drawLine(0, y, gridSize.getBoardSize(), y);
            y += gridSize.getCellSize() + (thick ? THICK_BORDER_WIDTH : THIN_BORDER_WIDTH);
        }

        // The vertical lines
        int x = THICK_BORDER_WIDTH + gridSize.getCellSize();
        for (int n = 1; n < 9; ++n) {
            boolean thick = (n % 3) == 0;
            g.setStroke(thick ? thickStroke : thinStroke);
            g.drawLine(x, 0, x, gridSize.getBoardSize());
            x += gridSize.getCellSize() + (thick ? THICK_BORDER_WIDTH : THIN_BORDER_WIDTH);
        }

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    static void fillCellBackground(Graphics2D g, 
                                   int cellSize, 
                                   CellColor cellColor, 
                                   boolean selected, 
                                   boolean invalid) {
        Color originalColor = g.getColor();

        Color bg = getCellBackground(cellColor, selected, invalid);
        g.setColor(bg);
        g.fillRect(0, 0, cellSize, cellSize);

        g.setColor(originalColor);
    }

    private static Color getCellBackground(CellColor cellColor, boolean selected, boolean invalid) {
        if (selected) {
            return SELECTION_COLOR;
        } else if (invalid) {
            return INVALID_CELL_COLOR;
        } else {
            return getColorOfCell(cellColor);
        }
    }

    static void drawValue(Graphics2D g, Value value, boolean isGiven, GridSize gridSize) {
        Font originalFont = g.getFont();
        Color originalColor = g.getColor();

        g.setFont(gridSize.getValueFont());
        g.setColor(isGiven ? GIVEN_VALUE_COLOR : ENTERED_VALUE_COLOR);

        String text = Integer.toString(value.toInt());
        drawTextCentered(g, gridSize.getValueFont(), text, gridSize);

        g.setFont(originalFont);
        g.setColor(originalColor);
    }

    private static void drawTextCentered(Graphics2D g, Font font, String text, GridSize gridSize) {
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = (gridSize.getCellSize() - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java
        // 2d 0 is top of the screen)
        int y = ((gridSize.getCellSize() - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        // Draw the String
        g.drawString(text, x, y);
    }

    static void drawPencilMarks(Graphics2D g, PencilMarks pencilMarks, GridSize gridSize) {
        if (pencilMarks.isEmpty()) {
            return;
        }

        Font originalFont = g.getFont();
        Color originalColor = g.getColor();

        g.setFont(gridSize.getPencilMarkFont());
        g.setColor(PENCIL_MARK_COLOR);

        if (pencilMarks.hasCornerMarks()) {
            int num = 1;
            for (Value pencilMark : pencilMarks.iterateOverCornerMarks()) {
                String text = pencilMark.toString();
                Point p = gridSize.getCornerPencilMarkLocation(g, text, num);
                g.drawString(text, p.x, p.y);
                ++num;
            }
        }
        if (pencilMarks.hasCenterMarks()) {
            String text = pencilMarks.centerAsString();
            drawTextCentered(g, gridSize.getPencilMarkFont(), text, gridSize);
        }

        g.setFont(originalFont);
        g.setColor(originalColor);
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
    
    static Icon getPuzzleStateIcon(PuzzleState state) {
        switch (state) {
        case NEW:
            return new PuzzleStateIcon(CELL_COLOR_MAP.get(CellColor.BLUE));
        case PROGRESS:
            return new PuzzleStateIcon(CELL_COLOR_MAP.get(CellColor.ORANGE));
        case SOLVED:
            return new PuzzleStateIcon(CELL_COLOR_MAP.get(CellColor.GREEN));
        default:
            throw new RuntimeException("Unknown state: " + state);
        }
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
    
    
    private static class PuzzleStateIcon implements Icon {
        private final Color color;

        public PuzzleStateIcon(Color color) {
            this.color = requireNonNull(color);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color originalColor = g.getColor();
            g.setColor(color);
            g.fillOval(x, y, getIconWidth(), getIconHeight());
            g.setColor(originalColor);
        }

        @Override
        public int getIconWidth() {
            return PUZZLE_STATE_ICON_SIZE;
        }

        @Override
        public int getIconHeight() {
            return PUZZLE_STATE_ICON_SIZE;
        }
    }
    
    

    private UiConstants() {/**/}

}
