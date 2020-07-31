package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Utility class that builds grids for the Hint-related unit tests.
 * <p>
 * This builder offers a (hopefully) convenient way of building grids 
 * based on the fact that the hints typically work with cells that either
 * already have a value, or has one or more center pencil marks. A cell
 * with a value is represented by the string representation of that value, 
 * e.g. "7". A cell with center pencil marks is represented by the string
 * "[xyz]", e.g. ["25"], "[12369]". Optional spaces can be provided between
 * cells in the input strings to provide visual clues for how the cells 
 * are grouped - spaces are ignored when parsing the string (except when
 * parsing pencil marks, spaces are not allowed between "[" and "]").
 * 
 */
class GridBuilder {

    private final Map<House, List<Cell>> houses = new HashMap<>();
    
    private GridBuilder() {/**/}
    
    static GridBuilder builder() {
        return new GridBuilder();
    }

    /**
     * Builds the resulting grid.
     */
    public Grid build() {
        Grid.Builder builder = Grid.builder();
        houses.forEach(builder::house);
        return builder.build();
    }
    
    public GridBuilder row(int rowNum, String input) {
        List<Cell> cells = parseString(input, 9);
        houses.put(new House(Type.ROW, rowNum), cells);
        return this;
    }
    
    public GridBuilder column(int colNum, String input) {
        List<Cell> cells = parseString(input, 9);
        houses.put(new House(Type.COLUMN, colNum), cells);
        return this;
    }
    
    public GridBuilder box(int boxNum, String row1, String row2, String row3) {
        List<Cell> cells = new ArrayList<>();
        cells.addAll(parseString(row1, 3));
        cells.addAll(parseString(row2, 3));
        cells.addAll(parseString(row3, 3));
        houses.put(new House(Type.BOX, boxNum), cells);
        return this;
    }
    
    public GridBuilder fullyUnknownRow(int rowNum) {
        return fullyUnknownHouse(new House(Type.ROW, rowNum));
    }
    
    public GridBuilder fullyUnknownColumn(int colNum) {
        return fullyUnknownHouse(new House(Type.COLUMN, colNum));
    }
    
    public GridBuilder fullyUnknownBox(int boxNum) {
        return fullyUnknownHouse(new House(Type.BOX, boxNum));
    }

    private List<Cell> parseString(String input, int expectedNumberOfCells) {
        List<Cell> cells = new StringParser(input).parse();
        checkArgument(cells.size() == expectedNumberOfCells, "Expected %s number of cells, got %s", 
                expectedNumberOfCells, cells.size());
        return cells;
    }
    
    private Cell cellWithAllCandidates() {
        Cell cell = Cell.empty();
        cell.getCenterMarks().setValues(Value.ALL);
        return cell;
    }
    
    private GridBuilder fullyUnknownHouse(House house) {
        houses.put(house, IntStream.rangeClosed(1, 9)
                .mapToObj(i -> cellWithAllCandidates())
                .collect(toList()));
        return this;
    }

    
    private static class StringParser {
        private final String input;
        private int index;
        private final List<Cell> cells = new ArrayList<>();
        
        public StringParser(String input) {
            this.input = input;
        }
        
        public List<Cell> parse() {
            while (index < input.length()) {
                char c = input.charAt(index);
                // XXX: This assumes ascii
                if (Character.isDigit(c)) {
                    // FIXME: We use this same conversion from char to Value in PuzzleStorageRepresentation.
                    Value value = Value.of(c - 48);
                    cells.add(Cell.given(value));
                    ++index;
                } else if (c == '[') {
                    Cell cell = parsePencilMarks();
                    cells.add(cell);
                } else if (c == ' ') {
                    // spaces are ignored
                    ++index;
                } else {
                    throw illegalCharacter(c);
                }
            }
            return cells;
        }
        
        private Cell parsePencilMarks() {
            int startIndex = index;
            EnumSet<Value> values = EnumSet.noneOf(Value.class);
            ++index;
            while (index < input.length()) {
                char c = input.charAt(index);
                ++index;
                if (Character.isDigit(c)) {
                    // FIXME: We use this same conversion from char to Value in PuzzleStorageRepresentation.
                    Value value = Value.of(c - 48);
                    values.add(value);
                } else if (c == ']') {
                    Cell cell = Cell.empty();
                    cell.getCenterMarks().setValues(values);
                    return cell;
                } else {
                    throw illegalCharacter(c);
                }
            }
            throw new IllegalArgumentException("Unterminated pencil mark cell at position " + startIndex + 
                    " in input \"" + input + "\"");
        }

        private IllegalArgumentException illegalCharacter(char c) {
            return new IllegalArgumentException("Unexpected character '" + c + "' at position " + index + 
                    " in input string \"" + input + "\"");
        }
    }
    
}
