package jetoze.tzudoku.ui;

import static com.google.common.base.Preconditions.*;
import static jetoze.tzudoku.ui.UiLook.*;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import jetoze.tzudoku.model.Position;

/**
 * Defines the size of various elements of the playing board.
 */
public enum BoardSize {
    SMALL(30),
    REGULAR(50);
    
    /**
     * The size in pixels of a single cell.
     */
    private final int cellSize;
    
    /**
     * The width in pixels of the sandwich area.
     */
    private final int sandwichAreaWidth;
    
    /**
     * The size in pixels of the 9x9 grid of cells, including borders.
     */
    private final int gridSize;
    
    /**
     * The size in pixels of the entire board, including the grid and the surrounding
     * sandwich areas.
     */
    private final int boardSize;
    private final Font valueFont;
    private final Font pencilMarkFont;
    private final Font sandwichFont;
    
    private BoardSize(int cellSize) {
        this.cellSize = cellSize;        this.gridSize = 9/* cells */ * cellSize +
                // TODO: No idea why this is necessary
                ((int) (3.5/* thick borders */ * THICK_BORDER_WIDTH)) + 
                8/* thin borders */ * THIN_BORDER_WIDTH;
        this.sandwichAreaWidth = cellSize;
        this.boardSize = gridSize + 2 * sandwichAreaWidth; // surround the board on all sides
        this.valueFont = new Font("Tahoma", Font.PLAIN, (2 * cellSize) / 3);
        this.pencilMarkFont = new Font("Tahoma", Font.PLAIN, cellSize / 4);
        this.sandwichFont = new Font("Tahoma", Font.PLAIN, cellSize / 2);
    }
    
    /**
     * Returns the size (pixels) of a single cell.
     */
    public int getCellSize() {
        return cellSize;
    }
    
    /**
     * Returns the width (pixels) of the sandwich area.
     */
    public int getSandwichAreaWidth() {
        return sandwichAreaWidth;
    }
    
    /**
     * Returns the size (pixels) of the 9x9 grid of cells.
     */
    public int getGridSize() {
        return gridSize;
    }
    
    /**
     * Returns the size (pixels) of the entire board, including the 9x9 grid of cells and
     * the sandwich areas.
     */
    public int getBoardSize() {
        return boardSize;
    }

    public Font getValueFont() {
        return valueFont;
    }
    
    public Font getPencilMarkFont() {
        return pencilMarkFont;
    }

    public Font getSandwichFont() {
        return sandwichFont;
    }

    public Rectangle getCellBounds(Position pos) {
        Point upperLeft = getUpperLeftCellCorner(pos.getRow(), pos.getColumn());
        return new Rectangle(upperLeft.x, upperLeft.y, cellSize, cellSize);
    }

    private Point getUpperLeftCellCorner(int row, int col) {
        int numOfPrecedingThickBordersToTheLeft = (col - 1) / 3;
        int numOfPrecedingThinBordersToTheLeft = (col - 1) - numOfPrecedingThickBordersToTheLeft;
        int x = getSandwichAreaWidth() +
                THICK_BORDER_WIDTH /* left edge */ + (col - 1) * cellSize /* preceding cells */
                + numOfPrecedingThickBordersToTheLeft * THICK_BORDER_WIDTH
                + numOfPrecedingThinBordersToTheLeft * THIN_BORDER_WIDTH;

        int numOfPrecedingThickBordersAbove = (row - 1) / 3;
        int numOfPrecedingThinBordersAbove = (row - 1) - numOfPrecedingThickBordersAbove;
        int y = getSandwichAreaWidth() +
                THICK_BORDER_WIDTH /* left edge */ + (row - 1) * cellSize /* preceding cells */
                + numOfPrecedingThickBordersAbove * THICK_BORDER_WIDTH
                + numOfPrecedingThinBordersAbove * THIN_BORDER_WIDTH;

        return new Point(x, y);
    }
    
    public Rectangle getRowSandwichSumBounds(int row) {
        checkArgument(row >= 1 && row <= 9);
        int x = 0;
        int y = getUpperLeftCellCorner(row, 1).y;
        return new Rectangle(x,  y, sandwichAreaWidth, sandwichAreaWidth);
    }
    
    public Rectangle getColumnSandwichSumBounds(int col) {
        checkArgument(col >= 1 && col <= 9);
        int x = getUpperLeftCellCorner(1, col).x;
        int y = 0;
        return new Rectangle(x,  y, sandwichAreaWidth, sandwichAreaWidth);
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
