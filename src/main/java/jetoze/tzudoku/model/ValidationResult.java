package jetoze.tzudoku.model;

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

    public ImmutableSet<Position> getInvalidPositions() {
        return invalidPositions;
    }
    
}
