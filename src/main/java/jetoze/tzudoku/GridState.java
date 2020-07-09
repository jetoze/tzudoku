package jetoze.tzudoku;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class GridState {
	private List<String> given = new ArrayList<>();
	private List<String> entered = new ArrayList<>();
	
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
				}
			}
			given.add(givenValuesInRow.toString());
			entered.add(enteredValuesInRow.toString());
		}
	}
	
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static GridState fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, GridState.class);
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
