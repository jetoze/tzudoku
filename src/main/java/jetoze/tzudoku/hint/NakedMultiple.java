package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.PencilMarks;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class NakedMultiple implements Hint {

    private final Grid grid;
    private final ImmutableSet<Position> positions;
    private final ImmutableSet<Value> values;
    private final ImmutableSet<Position> targets;
    
    public NakedMultiple(Grid grid, Set<Position> positions, Set<Value> values, Set<Position> targets) {
        checkArgument(positions.size() >= 2);
        checkArgument(positions.size() == values.size());
        checkArgument(Sets.intersection(positions, targets).isEmpty());
        this.grid = requireNonNull(grid);
        this.positions = ImmutableSet.copyOf(positions);
        this.values = ImmutableSet.copyOf(values);
        this.targets = ImmutableSet.copyOf(targets);
    }

    @Override
    public SolvingTechnique getTechnique() {
        switch (values.size()) {
        case 2:
            return SolvingTechnique.NAKED_PAIR;
        case 3:
            return SolvingTechnique.NAKED_TRIPLE;
        case 4:
            return SolvingTechnique.NAKED_QUADRUPLE;
        default:
            throw new RuntimeException("We need a SolvingTechnique for " + values.size() + " number of values");
        }
    }

    public ImmutableSet<Position> getPositions() {
        return positions;
    }

    public ImmutableSet<Value> getValues() {
        return values;
    }
    
    public ImmutableSet<Position> getTargets() {
        return targets;
    }

    /**
     * Removes the common values as candidates from the other positions in the same house.
     */
    @Override
    public void apply() {
        HintUtils.eliminateCandidates(grid, targets, values);
    }

    public static Optional<NakedMultiple> findNakedPair(Grid grid) {
        return findNext(grid, 2);
    }
    
    public static Optional<NakedMultiple> findNakedTriple(Grid grid) {
        return findNext(grid, 3);
    }
    
    public static Optional<NakedMultiple> findNakedQuadruple(Grid grid) {
        return findNext(grid, 4);
    }

    public static Optional<NakedMultiple> findNext(Grid grid, int size) {
        requireNonNull(grid);
        checkArgument(size >= 2 && size <= 9);
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
        public NakedMultiple findNext() {
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
                    ImmutableSet<Position> targets = Sets.difference(emptyCellsWithPencilMarks, group).stream()
                            .filter(p -> {
                                Cell cell = grid.cellAt(p);
                                return !Sets.intersection(cell.getCenterMarks().getValues(), allCandidatesInGroup).isEmpty();
                            }).collect(toImmutableSet());
                    if (!targets.isEmpty()) {
                        return new NakedMultiple(grid, group, allCandidatesInGroup, targets);
                    }
                }
            }
            return null;
        }
    }
    
}
