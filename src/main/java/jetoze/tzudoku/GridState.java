package jetoze.tzudoku;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;

public class GridState {
	private List<String> given = new ArrayList<>();
	private List<String> entered = new ArrayList<>();
	private List<PencilMarkState> pencilMarks = new ArrayList<>();
	
	public GridState(Grid grid) {
		for (int r = 1; r <= 9; ++r) {
			StringBuilder givenValuesInRow = new StringBuilder();
			StringBuilder enteredValuesInRow = new StringBuilder();
			for (Position p : Position.positionsInRow(r)) {
				Cell cell = grid.cellAt(p);
				String value = cell.getValue()
						.map(Object::toString)
						.orElse("x");
				if (cell.isGiven()) {
					givenValuesInRow.append(value);
					enteredValuesInRow.append("x");
				} else {
					enteredValuesInRow.append(value);
					givenValuesInRow.append("x");
					PencilMarks pm = ((UnknownCell) cell).getPencilMarks();
					if (!pm.isEmpty()) {
						pencilMarks.add(new PencilMarkState(p, pm));
					}
				}
			}
			given.add(givenValuesInRow.toString());
			entered.add(enteredValuesInRow.toString());
		}
	}
	
	public Grid restoreGrid() {
		List<Cell> cells = new ArrayList<>();
		for (int row = 1; row <= 9; ++row) {
			String givenValues = given.get(row - 1);
			String enteredValues = entered.get(row - 1);
			for (int col = 1; col <= 9; ++col) {
				Cell cell = restoreCell(givenValues, enteredValues, col);
				cells.add(cell);
			}
		}
		Grid grid = new Grid(cells);
		pencilMarks.forEach(pm -> pm.restore(grid));
		return grid;
	}
	
	private Cell restoreCell(String givenValuesInRow, String enteredValuesInRow, int col) {
		char c = givenValuesInRow.charAt(col - 1);
		if (c != 'x') {
			return GivenCell.of(Value.of(c - 48));
		}
		c = enteredValuesInRow.charAt(col - 1);
		if (c != 'x') {
			return UnknownCell.withValue(Value.of(c - 48));
		}
		return UnknownCell.empty();
	}
	
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static GridState fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, GridState.class);
	}
	
	
	private static class PencilMarkState {
		private Position position;
		private String corner;
		private String center;
		
		public PencilMarkState(Position p, PencilMarks marks) {
			this.position = p;
			this.corner = marks.cornerAsString();
			this.center = marks.centerAsString();
		}
		
		public void restore(Grid grid) {
			Cell cell = grid.cellAt(position);
			if (cell.isGiven()) {
				return;
			}
			PencilMarks marks = ((UnknownCell) cell).getPencilMarks();
			toValues(corner).forEach(marks::toggleCorner);
			toValues(center).forEach(marks::toggleCenter);
		}
		
		private Stream<Value> toValues(String s) {
			return s.chars().map(c -> c - 48)
					.mapToObj(Value::of);
		}
	}
	
	
	public static void main(String[] args) {
		Grid grid = Grid.exampleOfUnsolvedGrid();
		((UnknownCell) grid.cellAt(new Position(3, 1))).setValue(Value.SEVEN);
		GridState state = new GridState(grid);
		String json = state.toJson();
		System.out.println(json);
		
		GridState state2 = GridState.fromJson(json);
		String json2 = state2.toJson();
		System.out.println(json2);

		System.out.println(json.equals(json2));
	}
}
