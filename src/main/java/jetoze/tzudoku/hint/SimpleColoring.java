package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.*;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
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
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void apply() {
        HintUtils.eliminateCandidates(grid, targets, Collections.singleton(value));
    }

    public static Optional<SimpleColoring> findNext(Grid grid) {
        return House.ALL.stream()
            .map(house -> new Detector(grid, house))
            .map(Detector::find)
            .filter(Objects::nonNull)
            .findAny();
    }
    
    
    // This implementation is not complete. It is just a starting point.
    private static class Detector {
        private final Grid grid;
        private final House startingHouse;
        
        public Detector(Grid grid, House startingHouse) {
            this.grid = grid;
            this.startingHouse = startingHouse;
        }
        
        @Nullable
        public SimpleColoring find() {
            EnumSet<Value> remainingValues = startingHouse.getRemainingValues(grid);
            if (remainingValues.size() < 2) {
                return null;
            }
            ImmutableSet<ConjugatePair> conjugatePairsInHouse = findConjugatePairsInHouse();
            if (conjugatePairsInHouse.isEmpty()) {
                return null;
            }
            // TODO: Complete me.
            return null;
        }
        
        private ImmutableSet<ConjugatePair> findConjugatePairsInHouse() {
            EnumSet<Value> remainingValues = startingHouse.getRemainingValues(grid);
            if (remainingValues.size() < 2) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<ConjugatePair> builder = ImmutableSet.builder();
            for (Value value : remainingValues) {
                List<Position> positions = startingHouse.getPositions()
                        .filter(p -> {
                            Cell cell = grid.cellAt(p);
                            return !cell.hasValue() && cell.getCenterMarks().contains(value);
                        }).collect(toList());
                if (positions.size() == 2) {
                    builder.add(new ConjugatePair(positions.get(0), positions.get(1)));
                }
            }
            return builder.build();
        }
        
        
    }
    
    
    private static class ConjugatePair {
        public final Position first;
        public final Position second;
        
        public ConjugatePair(Position first, Position second) {
            this.first = first;
            this.second = second;
            checkArgument(first.getBox() == second.getBox());
        }
    }
    
}
