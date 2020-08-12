package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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

public class Swordfish extends EliminatingHint {

    private final ImmutableList<House> houses;
    
    public Swordfish(Grid grid, Set<Position> forcingPositions, Value value, List<House> houses, Set<Position> targets) {
        super(SolvingTechnique.SWORDFISH, grid, forcingPositions, value, targets);
        checkArgument(houses.size() == 3); // TODO: Also check they are all either ROWs or COLUMNs.
        this.houses = ImmutableList.copyOf(houses);
    }

    public House.Type getHouseType() {
        return houses.get(0).getType();
    }
    
    public ImmutableList<House> getHouses() {
        return houses;
    }

    public Value getValue() {
        return getValues().iterator().next();
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
                .filter(house -> HintUtils.allCellsHaveCandidates(grid, house))
                .filter(house -> hasTriple(value, house))
                .collect(toList());
            if (houses.size() == 3) {
                valuesWithTwoOrThreeCandidatesInThreeHouses.putAll(value, houses);
            }
        }

        private boolean hasTriple(Value value, House house) {
            ImmutableSet<Position> candidates = HintUtils.collectCandidates(grid, value, house);
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
            return valuesWithTwoOrThreeCandidatesInThreeHouses.keySet().stream()
                .map(this::examineValue)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
        }
        
        // TODO: Clean me up!
        @Nullable
        private Swordfish examineValue(Value value) {
            List<House> houses = ImmutableList.copyOf(valuesWithTwoOrThreeCandidatesInThreeHouses.get(value));
            assert houses.size() == 3;
            // Create the Triples from the first house and see if any of them line up with
            // candidates in the other two houses. If we find a match, see if there are any
            // target cells from which we can eliminate the value.
            for (Triple firstHouseTriple : getTriplesFromHouse(houses.get(0), value)) {
                Triple secondHouseTriple = findMatchingTripleInHouse(value, firstHouseTriple, houses.get(1));
                if (secondHouseTriple == null) {
                    continue;
                }
                Triple thirdHouseTriple = findMatchingTripleInHouse(value, firstHouseTriple, houses.get(2));
                if (thirdHouseTriple == null) {
                    continue;
                }
                // We have three matching Triples in three houses. Now look at the cross Houses
                // at those positions, and see if we have any unfilled Cells with the value as a
                // candidate (ignoring the 9 cells we've matched up). Those are our target cells.
                Set<Position> matchedCells = new HashSet<>(firstHouseTriple.positions);
                matchedCells.addAll(secondHouseTriple.positions);
                matchedCells.addAll(thirdHouseTriple.positions);
                House.Type crossOrientation = (orientation == Type.ROW)
                        ? Type.COLUMN
                        : Type.ROW;
                Set<Position> targets = firstHouseTriple.getCrossCoordinates(orientation)
                        .mapToObj(crossOrientation::createHouse)
                        .flatMap(House::getPositions)
                        .filter(Predicate.not(matchedCells::contains))
                        .filter(HintUtils.isCandidate(grid, value))
                        .collect(toImmutableSet());
                if (!targets.isEmpty()) {
                    return new Swordfish(grid, matchedCells, value, houses, targets);
                }
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
            assert candidates.size() == 2;
            List<Position> cellsWithValue = house.getPositions()
                    .filter(p -> grid.cellAt(p).hasValue())
                    .collect(toList());
            assert !cellsWithValue.isEmpty();
            return cellsWithValue.stream()
                    .map(c -> new Triple(candidates, c))
                    .collect(toList());
        }
        
        @Nullable
        private Triple findMatchingTripleInHouse(Value value, Triple tripleFromFirstHouse, House otherHouse) {
            // The cells in the second house that have the value as candidate. We know there are
            // 2 or three of them.
            Set<Position> candidates = HintUtils.collectCandidates(grid, value, otherHouse);
            // The cells in the second house that match up with the triple from the first house
            List<Position> triple = tripleFromFirstHouse.getCrossCoordinates(orientation)
                    .mapToObj(n -> otherHouse.getPosition(n))
                    .collect(toList());
            // Does the second house triple fulfill the requirements to take part in the Swordfish?
            Predicate<Position> candidateCellOrFilledInCell = HintUtils.isCandidate(grid, value)
                    .or(p -> grid.cellAt(p).hasValue());
            boolean match = triple.stream().allMatch(candidateCellOrFilledInCell) &&
                    triple.containsAll(candidates);
            return match 
                    ? new Triple(triple) 
                    : null;

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
        
        public IntStream getCrossCoordinates(House.Type orientation) { // TODO: Better name?
            return positions.stream()
                    .mapToInt(orientation == Type.ROW
                            ? Position::getColumn
                            : Position::getRow);
        }
    }

}
