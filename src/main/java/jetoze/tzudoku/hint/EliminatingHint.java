package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.*;
import static java.util.Objects.requireNonNull;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Hint that eliminates one or more candidate values from one or more grid positions.
 */
public abstract class EliminatingHint implements Hint {
    
    private final SolvingTechnique solvingTechnique;
    private final Grid grid;
    private final ImmutableSet<Value> values;
    private final ImmutableSet<Position> forcingPositions;
    private final ImmutableSet<Position> targetPositions;
    
    protected EliminatingHint(SolvingTechnique solvingTechnique, Grid grid, Set<Position> forcingPositions, Value value, Set<Position> targetPositions) {
        this(solvingTechnique, grid, forcingPositions, ImmutableSet.of(value), targetPositions);
    }
    
    protected EliminatingHint(SolvingTechnique solvingTechnique, Grid grid, Set<Position> forcingPositions, Set<Value> values, Set<Position> targetPositions) {
        this.solvingTechnique = requireNonNull(solvingTechnique);
        this.grid = requireNonNull(grid);
        checkArgument(forcingPositions.size() >= 2, "Must always have at least two forcing positions (got %s)", forcingPositions.size());
        checkArgument(!values.isEmpty(), "Must provide at least one Value to eliminate");
        checkArgument(!targetPositions.isEmpty(), "Must provide at least one target position");
        this.forcingPositions = ImmutableSet.copyOf(forcingPositions);
        this.values = ImmutableSet.copyOf(values);
        this.targetPositions = ImmutableSet.copyOf(targetPositions);
    }
    
    @Override
    public final SolvingTechnique getTechnique() {
        return solvingTechnique;
    }

    /**
     * Returns the Grid this hint applies to.
     */
    public final Grid getGrid() {
        return grid;
    }

    /**
     * Returns the values that can be eliminated.
     * 
     * @return an ImmutableSet of one or more values
     */
    public final ImmutableSet<Value> getValues() {
        return values;
    }

    /**
     * Returns those positions in the grid that forces one or more values to be eliminated 
     * from other positions.
     * 
     * @return an ImmutableSet of two or more values
     */
    public final ImmutableSet<Position> getForcingPositions() {
        return forcingPositions;
    }

    /**
     * Returns those positions in the grid from which the candidate values can be eliminated.
     * 
     * @return an ImmutableSet of one or more positions
     */
    public final ImmutableSet<Position> getTargetPositions() {
        return targetPositions;
    }
    
    /**
     * Eliminates the values from the target positions.
     */
    @Override
    public final void apply() {
        // TODO: Move the HintUtils.eliminateCandidates implementation here
        // when we are done with this refactoring.
        HintUtils.eliminateCandidates(grid, targetPositions, values);
    }
    
    @Override
    public String toString() {
        return String.format("%s Forcing positions: %s Value(s): %s, Target position(s): %s",
                getClass().getSimpleName(), forcingPositions, values, targetPositions);
    }

}
