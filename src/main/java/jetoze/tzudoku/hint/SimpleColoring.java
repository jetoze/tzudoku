package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class SimpleColoring implements Hint {

    private final Grid grid;
    private final Value value;
    private final ImmutableSet<Position> targets;
    
    public SimpleColoring(Grid grid, Value value, Set<Position> targets) {
        this.grid = requireNonNull(grid);
        this.value = requireNonNull(value);
        this.targets = ImmutableSet.copyOf(targets);
        checkArgument(!targets.isEmpty());
    }

    @Override
    public SolvingTechnique getTechnique() {
        return SolvingTechnique.SIMPLE_COLORING;
    }

    /**
     * Returns the value that can be eliminated as a candidate.
     */
    public Value getValue() {
        return value;
    }

    /**
     * Returns the positions of the cells from which the value can be eliminated.
     */
    public ImmutableSet<Position> getTargets() {
        return targets;
    }

    /**
     * Eliminates the candidate value from the target cells.
     */
    @Override
    public void apply() {
        HintUtils.eliminateCandidates(grid, targets, Collections.singleton(value));
    }

    public static Optional<SimpleColoring> findNext(Grid grid) {
        Detector detector = new Detector(grid);
        return Optional.ofNullable(detector.find());
    }
    

    // TODO: I need a few more iterations to tidy me up.
    private static class Detector {
        private final Grid grid;
        private final Table<Value, House, ConjugatePair> allPairs = HashBasedTable.create();
        
        public Detector(Grid grid) {
            this.grid = grid;
        }
        
        @Nullable
        public SimpleColoring find() {
            House.ALL.stream().forEach(this::collectConjugatePairsInHouse);
            for (Value value : allPairs.rowKeySet()) {
                SimpleColoring result = searchForValue(value);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
        
        @Nullable
        private SimpleColoring searchForValue(Value value) {
            ImmutableMultimap<Position, ConjugatePair> positions = getPositionsForValue(value);
            Set<Position> visitedPositions = new HashSet<>();
            for (Position p : positions.keySet()) {
                if (visitedPositions.contains(p)) {
                    continue;
                }
                ColorCoder colorCoder = new ColorCoder(value, p, positions);
                colorCoder.run();
                SimpleColoring hint = colorCoder.lookForColorAppearingTwiceInUnit(grid);
                if (hint == null) {
                    hint = colorCoder.lookForCellsSeeingOppositeColors(grid);
                }
                if (hint != null) {
                    return hint;
                }
                visitedPositions.addAll(colorCoder.getVisitedPositions());
            }
            return null;
        }
        
        private void collectConjugatePairsInHouse(House house) {
            EnumSet<Value> remainingValues = house.getRemainingValues(grid);
            if (remainingValues.size() < 2) {
                return;
            }
            for (Value value : remainingValues) {
                List<Position> positions = house.getPositions()
                        .filter(p -> {
                            Cell cell = grid.cellAt(p);
                            return !cell.hasValue() && cell.getCenterMarks().contains(value);
                        }).collect(toList());
                if (positions.size() == 2) {
                    allPairs.put(value, house, new ConjugatePair(positions.get(0), positions.get(1)));
                }
            }
        }
        
        /**
         * For a given Value, returns an multimap that maps an individual Position to the conjugate pairs
         * that position is a member of.
         */
        private final ImmutableMultimap<Position, ConjugatePair> getPositionsForValue(Value value) {
            ImmutableMultimap.Builder<Position, ConjugatePair> builder = ImmutableMultimap.builder();
            for (Table.Cell<Value, House, ConjugatePair> e : allPairs.cellSet()) {
                if (e.getRowKey() == value) {
                    ConjugatePair p = e.getValue();
                    builder.put(p.first, p);
                    builder.put(p.second, p);
                }
            }
            return builder.build();
        }
    }
    
    
    private static enum Color {
        ORANGE, BLUE;
        
        Color next() {
            return (this == ORANGE)
                    ? BLUE
                    : ORANGE;
        }
    }
    
    
    private static class ColorCoder {
        private final Value value;
        private final Position startPosition;
        private final ImmutableMultimap<Position, ConjugatePair> positions;
        private final Map<Position, Color> cellToColor = new HashMap<>();
        private final Multimap<Color, Position> colorToCells = HashMultimap.create();
        
        public ColorCoder(Value value, Position startPosition, ImmutableMultimap<Position, ConjugatePair> positions) {
            this.value = value;
            this.startPosition = startPosition;
            this.positions = positions;
        }
        
        public void run() {
            visit(startPosition, Color.ORANGE);
        }
        
        private void visit(Position p, Color color) {
            if (cellToColor.containsKey(p)) {
                // We have already visited this node
                return;
            }
            cellToColor.put(p, color);
            colorToCells.put(color, p);
            for (Position next : getPositionsToVisit(p)) {
                visit(next, color.next());
            }
        }
        
        private ImmutableSet<Position> getPositionsToVisit(Position start) {
            return positions.get(start).stream()
                    .flatMap(ConjugatePair::stream)
                    .filter(p -> start.sees(p))
                    .filter(Predicates.not(cellToColor::containsKey))
                    .collect(toImmutableSet());
        }
        
        @Nullable
        public SimpleColoring lookForColorAppearingTwiceInUnit(Grid grid) {
            for (Color color : Color.values()) {
                Set<House> houses = new HashSet<>();
                for (Position p : colorToCells.get(color)) {
                    House row = Type.ROW.createHouse(p.getRow());
                    if (!houses.add(row)) {
                        return eliminateAllCellsOfColor(grid, color);
                    }
                    House column = Type.COLUMN.createHouse(p.getColumn());
                    if (!houses.add(column)) {
                        return eliminateAllCellsOfColor(grid, color);
                    }
                    House box = Type.BOX.createHouse(p.getBox());
                    if (!houses.add(box)) {
                        return eliminateAllCellsOfColor(grid, color);
                    }
                }
            }
            return null;
        }
        
        private SimpleColoring eliminateAllCellsOfColor(Grid grid, Color color) {
            return new SimpleColoring(grid, value, ImmutableSet.copyOf(colorToCells.get(color)));
        }
        
        @Nullable
        public SimpleColoring lookForCellsSeeingOppositeColors(Grid grid) {
            ImmutableSet<Position> targets = Position.all()
                    .filter(isCandidateCell(grid))
                    .filter(seesTwoDifferentColors())
                    .collect(toImmutableSet());
            return targets.isEmpty()
                    ? null
                    : new SimpleColoring(grid, value, targets);
        }
        
        private Predicate<Position> isCandidateCell(Grid grid) {
            return p -> {
                Cell cell = grid.cellAt(p);
                return !cell.hasValue() && cell.getCenterMarks().contains(value) && !cellToColor.containsKey(p);
            };
        }
        
        private Predicate<Position> seesTwoDifferentColors() {
            return p -> {
                boolean seesBlue = colorToCells.get(Color.BLUE).stream()
                        .anyMatch(blueCell -> p.sees(blueCell));
                boolean seesOrange = colorToCells.get(Color.ORANGE).stream()
                        .anyMatch(orange -> p.sees(orange));
                return seesBlue && seesOrange;
            };
        }
        
        public ImmutableSet<Position> getVisitedPositions() {
            return ImmutableSet.copyOf(cellToColor.keySet());
        }
    }
    
    
    private static class ConjugatePair {
        public final Position first;
        public final Position second;
        
        public ConjugatePair(Position first, Position second) {
            this.first = first;
            this.second = second;
            checkArgument(first != second);
            checkArgument(first.sees(second));
        }
        
        public Stream<Position> stream() {
            return Stream.of(first, second);
        }

        @Override
        public String toString() {
            return first + " and " + second;
        }
    }
    
}
