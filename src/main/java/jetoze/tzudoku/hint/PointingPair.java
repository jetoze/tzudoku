package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
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
        checkArgument(isContainedInHouse(positions, Position::getBox));
        if (isContainedInHouse(positions, Position::getRow)) {
            return Type.ROW;
        } else if (isContainedInHouse(positions, Position::getColumn)) {
            return Type.COLUMN;
        } else {
            throw new IllegalArgumentException("Not contained to a single row or column");
        }
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
        return House.ALL.stream()
                // Since we are only interested in candidates in the same row or column, 
                // we do not have to look in Boxes.
                .filter(house -> house.getType() != House.Type.BOX)
                .map(house -> new Detector(grid, house))
                .map(Detector::findNext)
                .filter(Objects::nonNull)
                .findAny();
    }
    
    // TODO: Move this to the Position class, as a static utility mehtod? Perhaps take a House.Type
    // as input?
    private static boolean isContainedInHouse(Collection<Position> candidates, ToIntFunction<Position> f) {
        return (candidates.stream()
                .mapToInt(f)
                .distinct()
                .count() == 1L);
    }

    
    private static class Detector {
        private final Grid grid;
        private final House house;
        
        public Detector(Grid grid, House house) {
            this.grid = grid;
            this.house = house;
        }
        
        @Nullable
        public PointingPair findNext() {
            EnumSet<Value> remainingValues = house.getRemainingValues(grid);
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
            // Find the candidates for the given value in the House.
            ImmutableSet<Position> candidates = HintUtils.collectCandidates(grid, value, house);
            // Are the candidates in the same line in the same box?
            if (isLine(candidates) && isContainedInHouse(candidates, Position::getBox)) {
                int boxNumber = candidates.iterator().next().getBox();
                // We can now rule out value as a candidate from all the cells 
                // in this row/column that are in a different box, as well as 
                // the other cells in this box. Are there any such candidates?
                Stream<Position> otherBoxes = house.getPositions()
                        .filter(p -> p.getBox() != boxNumber);
                Stream<Position> sameBox = new House(House.Type.BOX, boxNumber).getPositions()
                        .filter(Predicate.not(candidates::contains));
                
                ImmutableSet<Position> targets = HintUtils.collectCandidates(grid, value, Stream.concat(otherBoxes, sameBox));
                if (!targets.isEmpty()) {
                    return new PointingPair(grid, value, candidates, targets);
                }
            }
            return null;
        }
        
        private boolean isLine(ImmutableSet<Position> candidates) {
            if (candidates.size() < 2) {
                // Need at least two candidates to form a line.
                return false;
            }
            return isContainedInHouse(candidates, Position::getRow) ||
                    isContainedInHouse(candidates, Position::getColumn);
        }
    }
}
