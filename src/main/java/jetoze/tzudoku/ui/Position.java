package jetoze.tzudoku.ui;

import java.util.Objects;

class Position {
	private final int row;
	private final int column;
	
	public Position(int row, int column) {
		super();
		this.row = row;
		this.column = column;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public Position left() {
		return new Position(row, column > 1
				? column - 1 : 9);
	}
	
	public Position right() {
		return new Position(row, (column < 9)
				? column + 1 : 1);
	}
	
	public Position up() {
		return new Position(row > 1
				? row - 1 : 9, column);
	}
	
	public Position down() {
		return new Position((row < 9) ? row + 1 : 1, column);
	}
	
	@Override
	public String toString() {
		return String.format("[%d, %s]", row, column);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof Position) {
			Position that = (Position) o;
			return this.row == that.row && this.column == that.column;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(row, column);
	}
}