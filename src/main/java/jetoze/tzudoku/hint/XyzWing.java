package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class XyzWing extends EliminatingHint {

    private final Position center;
    private final ImmutableSet<Position> wings;
    
    // TODO: Should the constructor take a TriValueCell and two BiValuesCells as input?
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
        return new Detector(grid).find();
    }

    
    private static class Detector {
        private final Grid grid;
        
        public Detector(Grid grid) {
            this.grid = grid;
        }
        
        public Optional<XyzWing> find() {
            return collectPossibleCenterCells()
                    .map(this::check)
                    .filter(Objects::nonNull)
                    .findFirst();
        }
        
        private Stream<Center> collectPossibleCenterCells() {
            return Position.all()
                    .map(p -> Center.examine(grid, p))
                    .filter(Objects::nonNull);
        }
        
        @Nullable
        private XyzWing check(Center centerCell) {
            ImmutableList<BiValueCell> possibleWings = centerCell.collectPossibleWings(grid);
            if (possibleWings.size() < 2) {
                // We need two wing cells
                return null;
            }
            // XXX: This n^2 algorithm isn't satisfying, but the number of wing cells is bounded
            // so we are at least not going to blow up.
            for (int i = 0; i < possibleWings.size() - 1; ++i) {
                for (int j = i + 1; j < possibleWings.size(); ++j) {
                    BiValueCell wing1 = possibleWings.get(i);
                    BiValueCell wing2 = possibleWings.get(j);
                    Value sharedValue = wing1.getSingleSharedValue(wing2).orElse(null);
                    if (sharedValue != null) {
                        // We have found a center and two wings that fulfills the XYZ-wing requirements.
                        // Last thing to check is if there are any cells that sees all these three cells,
                        // and have the shared value we just found as a candidate.
                        ImmutableSet<Position> targets = matchingPositionsSeenByAllThreeCells(centerCell, wing1, wing2, sharedValue);
                        if (!targets.isEmpty()) {
                            return new XyzWing(grid, centerCell.position, ImmutableSet.of(wing1.getPosition(), wing2.getPosition()), 
                                    sharedValue, targets);
                        }
                    }
                }
            }
            return null;
        }
        
        private ImmutableSet<Position> matchingPositionsSeenByAllThreeCells(
                Center center, 
                BiValueCell wing1, 
                BiValueCell wing2, Value value) {
            return Position.all()
                    .filter(p -> !p.equals(center.position) && !p.equals(wing1.getPosition()) && !p.equals(wing2.getPosition()))
                    .filter(p -> p.sees(center.position) && p.sees(wing1.getPosition()) && p.sees(wing2.getPosition()))
                    .filter(HintUtils.isCandidate(grid, value))
                    .collect(toImmutableSet());
        }
    }
    
    
    private static class Center {
        private final Position position;
        private final ImmutableSet<Value> candidates;
        
        public Center(Position position, ImmutableSet<Value> candidates) {
            assert candidates.size() == 3;
            this.position = position;
            this.candidates = candidates;
        }
        
        @Nullable
        public static Center examine(Grid grid, Position position) {
            Cell cell = grid.cellAt(position);
            if (!cell.hasValue()) {
                ImmutableSet<Value> candidates = cell.getCenterMarks().getValues();
                if (candidates.size() == 3) {
                    return new Center(position, candidates);
                }
            }
            return null;
        }
        
        public ImmutableList<BiValueCell> collectPossibleWings(Grid grid) {
            return position.seenBy()
                    .map(p -> BiValueCell.examine(grid, p))
                    .flatMap(Optional::stream)
                    .filter(c -> this.candidates.containsAll(c.getCandidates()))
                    .collect(toImmutableList());
        }
    }
    
}
