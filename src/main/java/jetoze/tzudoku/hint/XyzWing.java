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

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
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

    public static Optional<XyzWing> analyze(Grid grid) {
        requireNonNull(grid);
        return new Detector(grid).find();
    }

    
    // This algorithm is perfectly safe to run in a grid where not all cells
    // have candidates, since we are only looking at the interaction between known
    // Bi- and TriValueCells. There is no risk of producing a false negative.
    private static class Detector {
        private final Grid grid;
        
        public Detector(Grid grid) {
            this.grid = grid;
        }
        
        public Optional<XyzWing> find() {
            return collectPossiblePivots()
                    .map(this::check)
                    .filter(Objects::nonNull)
                    .findFirst();
        }
        
        private Stream<TriValueCell> collectPossiblePivots() {
            return Position.all()
                    .map(p -> TriValueCell.examine(grid, p))
                    .flatMap(Optional::stream);
        }
        
        @Nullable
        private XyzWing check(TriValueCell pivot) {
            ImmutableList<BiValueCell> possibleWings = collectPossibleWings(pivot);
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
                        // We have found a pivot and two wings that fulfills the XYZ-wing requirements.
                        if (areAllInSameHouse(pivot, wing1, wing2)) {
                            // This is in fact a Naked Triple, not an XYZ-wing.
                            return null;
                        }
                        // Last thing to check is if there are any cells that sees all these three cells,
                        // and have the shared value we just found as a candidate.
                        ImmutableSet<Position> targets = matchingPositionsSeenByAllThreeCells(
                                pivot, wing1, wing2, sharedValue);
                        if (!targets.isEmpty()) {
                            return new XyzWing(grid, pivot.getPosition(), 
                                    ImmutableSet.of(wing1.getPosition(), wing2.getPosition()), 
                                    sharedValue, targets);
                        }
                    }
                }
            }
            return null;
        }
        
        private ImmutableList<BiValueCell> collectPossibleWings(TriValueCell pivot) {
            return pivot.getPosition().seenBy()
                    .map(p -> BiValueCell.examine(grid, p))
                    .flatMap(Optional::stream)
                    .filter(c -> pivot.getCandidates().containsAll(c.getCandidates()))
                    .collect(toImmutableList());
        }
        
        private ImmutableSet<Position> matchingPositionsSeenByAllThreeCells(
                TriValueCell pivot, 
                BiValueCell wing1, 
                BiValueCell wing2, Value value) {
            return Position.seenByAll(pivot.getPosition(), wing1.getPosition(), wing2.getPosition())
                    .filter(HintUtils.isCandidate(grid, value))
                    .collect(toImmutableSet());
        }
    }
    
    private static boolean areAllInSameHouse(TriValueCell pivot, BiValueCell w1, BiValueCell w2) {
        return House.ifInSameHouse(ImmutableSet.of(pivot.getPosition(), w1.getPosition(), w2.getPosition()))
                .map(h -> true)
                .orElse(false);
    }

}
