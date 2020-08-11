package jetoze.tzudoku.hint;

import static java.util.Objects.*;

import java.util.Iterator;
import java.util.Optional;

import static com.google.common.base.Preconditions.*;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class XyzWing extends EliminatingHint {

    private final Position center;
    private final ImmutableSet<Position> wings;
    
    public XyzWing(Grid grid, Position centerPosition, Set<Position> wingPositions, Value value, Set<Position> targetPositions) {
        super(SolvingTechnique.XYZ_WING, grid, collectForcingPositions(centerPosition, wingPositions), value, targetPositions);
        this.center = requireNonNull(centerPosition);
        this.wings = ImmutableSet.copyOf(wingPositions);
    }
    
    private static ImmutableSet<Position> collectForcingPositions(Position center, Set<Position> wingPositions) {
        requireNonNull(center);
        checkArgument(wingPositions.size() == 2);
        checkArgument(!wingPositions.contains(center));
        Iterator<Position> it = wingPositions.iterator();
        Position wing1 = it.next();
        Position wing2 = it.next();
        checkArgument(center.sees(wing1) && center.sees(wing2));
        return ImmutableSet.of(center, wing1, wing2);
    }
    
    public Position getCenter() {
        return center;
    }

    public ImmutableSet<Position> getWings() {
        return wings;
    }
    
    public Value getValue() {
        return getValues().iterator().next();
    }

    public static Optional<XyzWing> findNext(Grid grid) {
        requireNonNull(grid);
        return Optional.empty();
    }

}
