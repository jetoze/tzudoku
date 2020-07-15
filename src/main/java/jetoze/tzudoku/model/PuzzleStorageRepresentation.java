package jetoze.tzudoku.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PuzzleStorageRepresentation {
    // TODO: Look into GSON TypeAdapters and custom serializers.
    private List<String> given;
    private List<String> entered;
    private List<PencilMarkState> pencilMarks;
    private List<ColorState> colors;
    private Set<Sandwich> rowSandwiches;
    private Set<Sandwich> columnSandwiches;

    private PuzzleStorageRepresentation() {
        // XXX: This is necessary to satisfy Gson when deserializing input that doesn't
        // have e.g. PencilMarks. Initializing these fields at declaration is not
        // enough, trial and error shows it has to be done inside a default constructor,
        // otherwise the field is overwritten with null by the deserializer. :/
        given = new ArrayList<>();
        entered = new ArrayList<>();
        pencilMarks = new ArrayList<>();
        colors = new ArrayList<>();
        rowSandwiches = new HashSet<>();
        columnSandwiches = new HashSet<>();
    }

    public PuzzleStorageRepresentation(Puzzle puzzle) {
        this();
        storeGrid(puzzle);
        rowSandwiches = puzzle.getSandwiches().getRows();
        columnSandwiches = puzzle.getSandwiches().getColumns();
    }

    private void storeGrid(Puzzle puzzle) {
        for (int r = 1; r <= 9; ++r) {
            StringBuilder givenValuesInRow = new StringBuilder();
            StringBuilder enteredValuesInRow = new StringBuilder();
            Position.positionsInRow(r).forEach(p -> {
                Cell cell = puzzle.getGrid().cellAt(p);
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

    public Puzzle restorePuzzle(String name) { // TODO: Include the name in the representation?
        Grid grid = restoreGrid();
        Sandwiches sandwiches = new Sandwiches(rowSandwiches, columnSandwiches);
        return new Puzzle(name, grid, sandwiches);
    }

    private Grid restoreGrid() {
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
        return new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(this);
    }

    public static PuzzleStorageRepresentation fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, PuzzleStorageRepresentation.class);
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
    
    
    public static void main(String[] args) {
        Grid grid = Grid.exampleOfUnsolvedGrid();
        Sandwiches sandwiches = Sandwiches.builder()
                .row(1, 12)
                .row(6, 0)
                .row(7, 35)
                .column(2, 9)
                .column(5, 19)
                .build();
        Puzzle p1 = new Puzzle("Test Puzzle", grid, sandwiches);
        String json = new PuzzleStorageRepresentation(p1).toJson();
        System.out.println(json);
        Puzzle p2 = PuzzleStorageRepresentation.fromJson(json).restorePuzzle("Test Puzzle");
        System.out.println(p2.getName());
        System.out.println(p2.getGrid());
        System.out.println(p2.getSandwiches());
    }
}
