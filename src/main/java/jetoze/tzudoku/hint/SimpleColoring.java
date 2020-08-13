package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Simple Coloring is a chaining strategy that follows chains of conjugate pairs for 
 * a given candidate value in the grid, assigning each cell an alternating color. It is 
 * explained e.g. here: <a href="https://www.sudokuwiki.org/Singles_Chains">https://www.sudokuwiki.org/Singles_Chains</a>
 * The colors used by this implementation are arbitrarily chosen to be "blue" and "orange". The following
 * will subsequently talk about "blue" and "orange" cells.
 * <p>
 * Since the chain only travels between conjugate pairs - the only two cells in a House in which the digit 
 * appears as a candidate - it follows that once the chain has been fully traversed we know that either
 * all the Blue cells, or all the Orange cells, will contain the candidate value in the solved puzzle.
 * <p>
 * There are two types of successful Simple Coloring hints:
 * <ul>
 * <li><b>Color appears twice in a House ("Too crowded house")</b>: If, after the chain has been completed, 
 * two (or more) cells of the same color, appear in the same House, one can conclude that the the the cells of 
 * that color cannot contain the candidate digit, as that would result in the same digit appearing twice in the 
 * same House. As a consequence, the digit can be eliminated as a candidate from the cells of that color, and 
 * can immediately be assigned to the cells of the opposite color.</li>
 * <li><b>Other cell(s) sees two cells of opposite colors ("Sees both colors")</b>: If, after the chain has 
 * been completed, there are cells that sees both Blue and Orange cells, the candidate value can be eliminated 
 * from those cells. This follows because it is guaranteed that either the Blue or the Orange cell will contain 
 * the digit in the solved puzzle, so a cell that sees both colors will always see the digit and can thus not 
 * contain the digit itself.</li>
 * </ul>
 */
public class SimpleColoring implements Hint {
    
    private final Grid grid;
    private final Value value;
    private final ImmutableMap<Color, ImmutableSet<Position>> coloredCells;
    private final ImmutableSet<Position> eliminated;
    @Nullable
    private final TooCrowdedHouse houseTooCrowded;
    
    private SimpleColoring(Grid grid, 
                           Value value,
                           Map<Color, ImmutableSet<Position>> coloredCells,
                           @Nullable TooCrowdedHouse houseTooCrowded,
                           ImmutableSet<Position> eliminated) {
        this.grid = requireNonNull(grid);
        this.value = requireNonNull(value);
        this.coloredCells = ImmutableMap.copyOf(coloredCells);
        this.houseTooCrowded = houseTooCrowded;
        this.eliminated = eliminated;
    }    

    @Override
    public SolvingTechnique getTechnique() {
        return SolvingTechnique.SIMPLE_COLORING;
    }

    /**
     * Returns the positions of the cells that were assigned the given color. This
     * is purely for informational purposes.
     */
    public ImmutableSet<Position> getCellsOfColor(Color color) {
        return coloredCells.get(requireNonNull(color));
    }
    
    /**
     * Returns the value that can be eliminated as a candidate or entered as a value.
     */
    public Value getValue() {
        return value;
    }

    /**
     * Returns the positions of the cells from which the value can be eliminated as
     * a candidate.
     * <p>
     * Note that in both the "Too crowded house" and the "Sees both colors" case,
     * there will always be at least one cell from which the digit can be
     * eliminated.
     * 
     * @return an ImmutableSet of at least one Position.
     */
    public ImmutableSet<Position> getCellsToEliminate() {
        return eliminated;
    }
    
    /**
     * If this SimpleColoring represents the case of the same color appearing twice in the
     * same House ("Too Crowded House"), this method returns the House that was too crowded.
     * This is purely for informational purposes.
     */
    public Optional<TooCrowdedHouse> getHouseTooCrowded() {
        return Optional.ofNullable(houseTooCrowded);
    }
    
    /**
     * Returns the positions of the cells, if any, in which the value can be written in.
     * <p>
     * This is for the case of a "Too crowded house", where the outcome of the hint is that
     * we know in what group of cells (blue or orange) the digit should go. In case of 
     * a "Sees both colors" hint, this method returns an empty set.
     */
    public ImmutableSet<Position> getCellsThatCanBePenciledIn() {
        return getHouseTooCrowded()
                .map(TooCrowdedHouse::getColor)
                .map(Color::next)
                .map(coloredCells::get)
                .orElse(ImmutableSet.of());
    }

    @Override
    public void apply() {
        HintUtils.eliminateCandidates(grid, eliminated, Collections.singleton(value));
        getCellsThatCanBePenciledIn().stream()
            .map(grid::cellAt)
            .forEach(cell -> cell.setValue(value));
    }
    
    
    
    /**
     * The colors we use for the coloring. The values don't matter, we just need two of them.
     */
    public static enum Color {
        BLUE, ORANGE;
        
        /**
         * Alternates colors, ORANGE -> BLUE -> ORANGE -> BLUE -> ...
         */
        Color next() {
            return (this == BLUE)
                    ? ORANGE
                    : BLUE;
        }
    }

    /**
     * Provides information about the House that became too crowded if the Simple Coloring
     * chain resulted in a Too Crowded House.
     */
    public static class TooCrowdedHouse {
        private final House house;
        private final Color color;

        public TooCrowdedHouse(House house, Color color) {
            this.house = requireNonNull(house);
            this.color = requireNonNull(color);
        }

        /**
         * The House that became too crowded.
         */
        public House getHouse() {
            return house;
        }

        /**
         * The color of the cells that crowded the House. The result of the Simple Coloring
         * technique is that the digit can be eliminated from all cells of this color, and
         * can be entered as a value into all cells of the other color.
         */
        public Color getColor() {
            return color;
        }
    }
    
    
    static Builder builder(Grid grid, Value value) {
        return new Builder(grid, value);
    }
    
    static class Builder {
        private final Grid grid;
        private final Value value;
        private final EnumMap<Color, ImmutableSet<Position>> coloredCells = new EnumMap<>(Color.class);
        
        public Builder(Grid grid, Value value) {
            this.grid = grid;
            this.value = value;
        }
        
        public Builder blueCells(Collection<Position> blueCells) {
            this.coloredCells.put(Color.BLUE, ImmutableSet.copyOf(blueCells));
            return this;
        }
        
        public Builder orangeCells(Collection<Position> orangeCells) {
            this.coloredCells.put(Color.ORANGE, ImmutableSet.copyOf(orangeCells));
            return this;
        }
        
        public SimpleColoring tooCrowdedHouse(TooCrowdedHouse houseTooCrowded) {
            checkState(coloredCells.size() == 2);
            ImmutableSet<Position> eliminate = coloredCells.get(houseTooCrowded.color);
            return new SimpleColoring(grid, value, coloredCells, houseTooCrowded, eliminate);
        }
        
        public SimpleColoring seesBothColors(Set<Position> targets) {
            checkState(coloredCells.size() == 2);
            return new SimpleColoring(grid, value, coloredCells, null, ImmutableSet.copyOf(targets));
        }
    }
    
    public static Optional<SimpleColoring> findNext(Grid grid) {
        Detector detector = new Detector(grid);
        return detector.find();
    }
    

    private static class Detector {
        private final Grid grid;
        private final Multimap<Value, ConjugatePair> allPairs = HashMultimap.create();
        
        public Detector(Grid grid) {
            this.grid = grid;
        }
        
        public Optional<SimpleColoring> find() {
            if (!grid.allCellsHaveValueOrCandidates(Position.all())) {
                // This technique is not safe to run in its current form unless there
                // are candidates in all cells of the grid.
                return Optional.empty();
            }
            House.ALL.stream().forEach(this::collectConjugatePairsInHouse);
            return allPairs.keySet().stream()
                    .map(this::searchForValue)
                    .filter(Objects::nonNull)
                    .findFirst();
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
                ColorCoder colorCoder = new ColorCoder(grid, value, p, positions);
                SimpleColoring hint = colorCoder.run();
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
     * Traverses the graph of Conjugate Pair positions, coloring in each position that is
     * visited with alternate colors.
     */
    private static class ColorCoder {
        private final Grid grid;
        private final Value value;
        private final Position startPosition;
        private final ImmutableMultimap<Position, ConjugatePair> positions;
        private final Map<Position, Color> cellToColor = new HashMap<>();
        private final Multimap<Color, Position> colorToCells = HashMultimap.create();
        
        public ColorCoder(Grid grid, Value value, Position startPosition, ImmutableMultimap<Position, ConjugatePair> positions) {
            this.grid = grid;
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
        public SimpleColoring run() {
            visit(startPosition, Color.BLUE);
            SimpleColoring result = lookForColorAppearingTwiceInHouse();
            if (result == null) {
                result = lookForCellsSeeingOppositeColors();
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
        private SimpleColoring lookForColorAppearingTwiceInHouse() {
            return Stream.of(Color.values())
                .map(this::lookForTooCrowdedHouse)
                .flatMap(Optional::stream)
                .map(this::tooCrowdedHouse)
                .findFirst()
                .orElse(null);
        }

        private Optional<TooCrowdedHouse> lookForTooCrowdedHouse(Color color) {
            Set<House> houses = new HashSet<>();
            return colorToCells.get(color).stream()
                    .flatMap(Position::memberOf)
                    // houses.add() returns false if we add the same House a second time.
                    .filter(Predicate.not(houses::add))
                    .map(house -> new TooCrowdedHouse(house, color))
                    .findFirst();
        }
        
        private SimpleColoring tooCrowdedHouse(TooCrowdedHouse houseTooCrowded) {
            return builder(grid, value)
                    .blueCells(colorToCells.get(Color.BLUE))
                    .orangeCells(colorToCells.get(Color.ORANGE))
                    .tooCrowdedHouse(houseTooCrowded);
        }
        
        @Nullable
        private SimpleColoring lookForCellsSeeingOppositeColors() {
            ImmutableSet<Position> targets = Position.all()
                    .filter(HintUtils.isCandidate(grid, value))
                    .filter(Predicate.not(cellToColor::containsKey)) // We are only interested in positions that are not part of a conjugate pair
                    .filter(seesColor(Color.BLUE).and(seesColor(Color.ORANGE)))
                    .collect(toImmutableSet());
            return targets.isEmpty()
                    ? null
                    : builder(grid, value)
                        .blueCells(colorToCells.get(Color.BLUE))
                        .orangeCells(colorToCells.get(Color.ORANGE))
                        .seesBothColors(targets);
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
