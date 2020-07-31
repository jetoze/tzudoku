package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Multiple implements Hint {

    private final Grid grid;
    private final House house;
    private final ImmutableSet<Position> positions;
    private final ImmutableSet<Value> values;
    
    public Multiple(Grid grid, House house, Set<Position> positions, Set<Value> values) {
        checkArgument(positions.size() > 2);
        checkArgument(positions.size() == values.size());
        this.grid = requireNonNull(grid);
        this.house = requireNonNull(house);
        this.positions = ImmutableSet.copyOf(positions);
        this.values = ImmutableSet.copyOf(values);
    }

    public ImmutableSet<Position> getPositions() {
        return positions;
    }

    public ImmutableSet<Value> getValues() {
        return values;
    }

    /**
     * Removes the common values as candidates from the other positions in the same house.
     */
    @Override
    public void apply() {
        house.getPositions().filter(Predicate.not(positions::contains))
            .map(grid::cellAt)
            .filter(Predicate.not(Cell::isGiven))
            .map(Cell::getCenterMarks)
            .forEach(m -> {
                values.forEach(m::remove);
            });
    }

    public static Optional<Multiple> findNextTriple(Grid grid) {
        return findNext(grid, 3);
    }

    public static Optional<Multiple> findNext(Grid grid, int size) {
        requireNonNull(grid);
        checkArgument(size > 2 && size <= 9);
        return House.ALL.stream()
                .map(house -> new Detector(grid, house, size))
                .map(Detector::findNext)
                .filter(Objects::nonNull)
                .findAny();
    }
    
    
    private static class Detector {
        private final Grid grid;
        private final House house;
        private final int size;

        public Detector(Grid grid, House house, int size) {
            this.grid = grid;
            this.house = house;
            this.size = size;
        }
        
        @Nullable
        public Multiple findNext() {
            EnumSet<Value> remainingValues = house.getRemainingValues(grid);
            if (remainingValues.size() <= size) {
                return null;
            }
            ImmutableSet<Position> emptyCellsWithPencilMarks = house.getMatchingPositions(grid, cell -> {
                return !cell.hasValue() && !cell.getCenterMarks().isEmpty();
            });
            if (emptyCellsWithPencilMarks.size() <= size) {
                return null;
            }
            // TODO: This brute force approach doesn't scale well with size. There must be
            // a more clever way of doing this.
            for (Set<Position> group : Sets.combinations(emptyCellsWithPencilMarks, size)) {
                Set<Value> allCandidatesInGroup = new HashSet<>();
                group.stream()
                    .map(grid::cellAt)
                    .map(Cell::getCenterMarks)
                    .map(PencilMarks::getValues)
                    .forEach(allCandidatesInGroup::addAll);
                if (allCandidatesInGroup.size() == size) {
                    // Check if any other cell in the house has a matching candidate value
                    // that can be eliminated.
                    boolean targetCellExists = Sets.difference(emptyCellsWithPencilMarks, group).stream()
                            .map(grid::cellAt)
                            .map(Cell::getCenterMarks)
                            .flatMap(pm -> pm.getValues().stream())
                            .anyMatch(allCandidatesInGroup::contains);
                    if (targetCellExists) {
                        return new Multiple(grid, house, group, allCandidatesInGroup);
                    }
                }
            }
            return null;
        }
    }
    
}
