package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

public class KillerCage {

    // TODO: We might need a more sophisticated data structure in order
    // to efficiently figure out how to render the border.
    private final ImmutableSet<Position> positions;
    
    @Nullable
    private final Integer sum;
    
    public KillerCage(Set<Position> positions, int sum) {
        checkArgument(sum >= 3 && sum <= 45);
        this.positions = validatePositions(positions);
        this.sum = sum;
    }
    
    public KillerCage(Set<Position> positions) {
        this.positions = validatePositions(positions);
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
    
    public ImmutableSet<Position> getLeftBoundary() {
        throw new UnsupportedOperationException();
    }
    
    public ImmutableSet<Position> getRightBoundary() {
        throw new UnsupportedOperationException();
    }
    
    public ImmutableSet<Position> getUpperBoundary() {
        throw new UnsupportedOperationException();
    }
    
    public ImmutableSet<Position> getLowerBoundary() {
        throw new UnsupportedOperationException();
    }

}
