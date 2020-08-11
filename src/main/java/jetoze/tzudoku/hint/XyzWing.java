package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class XyzWing extends EliminatingHint implements PivotAndWingsHint {

    private final Position pivot;
    private final ImmutableSet<Position> wings;
    
    // TODO: Should the constructor take a TriValueCell and two BiValuesCells as input?
    public XyzWing(Grid grid, Position pivot, Set<Position> wings, Value value, Set<Position> targetPositions) {
        super(SolvingTechnique.XYZ_WING, grid, collectForcingPositions(pivot, wings), value, targetPositions);
        this.pivot = requireNonNull(pivot);
        this.wings = ImmutableSet.copyOf(wings);
    }
    
    private static ImmutableSet<Position> collectForcingPositions(Position pivot, Set<Position> wings) {
        requireNonNull(pivot);
        checkArgument(wings.size() == 2);
        checkArgument(!wings.contains(pivot));
        Iterator<Position> it = wings.iterator();
        Position wing1 = it.next();
        Position wing2 = it.next();
        checkArgument(pivot.sees(wing1) && pivot.sees(wing2));
        return ImmutableSet.of(pivot, wing1, wing2);
    }
    
    @Override
    public Position getPivot() {
        return pivot;
    }

    @Override
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
        
        private Stream<TriValueCell> collectPossibleCenterCells() {
            return Position.all()
                    .map(p -> TriValueCell.examine(grid, p))
                    .flatMap(Optional::stream);
        }
        
        @Nullable
        private XyzWing check(TriValueCell centerCell) {
            ImmutableList<BiValueCell> possibleWings = collectPossibleWings(centerCell);
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
                        ImmutableSet<Position> targets = matchingPositionsSeenByAllThreeCells(
                                centerCell, wing1, wing2, sharedValue);
                        if (!targets.isEmpty()) {
                            return new XyzWing(grid, centerCell.getPosition(), 
                                    ImmutableSet.of(wing1.getPosition(), wing2.getPosition()), 
                                    sharedValue, targets);
                        }
                    }
                }
            }
            return null;
        }
        
        private ImmutableList<BiValueCell> collectPossibleWings(TriValueCell centerCell) {
            return centerCell.getPosition().seenBy()
                    .map(p -> BiValueCell.examine(grid, p))
                    .flatMap(Optional::stream)
                    .filter(c -> centerCell.getCandidates().containsAll(c.getCandidates()))
                    .collect(toImmutableList());
        }
        
        private ImmutableSet<Position> matchingPositionsSeenByAllThreeCells(
                TriValueCell center, 
                BiValueCell wing1, 
                BiValueCell wing2, Value value) {
            return Position.all()
                    .filter(sees(center.getPosition()).and(
                            sees(wing1.getPosition()).and(
                            sees(wing2.getPosition()))))
                    .filter(HintUtils.isCandidate(grid, value))
                    .collect(toImmutableSet());
        }
        
        private Predicate<Position> sees(Position p) {
            return p::sees;
        }
    }
    
}
