package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public final class Grid {
    private final ImmutableMap<Position, Cell> cells;

    public static Grid exampleOfSolvedGrid() {
        return new Grid(IntStream.of(
                8, 2, 7, 1, 5, 4, 3, 9, 6, 
                9, 6, 5, 3, 2, 7, 1, 4, 8, 
                3, 4, 1, 6, 8, 9, 7, 5, 2, 
                5, 9, 3, 4, 6, 8, 2, 7, 1, 
                4, 7, 2, 5, 1, 3, 6, 8, 9, 
                6, 1, 8, 9, 7, 2, 4, 3, 5, 
                7, 8, 6, 2, 3, 5, 9, 1, 4,
                1, 5, 4, 7, 9, 6, 8, 2, 3,
                2, 3, 9, 8, 4, 1, 5, 6, 7)
                .mapToObj(Grid::toCell));
    }

    private static Cell toCell(int value) {
        return (value == 0) ? Cell.empty() : Cell.given(Value.of(value));
    }

    public static Grid exampleOfUnsolvedGrid() {
        return new Grid(
                "605004002", 
                "000600901", 
                "000050300", 
                "001000000", 
                "300587006", 
                "000000400", 
                "004030000",
                "503008000", 
                "800100207");
    }
    
    public static Grid emptyGrid() {
        return new Grid(IntStream.rangeClosed(1, 81)
                .mapToObj(i -> Cell.empty()));
    }

    public Grid(String... rows) {
        checkArgument(rows.length == 9, "Must provide 9 rows");
        checkArgument(Stream.of(rows).allMatch(r -> r.length() == 9), "Each row must have 9 digits");
        ImmutableMap.Builder<Position, Cell> mapBuilder = ImmutableMap.builder();
        for (int r = 1; r <= 9; ++r) {
            for (int c = 1; c <= 9; ++c) {
                var p = new Position(r, c);
                var intVal = rows[r - 1].charAt(c - 1) - 48;
                mapBuilder.put(p, toCell(intVal));
            }
        }
        this.cells = mapBuilder.build();
    }

    public Grid(Stream<Cell> cells) {
        this(cells.collect(toList()));
    }

    public Grid(List<Cell> cells) {
        checkArgument(cells.size() == 81, "Must provide 81 cells");
        ImmutableMap.Builder<Position, Cell> mapBuilder = ImmutableMap.builder();
        Iterator<Cell> it = cells.iterator();
        for (int r = 1; r <= 9; ++r) {
            for (int c = 1; c <= 9; ++c) {
                var p = new Position(r, c);
                mapBuilder.put(p, it.next());
            }
        }
        this.cells = mapBuilder.build();
    }
    
    public Grid(Map<Position, Cell> cells) {
        checkArgument(cells.size() == 81, "Must provide 81 cells");
        this.cells = ImmutableMap.copyOf(cells);
    }

    public ImmutableMap<Position, Cell> getCells() {
        return cells;
    }

    public Cell cellAt(Position p) {
        return cells.get(requireNonNull(p));
    }
    
    public boolean isEmpty() {
        return cells.values().stream().allMatch(Cell::isEmpty);
    }

    public boolean isSolved() {
        if (!allCellsHaveValues()) {
            return false;
        }
        for (int n = 1; n <= 9; ++n) {
            if (!hasAllValues(getRow(n))) {
                return false;
            }
            if (!hasAllValues(getColumn(n))) {
                return false;
            }
            if (!hasAllValues(getBox(n))) {
                return false;
            }
        }
        return true;
    }

    private boolean allCellsHaveValues() {
        return cells.values().stream().map(Cell::getValue).allMatch(Optional::isPresent);
    }

    private boolean hasAllValues(Stream<Cell> cells) {
        Set<Value> values = cells.map(Cell::getValue).flatMap(Optional::stream).collect(toSet());
        return values.equals(Value.ALL);
    }

    public Stream<Cell> getRow(int n) {
        return Position.positionsInRow(n).map(cells::get);
    }

    public Stream<Cell> getColumn(int n) {
        return Position.positionsInColumn(n).map(cells::get);
    }

    private Stream<Cell> getBox(int n) {
        return Position.positionsInBox(n).map(cells::get);
    }

    public ValidationResult validate() {
        Set<Position> invalidPositions = new HashSet<>();
        // Empty cells
        cells.entrySet().stream()
            .filter(e -> !e.getValue().hasValue())
            .map(Entry::getKey)
            .forEach(invalidPositions::add);
        invalidPositions.addAll(getCellsWithDuplicateValues());
        return new ValidationResult(invalidPositions);
    }

    public ImmutableSet<Position> getCellsWithDuplicateValues() {
        ImmutableSet.Builder<Position> bin = ImmutableSet.builder();
        for (int n = 1; n <= 9; ++n) {
            collectDuplicates(Position.positionsInRow(n), bin);
            collectDuplicates(Position.positionsInColumn(n), bin);
            collectDuplicates(Position.positionsInRow(n), bin);
        }
        return bin.build();
    }
    
    private void collectDuplicates(Stream<Position> positions, ImmutableSet.Builder<Position> bin) {
        Multimap<Value, Position> mm = HashMultimap.create();
        positions.forEach(p -> {
            Cell c = cells.get(p);
            c.getValue().ifPresent(v -> mm.put(v, p));
        });
        mm.asMap().values().stream()
            .filter(c -> c.size() > 1)
            .flatMap(Collection::stream)
            .forEach(bin::add);
    }
    
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int row = 1; row <= 9; ++row) {
            if (row >= 2) {
                output.append(System.getProperty("line.separator"));
            }
            Position.positionsInRow(row).map(cells::get).map(Cell::getValue)
                .forEach(v -> v.ifPresentOrElse(output::append, () -> output.append("x")));
        }
        return output.toString();
    }
}
