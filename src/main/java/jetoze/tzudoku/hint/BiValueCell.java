package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Represents a cell with exactly two candidate values.
 */
class BiValueCell {

    private final Position position;
    private final ImmutableSet<Value> candidates;
    
    public BiValueCell(Position position, Set<Value> candidates) {
        this.position = requireNonNull(position);
        checkArgument(candidates.size() == 2);
        this.candidates = ImmutableSet.copyOf(candidates);
    }
    
    /**
     * Checks if the cell at the given position in the grid has no value and exactly
     * two center mark values, and returns its BiValueCell representation if so.
     */
    public static Optional<BiValueCell> examine(Grid grid, Position p) {
        Cell cell = grid.cellAt(requireNonNull(p));
        if (!cell.hasValue()) {
            ImmutableSet<Value> candidates = cell.getCenterMarks().getValues();
            if (candidates.size() == 2) {
                return Optional.of(new BiValueCell(p, candidates));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the position of this cell in the grid.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns this cell's candidate values.
     * 
     * @return an ImmutableSet of exactly two elements.
     */
    public ImmutableSet<Value> getCandidates() {
        return candidates;
    }
    
    /**
     * Checks if this BiValueCell sees the other BiValueCell (i.e. if they share a house).
     */
    public boolean sees(BiValueCell other) {
        return this.position.sees(other.position);
    }
    
    /**
     * Returns the candidate values (if any) this BiValueCell shares with another BiValueCell.
     * 
     * @return an unmodifiable Set of zero, one, or two elements.
     */
    public Set<Value> getSharedValues(BiValueCell other) {
        return Sets.intersection(this.candidates, other.candidates);
    }
    
    /**
     * Returns the candidate values (if any) this BiValueCell has but the other BiValueCell has not.
     * 
     * @return an unmodifiable Set of zero, one, or two elements.
     */
    public Set<Value> getValuesNotShared(BiValueCell other) {
        return Sets.difference(this.candidates, other.candidates);
    }
    
    /**
     * Checks if this BiValueCell shares exactly one candidate value with another BiValueCell.
     * 
     * @see {@link #getSingleSharedValue(BiValueCell)}
     */
    public boolean isSharingSingleValue(BiValueCell other) {
        Set<Value> intersection = Sets.intersection(this.candidates, other.candidates);
        return intersection.size() == 1;
    }

    /**
     * If this BiValueCell shares exactly one candidate value with the other BiValueCell,
     * that candidate value is returned.
     * 
     * @see {@link #isSharingSingleValue(BiValueCell)}
     */
    public Optional<Value> getSingleSharedValue(BiValueCell other) {
        Set<Value> intersection = Sets.intersection(this.candidates, other.candidates);
        return intersection.size() == 1
                ? Optional.of(intersection.iterator().next())
                : Optional.empty();
    }
}
