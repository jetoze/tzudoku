package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

class CellUi extends JComponent {
    private final Position position;
    private Cell cell;
    private final BoardSize boardSize;
    private boolean selected;
    private boolean invalid;

    public CellUi(Position position, Cell cell, BoardSize boardSize) {
        this.position = requireNonNull(position);
        this.cell = requireNonNull(cell);
        this.boardSize = requireNonNull(boardSize);
        setOpaque(true);
        setFocusable(true);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    public Cell getCell() {
        return cell;
    }
    
    public void setCell(Cell cell) {
        this.cell = requireNonNull(cell);
    }

    public Position getPosition() {
        return position;
    }
    
    public boolean isGiven() {
        return cell.isGiven();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        UiLook.fillCellBackground(g2, boardSize.getCellSize(), cell.getColor(), selected, invalid);
        cell.getValue().ifPresentOrElse(value -> renderValue(g2, value), () -> renderPencilMarks(g2));
    }

    private void renderValue(Graphics2D g, Value value) {
        UiLook.drawValue(g, value, cell.isGiven(), boardSize);
    }

    private void renderPencilMarks(Graphics2D g) {
        if (cell.hasPencilMarks()) {
            UiLook.drawPencilMarks(g, cell.getCornerMarks(), cell.getCenterMarks(), boardSize);
        }
    }
}