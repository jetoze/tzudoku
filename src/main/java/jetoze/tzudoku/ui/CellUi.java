package jetoze.tzudoku.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.PencilMarks;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

class CellUi extends JComponent {
    private final Position position;
    private final Cell cell;
    private boolean selected;
    private boolean invalid;

    public CellUi(Position position, Cell cell) {
        this.position = position;
        this.cell = cell;
        setOpaque(true);
    }

    public Cell getCell() {
        return cell;
    }

    public Position getPosition() {
        return position;
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
        UiConstants.fillCellBackground(g2, cell.getColor(), selected, invalid);
        cell.getValue().ifPresentOrElse(value -> renderValue(g2, value), () -> renderPencilMarks(g2));
    }

    private void renderValue(Graphics2D g, Value value) {
        UiConstants.drawValue(g, value, cell.isGiven());
    }

    private void renderPencilMarks(Graphics2D g) {
        PencilMarks marks = cell.getPencilMarks();
        if (!marks.isEmpty()) {
            UiConstants.drawPencilMarks(g, marks);
        }
    }
}