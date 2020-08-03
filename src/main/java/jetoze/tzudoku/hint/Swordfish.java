package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class Swordfish implements Hint {

    private final Grid grid;
    private final Value value;
    private final ImmutableSet<Position> targets;
    
    public Swordfish(Grid grid, Value value, Set<Position> targets) {
        this.grid = requireNonNull(grid);
        this.value = requireNonNull(value);
        checkArgument(!targets.isEmpty());
        this.targets = ImmutableSet.copyOf(targets);
    }

    @Override
    public SolvingTechnique getTechnique() {
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void apply() {
        HintUtils.eliminateCandidates(grid, targets, Collections.singleton(value));
    }
    
    public static Optional<Swordfish> findNext(Grid grid) {
        return Stream.of(Type.ROW, Type.COLUMN)
                .map(o -> new Detector(grid, o))
                .map(Detector::find)
                .filter(Objects::nonNull)
                .findAny();
    }
    
    
    private static class Detector {
        
        private final Grid grid;
        private final House.Type orientation;
        private final Multimap<Value, House> valuesWithTwoOrThreeCandidatesInThreeHouses = HashMultimap.create();
        
        public Detector(Grid grid, Type orientation) {
            this.grid = grid;
            this.orientation = orientation;
            Value.ALL.forEach(this::collectPossibleValues);
        }
        
        private void collectPossibleValues(Value value) {
            List<House> houses = IntStream.rangeClosed(1, 9).mapToObj(orientation::createHouse)
                .filter(house -> hasTriple(value, house))
                .collect(toList());
            if (houses.size() == 3) {
                valuesWithTwoOrThreeCandidatesInThreeHouses.putAll(value, houses);
            }
        }

        private boolean hasTriple(Value value, House house) {
            ImmutableSet<Position> candidates = HintUtils.collectCandidates(grid, value, house.getPositions());
            if (candidates.size() == 3) {
                return true;
            }
            if (candidates.size() == 2) {
                // If the house has only two candidate cells, we can use cells with values in the
                // third position.
                return house.getPositions().map(grid::cellAt).anyMatch(Cell::hasValue);
            }
            return false;
        }
        
        @Nullable
        public Swordfish find() {
            
            return null;
        }
        
        @Nullable
        private Swordfish examineValue(Value value) {
            List<House> houses = ImmutableList.copyOf(valuesWithTwoOrThreeCandidatesInThreeHouses.get(value));
            assert houses.size() == 3;
            // Create the Triples from the first houses and see if any of them lines up with
            // candidates in the other two houses. If we find a match, see if there are any
            // target cells from which we can eliminate the value.
            for (Triple triple : getTriplesFromHouse(houses.get(0), value)) {
                // TODO: Complete me.
            }
            return null;
        }
        
        private List<Triple> getTriplesFromHouse(House house, Value value) {
            List<Position> candidates = house.getPositions()
                    .filter(HintUtils.isCandidate(grid, value))
                    .collect(toList());
            if (candidates.size() == 3) {
                return Collections.singletonList(new Triple(candidates));
            }
            // For a House with two candidates, create all possible Triples by adding
            // a filled-in Cell. We know that at least one such Cell exists, thanks to the
            // condition we applied when we collected Houses of interest -- see the 
            // hasTriple method.
            List<Position> cellsWithValue = house.getPositions()
                    .filter(p -> grid.cellAt(p).hasValue())
                    .collect(toList());
            assert !cellsWithValue.isEmpty();
            return cellsWithValue.stream()
                    .map(c -> new Triple(candidates, c))
                    .collect(toList());
        }
    }
    
    
    private static class Triple {
        private final ImmutableList<Position> positions;
        
        public Triple(List<Position> twoCandidates, Position cellWithValue) {
            checkArgument(twoCandidates.size() == 2);
            checkArgument(!twoCandidates.contains(cellWithValue));
            this.positions = ImmutableList.of(twoCandidates.get(0), twoCandidates.get(1), cellWithValue);
        }
        
        public Triple(List<Position> positions) {
            checkArgument(positions.size() == 3);
            this.positions = ImmutableList.copyOf(positions);
        }
    }

}
