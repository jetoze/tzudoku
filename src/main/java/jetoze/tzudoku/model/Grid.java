package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public final class Grid {

    public static Grid emptyGrid() {
        return new Grid(IntStream.rangeClosed(1, 81)
                .mapToObj(i -> Cell.empty()));
    }
    
    /**
     * Returns a deep copy of the given grid.
     */
    public static Grid copyOf(Grid grid) {
        ImmutableMap<Position, Cell> cells = grid.cells.entrySet().stream().collect(toImmutableMap(
                e -> e.getKey(),
                e -> Cell.copyOf(e.getValue())));
        return new Grid(cells);
    }

    private final ImmutableMap<Position, Cell> cells;

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
        House.ALL.stream().forEach(s -> collectDuplicates(s, bin));
        return bin.build();
    }
    
    private void collectDuplicates(House house, ImmutableSet.Builder<Position> bin) {
        Multimap<Value, Position> mm = HashMultimap.create();
        house.getPositions().forEach(p -> {
            Cell c = cells.get(p);
            c.getValue().ifPresent(v -> mm.put(v, p));
        });
        mm.asMap().values().stream()
            .filter(c -> c.size() > 1)
            .forEach(bin::addAll);
    }
    
    public void showRemainingCandidates() {
        Position.all().forEach(this::showRemainingCandidates);
    }
    
    private void showRemainingCandidates(Position p) {
        Cell cell = cells.get(p);
        if (cell.hasValue() || !cell.getCenterMarks().isEmpty()) {
            return;
        }
        Set<Value> candidates = EnumSet.allOf(Value.class);
        p.seenBy().map(cells::get).map(Cell::getValue).flatMap(Optional::stream).forEach(candidates::remove);
        cell.getCenterMarks().setValues(candidates);
    }
    
    /**
     * Checks if all the cells at the given positions in this grid have either a value
     * or one or more candidates in their center pencil marks.
     */
    public boolean allCellsHaveValueOrCandidates(Stream<Position> positions) {
        return positions.map(this::cellAt)
                .filter(Predicate.not(Cell::hasValue))
                .map(Cell::getCenterMarks)
                .noneMatch(PencilMarks::isEmpty);
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

    public boolean isEquivalent(Grid other) {
        // See comment in areEquivalent below for why we can't implement
        // equals and hashCode.
        return cells.keySet()
                .stream()
                .allMatch(p -> {
                    Cell c1 = this.cellAt(p);
                    Cell c2 = other.cellAt(p);
                    return areEquivalent(c1, c2);
                });
    }
    
    private static boolean areEquivalent(Cell c1, Cell c2) {
        // XXX: We can't implement Cell.equals at the moment, since we use
        // Cell as Map keys in a few places. We would need to associate
        // each Cell with its Position, which I'm reluctant to do.
        // Alternatively, we could stop using Cell as keys in a Map.
        return c1.getValue().equals(c2.getValue()) && 
                (c1.isGiven() == c2.isGiven()) &&
                (c1.getColor() == c2.getColor()) &&
                c1.getCornerMarks().equals(c2.getCornerMarks()) &&
                c1.getCenterMarks().equals(c2.getCenterMarks());

    }
    
    @Override
    public int hashCode() {
        return cells.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) || ((obj instanceof Grid) && this.cells.equals(((Grid) obj).cells));
    }
    

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
    
    
    public static Builder builder() {
        return new Builder();
    }
    
    
    public static final class Builder {
        private final Map<Position, Cell> gridCells = Position.all().collect(Collectors.toMap(p -> p, p -> Cell.empty()));
        private final Set<Position> addedCells = new HashSet<>();
        
        public Builder row(int rowNum, List<Cell> cells) {
            return addCells(rowNum, cells, Position::positionsInRow);
        }
        
        public Builder row(int rowNum, Cell... cells) {
            return row(rowNum, Arrays.asList(cells));
        }

        public Builder column(int colNum, List<Cell> cells) {
            return addCells(colNum, cells, Position::positionsInColumn);
        }
        
        public Builder column(int colNum, Cell... cells) {
            return column(colNum, Arrays.asList(cells));
        }

        public Builder box(int boxNum, List<Cell> cells) {
            return addCells(boxNum, cells, Position::positionsInBox);
        }
        
        public Builder box(int boxNum, Cell... cells) {
            return box(boxNum, Arrays.asList(cells));
        }
        
        public Builder house(House house, List<Cell> cells) {
            checkCells(cells);
            Iterator<Cell> it = cells.iterator();
            house.getPositions().forEach(p -> addCell(p, it.next()));
            return this;
        }

        private Builder addCells(int houseNumber, List<Cell> cells, IntFunction<Stream<Position>> positionsSupplier) {
            checkCells(cells);
            Iterator<Cell> it = cells.iterator();
            positionsSupplier.apply(houseNumber).forEach(p -> addCell(p, it.next()));
            return this;
        }
        
        private void addCell(Position p, Cell cell) {
            boolean isNew = addedCells.add(p);
            checkArgument(isNew, "A cell has already been added at position %s", p);
            gridCells.put(p, cell);
        }

        private static void checkCells(List<Cell> cells) {
            checkArgument(cells.size() == 9);
            checkArgument(cells.stream().allMatch(Objects::nonNull));
        }
        
        public Grid build() {
            return new Grid(gridCells);
        }
    }
}
