package jetoze.tzudoku.model;

import static java.util.Objects.*;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class ValidationResult {

    private final ImmutableSet<Position> invalidPositions;
    
    public ValidationResult(Set<Position> invalidPositions) {
        this.invalidPositions = ImmutableSet.copyOf(invalidPositions);
    }
    
    public boolean isSolved() {
        return invalidPositions.isEmpty();
    }

    public boolean isInvalid(Position p) {
        requireNonNull(p);
        return invalidPositions.contains(p);
    }

    @Override
    public String toString() {
        return isSolved()
                ? "Solved"
                : "Invalid positions: " + invalidPositions;
    }
    
}
