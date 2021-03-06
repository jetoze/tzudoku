package jetoze.tzudoku.model;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import jetoze.tzudoku.constraint.ChessConstraint;
import jetoze.tzudoku.constraint.KillerCage;
import jetoze.tzudoku.constraint.KillerCages;
import jetoze.tzudoku.constraint.Sandwich;
import jetoze.tzudoku.constraint.Sandwiches;

public class PuzzleStorageRepresentation {
    private static final Comparator<Sandwich> SANDWICH_ORDER = Comparator.comparing(SandwichAdapter::houseRepresentation);
    
    private List<String> given;
    private List<String> entered;
    private List<PencilMarkState> pencilMarks;
    private List<ColorState> colors;
    private Set<Sandwich> sandwiches;
    private Set<KillerCage> killerCages;
    private Set<ChessConstraint> chessConstraints;

    private PuzzleStorageRepresentation() {
        // XXX: This is necessary to satisfy Gson when deserializing input that doesn't
        // have e.g. PencilMarks. Initializing these fields at declaration is not
        // enough, trial and error shows it has to be done inside a default constructor,
        // otherwise the field is overwritten with null by the deserializer. :/
        given = new ArrayList<>();
        entered = new ArrayList<>();
        pencilMarks = new ArrayList<>();
        colors = new ArrayList<>();
        sandwiches = new TreeSet<>(SANDWICH_ORDER);
        killerCages = new HashSet<>();
        chessConstraints = new HashSet<>();
    }

    public PuzzleStorageRepresentation(Puzzle puzzle) {
        this();
        storeGrid(puzzle);
        sandwiches.addAll(puzzle.getSandwiches().getRows());
        sandwiches.addAll(puzzle.getSandwiches().getColumns());
        killerCages.addAll(puzzle.getKillerCages().getCages());
        chessConstraints.addAll(puzzle.getChessConstraints());
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
        if (cell.hasPencilMarks()) {
            pencilMarks.add(new PencilMarkState(p, cell.getCornerMarks(), cell.getCenterMarks()));
        }
        CellColor color = cell.getColor();
        if (color != CellColor.WHITE) {
            colors.add(new ColorState(p, color));
        }
    }

    public Puzzle restorePuzzle(String name) { // TODO: Include the name in the representation?
        Grid grid = restoreGrid();
        Map<House.Type, List<Sandwich>> sandwichesByType = sandwiches.stream()
                .collect(Collectors.groupingBy(s -> s.getHouse().getType()));
        Collection<Sandwich> rowSandwiches = sandwichesByType.getOrDefault(House.Type.ROW, Collections.emptyList());
        Collection<Sandwich> columnSandwiches = sandwichesByType.getOrDefault(House.Type.COLUMN, Collections.emptyList());
        Sandwiches sandwiches = new Sandwiches(rowSandwiches, columnSandwiches);
        return new Puzzle(name, grid, sandwiches, new KillerCages(killerCages), chessConstraints);
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
        return createGson()
                .toJson(this);
    }

    public static PuzzleStorageRepresentation fromJson(String json) {
        Gson gson = createGson();
        return gson.fromJson(json, PuzzleStorageRepresentation.class);
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Sandwich.class, new SandwichAdapter())
                .registerTypeAdapter(KillerCage.class, new KillerCageAdapter())
                .setPrettyPrinting()
                .create();
    }
    

    private static class PencilMarkState {
        private int row;
        private int col;
        private String corner;
        private String center;

        public PencilMarkState(Position p, PencilMarks cornerMarks, PencilMarks centerMarks) {
            this.row = p.getRow();
            this.col = p.getColumn();
            this.corner = PencilMarks.valuesAsString(cornerMarks);
            this.center = PencilMarks.valuesAsString(centerMarks);
        }

        public void restore(Grid grid) {
            Cell cell = grid.cellAt(new Position(row, col));
            if (cell.isGiven()) {
                return;
            }
            PencilMarks cornerMarks = cell.getCornerMarks();
            toValues(corner).forEach(cornerMarks::toggle);
            PencilMarks centerMarks = cell.getCenterMarks();
            toValues(center).forEach(centerMarks::toggle);
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
    
    
    private static class SandwichAdapter extends TypeAdapter<Sandwich> {

        public static String houseRepresentation(Sandwich s) {
            House.Type houseType = s.getHouse().getType();
            return (houseType == House.Type.ROW ? "r" : "c") + s.getHouse().getNumber();
        }
        
        private static House toHouse(String s) {
            House.Type houseType = s.charAt(0) == 'r'
                    ? House.Type.ROW
                    : House.Type.COLUMN;
            int number = s.charAt(1) - 48;
            return houseType.createHouse(number);
        }
        
        @Override
        public void write(JsonWriter out, Sandwich value) throws IOException {
            out.beginArray()
                .value(houseRepresentation(value))
                .value(value.getSum())
                .endArray();
        }

        @Override
        public Sandwich read(JsonReader in) throws IOException {
            in.beginArray();
            String houseRep = in.nextString();
            House house = toHouse(houseRep);
            int sum = in.nextInt();
            in.endArray();
            return new Sandwich(house, sum);
        }
    }
    
    private static class KillerCageAdapter extends TypeAdapter<KillerCage> {

        @Override
        public void write(JsonWriter out, KillerCage cage) throws IOException {
            out.value(encode(cage));
        }

        @Override
        public KillerCage read(JsonReader in) throws IOException {
            return decode(in.nextString());
        }
        
        private String encode(KillerCage cage) {
            StringBuilder s = new StringBuilder(cage.getPositions().stream()
                    .map(Object::toString)
                    .collect(joining(" ")));
            cage.getSum().ifPresent(sum -> s.append(": ").append(sum));
            return s.toString();
        }
        
        private KillerCage decode(String s) {
            Integer sum = null;
            String positionsString = null;
            int sumMarkerIndex = s.indexOf(':');
            if (sumMarkerIndex > 0) {
                sum = Integer.parseInt(s.substring(sumMarkerIndex + 2));
                positionsString = s.substring(0, sumMarkerIndex);
            } else {
                positionsString = s;
            }
            Set<Position> positions = Stream.of(positionsString.split("\\s"))
                    .map(Position::fromString)
                    .collect(toSet());
            return (sum != null)
                    ? new KillerCage(positions, sum)
                    : new KillerCage(positions);
        }
    }

}
