package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;

public class KillerCage {

    private final ImmutableSet<Position> positions;
    /**
     * Maps the row number to a list of the killer cells in that row,
     * ordered by column.
     */
    private final ImmutableListMultimap<Integer, Position> byRowAndColumn;
    /**
     * Maps the column number to a list of the killer cells in that row,
     * ordered by row.
     */
    private final ImmutableListMultimap<Integer, Position> byColumnAndRow;
    
    @Nullable
    private final Integer sum;
    
    public KillerCage(Set<Position> positions, int sum) {
        checkArgument(sum >= 3 && sum <= 45);
        this.positions = validatePositions(positions);
        this.byRowAndColumn = toMultimap(positions, Position::getRow, Position::getColumn);
        this.byColumnAndRow = toMultimap(positions, Position::getColumn, Position::getRow);
        this.sum = sum;
    }
    
    public KillerCage(Set<Position> positions) {
        this.positions = validatePositions(positions);
        this.byRowAndColumn = toMultimap(positions, Position::getRow, Position::getColumn);
        this.byColumnAndRow = toMultimap(positions, Position::getColumn, Position::getRow);
        this.sum = null;
    }
    
    private static ImmutableSet<Position> validatePositions(Set<Position> positions) {
        checkArgument(positions.stream().allMatch(Objects::nonNull), 
                "The killer cage must not contain null positions (%s)", positions);
        checkArgument(positions.size() >= 2 && positions.size() <= 9,
                "The killer cage must contain 2-9 positions (had %s)", positions.size());
        // XXX: isValidShape repeats some of the checks we've already performed here.
        checkArgument(isValidShape(positions), "The killer cage cells must be orthogonally connected");
        return ImmutableSet.copyOf(positions);
    }
    
    private static ImmutableListMultimap<Integer, Position> toMultimap(Set<Position> positions,
                                                                       Function<Position, Integer> keyFunction,
                                                                       ToIntFunction<Position> valueOrder) {
        return positions.stream()
                .sorted(Comparator.comparingInt(valueOrder))
                .collect(toImmutableListMultimap(keyFunction, p -> p));
    }
    
    public static boolean isValidShape(Set<Position> positions) {
        checkArgument(positions.stream().allMatch(Objects::nonNull));
        if (positions.size() < 2 || positions.size() > 9) {
            return false;
        }
        Set<Position> unvisitedCells = new HashSet<>(positions);
        visit(unvisitedCells.iterator().next(), unvisitedCells);
        return unvisitedCells.isEmpty();
    }

    // TODO: Should this live with the Position class itself? Something like Position.areOrthogonallyConnected?
    private static void visit(Position position, Set<Position> unvisitedCells) {
        if (!unvisitedCells.contains(position)) {
            return;
        }
        unvisitedCells.remove(position);
        position.orthogonallyConnected().forEach(p -> visit(p, unvisitedCells));
    }
    
    public ImmutableSet<Position> getPositions() {
        return positions;
    }

    public boolean hasSum() {
        return sum != null;
    }
    
    public Optional<Integer> getSum() {
        return Optional.ofNullable(sum);
    }
    
    // TODO: Replace the getXXXBoundary() methods with a single getBoundary() that returns a 
    // suitable data structure? Something where a position is mapped to one or more of 
    // an enum LEFT, RIGHT, UPPER, LOWER?
    
    // FIXME: The boundary implementation is broken in a couple of ways:
    //        1. We do not find the correct upper and lower boundary for a C-shaped cage.
    //           Similarily for an U-shaped cage.
    //        2. Consider the case of a left boundary consisting of three cells. The border
    //           for the middle cell needs to run the entire height of the cell, where as the
    //           borders for the top and bottom cells need to honor the margin.
    
    public ImmutableSet<Position> getLeftBoundary() {
        // The left boundary is the first cell in each row of the cage.
        return getBoundary(byRowAndColumn, list -> list.get(0));
    }
    
    public ImmutableSet<Position> getRightBoundary() {
        // The right boundary is the last cell in each row of the cage.
        return getBoundary(byRowAndColumn, list -> list.get(list.size() - 1));
    }
    
    public ImmutableSet<Position> getUpperBoundary() {
        // The upper boundary is the first cell in each column of the cage.
        return getBoundary(byColumnAndRow, list -> list.get(0));
    }
    
    public ImmutableSet<Position> getLowerBoundary() {
        // The lower boundary is the last cell in each column of the cage.
        return getBoundary(byColumnAndRow, list -> list.get(list.size() - 1));
    }
    
    public Position getLocationOfSum() {
        return IntStream.rangeClosed(1, 9)
                .mapToObj(Integer::valueOf)
                .filter(byRowAndColumn::containsKey)
                .map(byRowAndColumn::get)
                .map(list -> list.get(0))
                .findFirst()
                .orElseThrow();
    }
    
    boolean intersects(KillerCage cage) {
        return intersects(cage.getPositions());
    }
    
    boolean intersects(ImmutableSet<Position> positions) {
        return positions.stream()
                .anyMatch(this.getPositions()::contains);
    }
    
    private static ImmutableSet<Position> getBoundary(ImmutableListMultimap<Integer, Position> positions,
                                                      Function<ImmutableList<Position>, Position> extractor) {
        return positions.keys().stream()
                .map(positions::get)
                .map(extractor)
                .collect(toImmutableSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sum, positions);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof KillerCage) {
            KillerCage that = (KillerCage) obj;
            return Objects.equals(this.sum, that.sum) && this.positions.equals(that.positions);
        }
        return false;
    }

    // TODO: Implement toString().
}
