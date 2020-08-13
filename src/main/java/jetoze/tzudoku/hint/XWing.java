package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class XWing extends EliminatingHint {

    public XWing(Grid grid, Set<Position> positions, Value value, Set<Position> targets) {
        super(SolvingTechnique.X_WING, grid, positions, value, targets);
        checkArgument(positions.size() == 4);
        checkArgument(Sets.intersection(positions, targets).isEmpty());
    }
    
    /**
     * Returns the value that can be eliminated.
     */
    public Value getValue() {
        return getValues().iterator().next();
    }
    
    public static Optional<XWing> analyze(Grid grid) {
        // Start by looking at the columns, then rows
        XWing xwing = new Detector(grid, Type.COLUMN).find();
        if (xwing == null) {
            xwing = new Detector(grid, Type.ROW).find();
        }
        return Optional.ofNullable(xwing);
    }
    
    
    private static class Detector {
        // TODO: Clean me up. I'm messy and too complicated to follow.
        private final Grid grid;
        private final Type houseType;
        private int houseNum;
        // FIXME: Terrible name. When we are processing columns, this is the function
        // that returns the row number of a given position. When we are processing rows,
        // this is the function that returns the column number of a given position.
        private final Function<Position, Integer> coordinateFunction;
        
        public Detector(Grid grid, Type houseType) {
            this.grid = grid;
            this.houseType = houseType;
            this.coordinateFunction = (houseType == Type.COLUMN)
                    ? Position::getRow
                    : Position::getColumn;
        }
        
        @Nullable
        public XWing find() {
            // This detection algorithm is safe to use even when not all cells in the
            // grid have candidates, as long as we only consider Houses that *do* have
            // candidates in all cells.
            for (houseNum = 1; houseNum < 9; ++houseNum) {
                House house = new House(houseType, houseNum);
                if (!HintUtils.allCellsHaveCandidates(grid, house)) {
                    continue;
                }
                EnumSet<Value> remainingValues = house.getRemainingValues(grid);
                if (remainingValues.size() < 2) {
                    continue;
                }
                for (Value value : remainingValues) {
                    Pair firstPair = getCandidatesInHouse(house, value);
                    if (firstPair == null) {
                        continue;
                    }
                    Pair secondPair = lookForMatchingPairInOtherHouse(value, firstPair);
                    if (secondPair == null) {
                        continue;
                    }
                    // We have a matching pair. Now, in order to qualify for an X-wing there
                    // must exist at least one target cell from which we can eliminate a value.
                    Set<Position> targets = lookForTargets(firstPair, secondPair, value);
                    if (!targets.isEmpty()) {
                        return new XWing(
                                grid, 
                                ImmutableSet.of(firstPair.first, firstPair.second, secondPair.first, secondPair.second),
                                value,
                                targets);
                    }
                    
                }
            }
            return null;
        }
        
        @Nullable
        private Pair getCandidatesInHouse(House house, Value value) {
            Set<Position> candidates = HintUtils.collectCandidates(grid, value, house);
            return (candidates.size() == 2)
                    ? new Pair(candidates)
                    : null;
        }
        
        @Nullable
        private Pair lookForMatchingPairInOtherHouse(Value value, Pair firstPair) {
            for (int nextHouseNum = houseNum + 1; nextHouseNum <= 9; ++nextHouseNum) {
                House nextHouse = new House(houseType, nextHouseNum);
                if (!HintUtils.allCellsHaveCandidates(grid, nextHouse)) {
                    continue;
                }
                Pair secondPair = getCandidatesInHouse(nextHouse, value);
                if (secondPair == null) {
                    continue;
                }
                // We have two candidate pairs. If we are processing columns, check if the two pairs
                // occupy the same rows. If we are processing rows, check if they occupy the same columns.
                if (coordinateFunction.apply(firstPair.first) == coordinateFunction.apply(secondPair.first)
                        && coordinateFunction.apply(firstPair.second) == coordinateFunction.apply(secondPair.second)) {
                    return secondPair;
                }
            }
            return null;
        }
        
        private Set<Position> lookForTargets(Pair firstPair, Pair secondPair, Value value) {
            // If we are processing columns we need to look across the two rows defined
            // by the matching pairs, and vice versa.
            Type typeOfHouseToSearchIn = (houseType == Type.ROW)
                    ? Type.COLUMN
                    : Type.ROW;
            int firstHouseNumberToSearchIn = coordinateFunction.apply(firstPair.first);
            int secondHouseNumberToSearchIn = coordinateFunction.apply(firstPair.second);
            Stream<Position> positionsInFirstHouse = new House(typeOfHouseToSearchIn, firstHouseNumberToSearchIn)
                    .getPositions().filter(p -> !p.equals(firstPair.first) && !p.equals(secondPair.first));
            Stream<Position> positionsInSecondHouse = new House(typeOfHouseToSearchIn, secondHouseNumberToSearchIn)
                    .getPositions().filter(p -> !p.equals(firstPair.second) && !p.equals(secondPair.second));
            return HintUtils.collectCandidates(grid, value, Stream.concat(positionsInFirstHouse, positionsInSecondHouse));
        }
        
        
        private static final class Pair {
            public final Position first;
            public final Position second;
            
            public Pair(Set<Position> position) {
                Iterator<Position> it = position.iterator();
                first = it.next();
                second = it.next();
                checkArgument(!it.hasNext());
            }
            
            @Override
            public String toString() {
                return String.format("%s and %s", first, second);
            }
        }
    }

}
