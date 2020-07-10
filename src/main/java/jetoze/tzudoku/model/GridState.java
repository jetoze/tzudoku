package jetoze.tzudoku.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;

public class GridState {
    private List<String> given;
    private List<String> entered;
    private List<AdditionalCellState> cellStates;

    @SuppressWarnings("unused")
    private GridState() {
        // XXX: This is necessary to satisfy Gson when deserializing input that doesn't
        // have e.g. PencilMarks. Initializing these fields at declaration is not
        // enough,
        // trial and error shows it has to be done inside a default constructor,
        // otherwise
        // the field is overwritten with null by the deserializer. :/
        given = new ArrayList<>();
        entered = new ArrayList<>();
        cellStates = new ArrayList<>();
    }

    public GridState(Grid grid) {
        given = new ArrayList<>();
        entered = new ArrayList<>();
        cellStates = new ArrayList<>();
        for (int r = 1; r <= 9; ++r) {
            StringBuilder givenValuesInRow = new StringBuilder();
            StringBuilder enteredValuesInRow = new StringBuilder();
            for (Position p : Position.positionsInRow(r)) {
                Cell cell = grid.cellAt(p);
                String value = cell.getValue().map(Object::toString).orElse("x");
                if (cell.isGiven()) {
                    givenValuesInRow.append(value);
                    enteredValuesInRow.append("x");
                } else {
                    enteredValuesInRow.append(value);
                    givenValuesInRow.append("x");
                    storeAdditionalState(p, cell);
                }
                if (cell.hasNewInformation()) {
                    
                }
            }
            given.add(givenValuesInRow.toString());
            entered.add(enteredValuesInRow.toString());
        }
    }
    
    private void storeAdditionalState(Position p, Cell cell) {
        PencilMarks pm = cell.getPencilMarks();
        CellColor color = cell.getColor();
        cellStates.add(new AdditionalCellState(p, pm, color));
    }

    public Grid restoreGrid() {
        List<Cell> cells = new ArrayList<>();
        for (int row = 1; row <= 9; ++row) {
            String givenValues = given.get(row - 1);
            String enteredValues = entered.isEmpty() ? 
                    "x".repeat(9) // Allows us to leave out this field in JSON
                    : entered.get(row - 1);
            for (int col = 1; col <= 9; ++col) {
                Cell cell = restoreCell(givenValues, enteredValues, col);
                cells.add(cell);
            }
        }
        Grid grid = new Grid(cells);
        cellStates.forEach(s -> s.restore(grid));
        return grid;
    }

    private Cell restoreCell(String givenValuesInRow, String enteredValuesInRow, int col) {
        char c = givenValuesInRow.charAt(col - 1);
        if (c != 'x') {
            return Cell.given(Value.of(c - 48));
        }
        c = enteredValuesInRow.charAt(col - 1);
        if (c != 'x') {
            return Cell.unknownWithValue(Value.of(c - 48));
        }
        return Cell.empty();
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static GridState fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, GridState.class);
    }

    private static class AdditionalCellState {
        private int row;
        private int col;
        private String corner;
        private String center;
        private CellColor color;

        public AdditionalCellState(Position p, PencilMarks marks, CellColor color) {
            this.row = p.getRow();
            this.col = p.getColumn();
            this.corner = marks.cornerAsString();
            this.center = marks.centerAsString();
            this.color = color;
        }

        public void restore(Grid grid) {
            Cell cell = grid.cellAt(new Position(row, col));
            if (color != null) {
                cell.setColor(color);
            }
            if (cell.isGiven()) {
                return;
            }
            PencilMarks marks = cell.getPencilMarks();
            toValues(corner).forEach(marks::toggleCorner);
            toValues(center).forEach(marks::toggleCenter);
        }

        private Stream<Value> toValues(String s) {
            return s.chars().map(c -> c - 48).mapToObj(Value::of);
        }
    }
}
