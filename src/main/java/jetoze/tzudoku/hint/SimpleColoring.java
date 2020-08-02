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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

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
        return detector.find();
    }
    

    // TODO: I need a few more iterations to tidy me up.
    private static class Detector {
        private final Grid grid;
        private final Multimap<Value, ConjugatePair> allPairs = HashMultimap.create();
        
        public Detector(Grid grid) {
            this.grid = grid;
        }
        
        public Optional<SimpleColoring> find() {
            House.ALL.stream().forEach(this::collectConjugatePairsInHouse);
            return allPairs.keySet().stream()
                    .map(this::searchForValue)
                    .filter(Objects::nonNull)
                    .findAny();
        }
        
        @Nullable
        private SimpleColoring searchForValue(Value value) {
            ImmutableMultimap<Position, ConjugatePair> positions = getPositionsForValue(value);
            Set<Position> visitedPositions = new HashSet<>();
            // We iterate over all individual positions in the set of Conjugate Pair for this value, 
            // and for each position we traverse the graph of Conjugate Pairs that can be reached
            // from that position, coloring in nodes as we go. 
            // If we find that Simple Coloring can be applied, we stop and return the result. Otherwise
            // we go to the next position and traverse its graph, skipping positions that have already
            // been visited by earlier traversals.
            for (Position p : positions.keySet()) {
                if (visitedPositions.contains(p)) {
                    continue;
                }
                ColorCoder colorCoder = new ColorCoder(value, p, positions);
                SimpleColoring hint = colorCoder.run(grid);
                if (hint != null) {
                    return hint;
                }
                // Mark all positions visited by the ColorCoder run - we can skip them
                // in subsequent iterations.
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
                        .filter(HintUtils.isCandidate(grid, value))
                        .collect(toList());
                if (positions.size() == 2) {
                    allPairs.put(value, new ConjugatePair(positions.get(0), positions.get(1)));
                }
            }
        }
        
        /**
         * For a given Value, returns an multimap that maps an individual Position to the conjugate pairs
         * that position is a member of.
         */
        private final ImmutableMultimap<Position, ConjugatePair> getPositionsForValue(Value value) {
            ImmutableMultimap.Builder<Position, ConjugatePair> builder = ImmutableMultimap.builder();
            for (ConjugatePair cp : allPairs.get(value)) {
                builder.put(cp.first, cp);
                builder.put(cp.second, cp);
            }
            return builder.build();
        }
    }
    
    
    /**
     * The colors we use for the coloring. The values don't matter, we just need two of them.
     */
    private static enum Color {
        ORANGE, BLUE;
        
        /**
         * Alternates colors, ORANGE -> BLUE -> ORANGE -> BLUE -> ...
         */
        Color next() {
            return (this == ORANGE)
                    ? BLUE
                    : ORANGE;
        }
    }
    
    
    /**
     * Traverses the graph of Conjugate Pair positions, coloring in each position that is
     * visited with alternate colors.
     */
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
        
        /**
         * Colors in all the cells that can be reached from the start position, and
         * checks if a Simple Coloring hint can be applied from the result.
         * 
         * @return the Simple Coloring hint that can be applied, or null if the coloring
         *         did not produce any useful result. In the latter case, call
         *         {@link #getVisitedPositions()} to get a set of all the positions that
         *         were visited in the traversal - these positions can be skipped when
         *         continuing to process the set of Conjugate Pairs for the value in
         *         question.
         */
        @Nullable
        public SimpleColoring run(Grid grid) {
            visit(startPosition, Color.ORANGE);
            SimpleColoring result = lookForColorAppearingTwiceInUnit(grid);
            if (result == null) {
                result = lookForCellsSeeingOppositeColors(grid);
            }
            return result;
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
        private SimpleColoring lookForColorAppearingTwiceInUnit(Grid grid) {
            // FIXME: This piece of code is impressively ugly
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
        private SimpleColoring lookForCellsSeeingOppositeColors(Grid grid) {
            ImmutableSet<Position> targets = Position.all()
                    .filter(HintUtils.isCandidate(grid, value))
                    .filter(Predicate.not(cellToColor::containsKey)) // We are only interested in positions that are not part of a conjugate pair
                    .filter(seesColor(Color.ORANGE).and(seesColor(Color.BLUE)))
                    .collect(toImmutableSet());
            return targets.isEmpty()
                    ? null
                    : new SimpleColoring(grid, value, targets);
        }
        
        private Predicate<Position> seesColor(Color color) {
            return p -> colorToCells.get(color).stream()
                    .anyMatch(coloredCell -> p.sees(coloredCell));
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
