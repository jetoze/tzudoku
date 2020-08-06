package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class PointingPair implements Hint {

    private final Grid grid;
    private final Value value;
    private final ImmutableSet<Position> positions;
    private final ImmutableSet<Position> targets;
    
    public PointingPair(Grid grid, Value value, Set<Position> positions, Set<Position> targets) {
        this.grid = requireNonNull(grid);
        this.value = requireNonNull(value);
        checkArgument(positions.size() >= 2);
        House.Type houseType = getHouseType(positions);
        // Store the positions ordered by row or column depending on how they line up.
        // Getting the comparator also verifies that the positions are indeed in a line.
        Comparator<Position> order = getSortOrder(houseType);
        this.positions = positions.stream()
                .sorted(order)
                .collect(toImmutableSet());
        this.targets = ImmutableSet.copyOf(targets);
    }
    
    private static House.Type getHouseType(Set<Position> positions) {
        checkArgument(House.ifInBox(positions).isPresent(), "Not contained to a box: %s", positions);
        House house = House.ifInRowOrColumn(positions).orElseThrow(() -> 
            new IllegalArgumentException("Not contained to a single row or column"));
        return house.getType();
    }
    
    private static Comparator<Position> getSortOrder(House.Type houseType) {
        switch (houseType) {
        case ROW:
            return Comparator.comparing(Position::getColumn);
        case COLUMN:
            return Comparator.comparing(Position::getRow);
        default:
            throw new RuntimeException("Unsupported House Type: " + houseType);
        }
    }
    
    @Override
    public SolvingTechnique getTechnique() {
        return SolvingTechnique.POINTING_PAIR;
    }

    /**
     * Returns the value identified by this pointing pair, i.e. the value that can be
     * eliminated from the target cells.
     */
    public Value getValue() {
        return value;
    }

    /**
     * Returns the positions that make up the pointing pair.
     * 
     * @return an ImmutableSet containing two or three positions.
     */
    public ImmutableSet<Position> getPositions() {
        return positions;
    }

    /**
     * Returns the targets that are affected by the pointing pair, i.e. the
     * positions in the grid of those cells from which the value can be eliminated.
     * 
     * @return an ImmutableSet of one or more positions.
     */
    public ImmutableSet<Position> getTargets() {
        return targets;
    }
    
    /**
     * Removes the value of the pointing pair as a candidate from all cells seen by
     * the pointing pair.
     */
    @Override
    public void apply() {
        HintUtils.eliminateCandidates(grid, targets, Collections.singleton(value));
    }

    public String toString() {
        return String.format("Positions: %s (Digit: %s)", positions, value);
    }
    
    public static Optional<PointingPair> findNext(Grid grid) {
        requireNonNull(grid);
        return IntStream.rangeClosed(1, 9)
                .mapToObj(House.Type.BOX::createHouse)
                .map(house -> new Detector(grid, house))
                .map(Detector::find)
                .filter(Objects::nonNull)
                .findAny();
    }

    
    private static class Detector {
        private final Grid grid;
        private final House box;
        
        public Detector(Grid grid, House box) {
            this.grid = grid;
            this.box = box;
        }
        
        @Nullable
        public PointingPair find() {
            EnumSet<Value> remainingValues = box.getRemainingValues(grid);
            if (remainingValues.size() < 2) {
                // We need at least two positions to form a pointing pair.
                return null;
            }
            return remainingValues.stream()
                    .map(this::examine)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null);
        }
        
        @Nullable
        private PointingPair examine(Value value) {
            ImmutableSet<Position> candidates = HintUtils.collectCandidates(grid, value, box);
            // Are the candidates in the same row or column? If so, collect candidate cells
            // from the same row or column, outside of this box.
            return House.ifInRowOrColumn(candidates)
                    .map(house -> house.getPositions().filter(Predicate.not(this.box::contains)))
                    .map(positions -> HintUtils.collectCandidates(grid, value, positions))
                    .filter(Predicate.not(Set::isEmpty))
                    .map(targets -> new PointingPair(grid, value, candidates, targets))
                    .orElse(null);
        }
    }
}
