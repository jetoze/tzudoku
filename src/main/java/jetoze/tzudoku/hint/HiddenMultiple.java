package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class HiddenMultiple implements Hint {

    private final Grid grid;
    private final ImmutableSet<Value> hiddenValues;
    private final ImmutableMultimap<Position, Value> valuesToEliminate;
    
    public HiddenMultiple(Grid grid, Set<Value> hiddenValues, Multimap<Position, Value> valuesToEliminate) {
        this.grid = requireNonNull(grid);
        checkArgument(hiddenValues.size() > 2);
        checkArgument(!valuesToEliminate.isEmpty());
        checkArgument(Sets.intersection(hiddenValues, new HashSet<>(valuesToEliminate.values())).isEmpty());
        this.hiddenValues = ImmutableSet.copyOf(hiddenValues);
        this.valuesToEliminate = ImmutableMultimap.copyOf(valuesToEliminate);
    }

    @Override
    public SolvingTechnique getTechnique() {
        switch (hiddenValues.size()) {
        case 2:
            return SolvingTechnique.HIDDEN_PAIR;
        case 3:
            return SolvingTechnique.HIDDEN_TRIPLE;
        case 4:
            return SolvingTechnique.HIDDEN_QUADRUPLE;
        default:
            throw new RuntimeException("Unexpected size: " + hiddenValues.size());
        }
    }
    
    public ImmutableSet<Value> getHiddenValues() {
        return hiddenValues;
    }

    public ImmutableSet<Position> getTargets() {
        return valuesToEliminate.keySet();
    }

    ImmutableMultimap<Position, Value> getValuesToEliminate() {
        return valuesToEliminate;
    }
    
    public ImmutableCollection<Value> getValuesToEliminate(Position target) {
        return valuesToEliminate.get(target);
    }

    @Override
    public void apply() {
        for (Position target : valuesToEliminate.keySet()) {
            HintUtils.eliminateCandidates(grid, Collections.singleton(target), valuesToEliminate.get(target));
        }
    }

    public static Optional<HiddenMultiple> findHiddenPair(Grid grid) {
        return find(grid, 2);
    }

    public static Optional<HiddenMultiple> findHiddenTriple(Grid grid) {
        return find(grid, 3);
    }

    public static Optional<HiddenMultiple> findHiddenQuadruple(Grid grid) {
        return find(grid, 4);
    }

    private static Optional<HiddenMultiple> find(Grid grid, int size) {
        requireNonNull(grid);
        return House.ALL.stream()
                .map(house -> new Detector(grid, house, size))
                .map(Detector::find)
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
        public HiddenMultiple find() {
            // FIXME: Some of this is common with the Naked Multiple detection.
            EnumSet<Value> remainingValuesInHouse = house.getRemainingValues(grid);
            if (remainingValuesInHouse.size() <= size) {
                // Not enough remaining candidates for the multiple to hide in
                return null;
            }
            ImmutableSet<Position> emptyCellsWithPencilMarks = house.getMatchingPositions(grid, cell -> {
                return !cell.hasValue() && !cell.getCenterMarks().isEmpty();
            });
            if (emptyCellsWithPencilMarks.size() <= size) {
                // Not enough cells with center marks for the multiple to hide in.
                return null;
            }
            for (Set<Value> hiddenValues : Sets.combinations(remainingValuesInHouse, size)) {
                Set<Position> candidateCells = emptyCellsWithPencilMarks.stream()
                        .filter(p -> {
                            Cell cell = grid.cellAt(p);
                            return !Sets.intersection(hiddenValues, cell.getCenterMarks().getValues()).isEmpty();
                        }).collect(toSet());
                if (candidateCells.size() == size) {
                    // The set of values are all confined to a set of cells with the same size.
                    // Now check if the multiples are hidden or naked.
                    Set<Value> allCandidates = candidateCells.stream()
                            .map(grid::cellAt)
                            .map(Cell::getCenterMarks)
                            .flatMap(pm -> pm.getValues().stream())
                            .collect(toSet());
                    if (allCandidates.equals(hiddenValues)) {
                        // This is a naked multiple. Nothing to see here.
                        continue;
                    }
                    // We have a naked multiple. Now figure out what values can be eliminated from each cell.
                    ImmutableMultimap.Builder<Position, Value> valuesToEliminate = ImmutableMultimap.builder();
                    for (Position target : candidateCells) {
                        valuesToEliminate.putAll(target, 
                                Sets.difference(grid.cellAt(target).getCenterMarks().getValues(), hiddenValues));
                            
                    }
                    return new HiddenMultiple(grid, hiddenValues, valuesToEliminate.build());
                }
            }
            return null;
        }
    }
    
}
