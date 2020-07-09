package jetoze.tzudoku.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.UnknownCell;
import jetoze.tzudoku.model.Value;

class CellUi extends JComponent {
	private final Position position;
	private final Cell cell;
	private boolean selected;
	
	// TODO: Background color.
	
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

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		UiConstants.fillCellBackground(g2, selected);
		cell.getValue().ifPresentOrElse(value -> renderValue(g2, value), 
				() -> renderPencilMarks(g2));
	}

	private void renderValue(Graphics2D g, Value value) {
		UiConstants.drawValue(g, value, cell.isGiven());
	}
	
	private void renderPencilMarks(Graphics2D g) {
		if (!cell.isGiven()) {
			UiConstants.drawPencilMarks(g, ((UnknownCell) cell).getPencilMarks());
		}
	}
}