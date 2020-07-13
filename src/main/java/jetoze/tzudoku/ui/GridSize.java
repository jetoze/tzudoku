package jetoze.tzudoku.ui;

import static jetoze.tzudoku.ui.UiConstants.*;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import jetoze.tzudoku.model.Position;

public enum GridSize {
    SMALL(30),
    REGULAR(50);
    
    private final int cellSize;
    private final int boardSize;
    private final Font valueFont;
    private final Font pencilMarkFont;
    
    private GridSize(int cellSize) {
        this.cellSize = cellSize;
        this.boardSize = 9/* cells */ * cellSize +
                // TODO: No idea why this is necessary
                ((int) (3.5/* thick borders */ * THICK_BORDER_WIDTH)) + 
                8/* thin borders */ * THIN_BORDER_WIDTH;
        this.valueFont = new Font("Tahoma", Font.PLAIN, (2 * cellSize) / 3);
        this.pencilMarkFont = new Font("Tahoma", Font.PLAIN, cellSize / 4);
    }
    
    public int getCellSize() {
        return cellSize;
    }
    
    public int getBoardSize() {
        return boardSize;
    }

    public Font getValueFont() {
        return valueFont;
    }
    
    public Font getPencilMarkFont() {
        return pencilMarkFont;
    }
    

    public Rectangle getCellBounds(Position pos) {
        Point upperLeft = getUpperLeftCellCorner(pos.getRow(), pos.getColumn());
        return new Rectangle(upperLeft.x, upperLeft.y, cellSize, cellSize);
    }

    public Point getUpperLeftCellCorner(int row, int col) {
        int numOfPrecedingThickBordersToTheLeft = (col - 1) / 3;
        int numOfPrecedingThinBordersToTheLeft = (col - 1) - numOfPrecedingThickBordersToTheLeft;
        int x = THICK_BORDER_WIDTH /* left edge */ + (col - 1) * cellSize /* preceding cells */
                + numOfPrecedingThickBordersToTheLeft * THICK_BORDER_WIDTH
                + numOfPrecedingThinBordersToTheLeft * THIN_BORDER_WIDTH;

        int numOfPrecedingThickBordersAbove = (row - 1) / 3;
        int numOfPrecedingThinBordersAbove = (row - 1) - numOfPrecedingThickBordersAbove;
        int y = THICK_BORDER_WIDTH /* left edge */ + (row - 1) * cellSize /* preceding cells */
                + numOfPrecedingThickBordersAbove * THICK_BORDER_WIDTH
                + numOfPrecedingThinBordersAbove * THIN_BORDER_WIDTH;

        return new Point(x, y);
    }

    public Point getCornerPencilMarkLocation(Graphics2D g, String text, int pencilMarkNo) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        switch (pencilMarkNo) {
        case 1:
        case 9: // yeah, the 9th pencil mark overwrites the first one...
            return new Point(4, metrics.getHeight());
        case 2:
            return new Point(cellSize - 4 - metrics.stringWidth(text), metrics.getHeight());
        case 3:
            return new Point(4, cellSize - 4 - metrics.getHeight() + metrics.getAscent());
        case 4:
            return new Point(cellSize - 4 - metrics.stringWidth(text),
                    cellSize - 4 - metrics.getHeight() + metrics.getAscent());
        case 5:
            return new Point((cellSize - metrics.stringWidth(text)) / 2, metrics.getHeight());
        case 6:
            return new Point((cellSize - metrics.stringWidth(text)) / 2,
                    cellSize - 4 - metrics.getHeight() + metrics.getAscent());
        case 7:
            return new Point(4, ((cellSize - metrics.getHeight()) / 2) + metrics.getAscent());
        case 8:
            return new Point(cellSize - 4 - metrics.stringWidth(text),
                    ((cellSize - metrics.getHeight()) / 2) + metrics.getAscent());
        default:
            throw new RuntimeException("Unexpected number of pencil marks: " + pencilMarkNo);
        }
    }
}
