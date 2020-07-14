package jetoze.tzudoku.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;

public class GridState {
    private List<String> given;
    private List<String> entered;
    private List<PencilMarkState> pencilMarks;
    private List<ColorState> colors = new ArrayList<>();

    @SuppressWarnings("unused")
    private GridState() {
        // XXX: This is necessary to satisfy Gson when deserializing input that doesn't
        // have e.g. PencilMarks. Initializing these fields at declaration is not
        // enough, trial and error shows it has to be done inside a default constructor,
        // otherwise the field is overwritten with null by the deserializer. :/
        given = new ArrayList<>();
        entered = new ArrayList<>();
        pencilMarks = new ArrayList<>();
        colors = new ArrayList<>();
    }

    public GridState(Grid grid) {
        given = new ArrayList<>();
        entered = new ArrayList<>();
        pencilMarks = new ArrayList<>();
        for (int r = 1; r <= 9; ++r) {
            StringBuilder givenValuesInRow = new StringBuilder();
            StringBuilder enteredValuesInRow = new StringBuilder();
            Position.positionsInRow(r).forEach(p -> {
                Cell cell = grid.cellAt(p);
                String value = cell.getValue().map(Object::toString).orElse("x");
                if (cell.isGiven()) {
                    givenValuesInRow.append(value);
                    enteredValuesInRow.append("x");
                } else {
                    enteredValuesInRow.append(value);
                    givenValuesInRow.append("x");
                }
                storeAdditionalState(p, cell);
            });
            given.add(givenValuesInRow.toString());
            entered.add(enteredValuesInRow.toString());
        }
    }
    
    private void storeAdditionalState(Position p, Cell cell) {
        PencilMarks pm = cell.getPencilMarks();
        if (!pm.isEmpty()) {
            pencilMarks.add(new PencilMarkState(p, pm));
        }
        CellColor color = cell.getColor();
        if (color != CellColor.WHITE) {
            colors.add(new ColorState(p, color));
        }
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
        pencilMarks.forEach(p -> p.restore(grid));
        colors.forEach(c -> c.restore(grid));
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

    private static class PencilMarkState {
        private int row;
        private int col;
        private String corner;
        private String center;

        public PencilMarkState(Position p, PencilMarks marks) {
            this.row = p.getRow();
            this.col = p.getColumn();
            this.corner = marks.cornerAsString();
            this.center = marks.centerAsString();
        }

        public void restore(Grid grid) {
            Cell cell = grid.cellAt(new Position(row, col));
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
    
    private static class ColorState {
        private int row;
        private int col;
        private String color;
        
        public ColorState(Position p, CellColor color) {
            this.row = p.getRow();
            this.col = p.getColumn();
            this.color = color.name();
        }
        
        public void restore(Grid grid) {
            if (color == null) {
                return;
            }
            Cell cell = grid.cellAt(new Position(row, col));
            cell.setColor(CellColor.valueOf(color));
        }
    }
}
