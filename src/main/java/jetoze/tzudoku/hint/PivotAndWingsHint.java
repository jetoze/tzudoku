package jetoze.tzudoku.hint;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Position;

/**
 * Hint that employs a "pivot" cell (AKA "hinge", "apex", or "middle"), and two "wing" cells. Examples are
 * XY wings and XYZ wings.
 */
public interface PivotAndWingsHint extends Hint {
    
    // TODO: Should this be an abstract class deriving from EliminatingHint? Then the logic for
    // deriving forcing positions from the hinge and wing cells can be placed here rather than in
    // respective implementation.
    
    /**
     * Returns the position of the pivot cell.
     */
    Position getPivot();
    
    /**
     * Returns the positions of the wings.
     */
    ImmutableSet<Position> getWings();

}
