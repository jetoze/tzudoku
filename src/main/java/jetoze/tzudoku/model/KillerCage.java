package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableTable.toImmutableTable;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;

public class KillerCage {

    // TODO: It is technically not necessary to store the positions separately
    // like this, since we also have them in a Table. This allows us to implement
    // getPositions() quickly and efficiently. Is that necessary?
    private final ImmutableSet<Position> positions;
    /**
     * Stores the positions of the cage by row and column. This is used for
     * calculating the boundary when rendering the cage in the UI.
     */
    private final ImmutableTable<Integer, Integer, Position> byRowAndColumn;
    
    @Nullable
    private final Integer sum;
    
    public KillerCage(Set<Position> positions, int sum) {
        checkArgument(sum >= 3 && sum <= 45);
        this.positions = validatePositions(positions);
        this.byRowAndColumn = positions.stream().collect(toImmutableTable(
                Position::getRow, Position::getColumn, p -> p));
        this.sum = sum;
    }
    
    public KillerCage(Set<Position> positions) {
        this.positions = validatePositions(positions);
        this.byRowAndColumn = positions.stream().collect(toImmutableTable(
                Position::getRow, Position::getColumn, p -> p));
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
    
    
    public boolean hasCellAbove(Position p) {
        return byRowAndColumn.contains(p.getRow() - 1, p.getColumn());
    }
    
    public boolean hasCellBelow(Position p) {
        return byRowAndColumn.contains(p.getRow() + 1, p.getColumn());
    }
    
    public boolean hasCellToTheLeft(Position p) {
        return byRowAndColumn.contains(p.getRow(), p.getColumn() - 1);
    }
    
    public boolean hasCellToTheRight(Position p) {
        return byRowAndColumn.contains(p.getRow(), p.getColumn() + 1);
    }
    
    public Position getLocationOfSum() {
        for (int row = 1; row <= 9; ++row) {
            for (int col = 1; col <= 9; ++col) {
                if (byRowAndColumn.contains(row, col)) {
                    return byRowAndColumn.get(row, col);
                }
            }
        }
        // We will never reach here.
        throw new NoSuchElementException();
    }
    
    boolean intersects(KillerCage cage) {
        return intersects(cage.getPositions());
    }
    
    boolean intersects(ImmutableSet<Position> positions) {
        return positions.stream()
                .anyMatch(this.getPositions()::contains);
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
