package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class YWing extends EliminatingHint implements HingeAndWingsHint {
    
    private final Position hinge;
    private final ImmutableSet<Position> wings;

    private YWing(Grid grid, 
                  Position hinge, 
                  ImmutableSet<Position> wings,
                  Value valueThatCanBeEliminated,
                  ImmutableSet<Position> targets) {
        super(SolvingTechnique.Y_WING, grid, ImmutableSet.<Position>builder().add(hinge).addAll(wings).build(),
                valueThatCanBeEliminated, targets);
        this.hinge = requireNonNull(hinge);
        this.wings = requireNonNull(wings);
        checkArgument(!wings.contains(hinge));
        checkArgument(!targets.contains(hinge));
        checkArgument(Sets.intersection(wings, targets).isEmpty());
    }

    @Override
    public Position getHinge() {
        return hinge;
    }

    @Override
    public ImmutableSet<Position> getWings() {
        return wings;
    }

    /**
     * Returns the value that can be eliminated from the target cells.
     */
    public Value getValue() {
        return getValues().iterator().next();
    }

    /**
     * Looks for a Y-wing in the given grid.
     * 
     * @return an Optional containing a YWing, or an empty optional if there are
     *         no Y-wings in the grid.
     */
    public static Optional<YWing> findNext(Grid grid) {
        return new Detector(grid).findNext();
    }

    
    private static class Detector {
        private final Grid grid;
        
        public Detector(Grid grid) {
            this.grid = requireNonNull(grid);
        }
        
        public Optional<YWing> findNext() {
            // TODO: I need to be cleaned up a bit. And I should have unit tests!
            // Collect all remaining cells that have exactly two possible values, according
            // to their center pencil marks.
            ImmutableList<BiValueCell> twoValueCells = Position.all()
                    .map(this::lookAt)
                    .filter(Objects::nonNull)
                    .collect(toImmutableList());
            if (twoValueCells.size() < 3) {
                // A y-wing requires three cells.
                return Optional.empty();
            }
            for (BiValueCell pivot : twoValueCells) {
                // See if we can find two matching wings for this center cell
                ImmutableList<BiValueCell> possibleWings = getPossibleWings(pivot, twoValueCells);
                if (possibleWings.size() < 2) {
                    continue;
                }
                for (int i = 0; i < (possibleWings.size() - 1); ++i) {
                    BiValueCell w1 = possibleWings.get(i);
                    Set<Value> valuesNotShared = w1.getValuesNotShared(pivot);
                    assert valuesNotShared.size() == 1 : "We should not have reached this point unless the two BiValueCells shares exactly one Value";
                    Value wingValue = valuesNotShared.iterator().next();
                    Set<Value> otherWingValues = ImmutableSet.of(wingValue, pivot.getValuesNotShared(w1).iterator().next());
                    for (int j = i + 1; j < possibleWings.size(); ++j) {
                        BiValueCell w2 = possibleWings.get(j);
                        if (!isInSameRowOrColumn(pivot, w1, w2) && w2.getCandidates().equals(otherWingValues)) {
                            // Now check if w1 and w2 are both seen by any cells that have wingValue as
                            // a candidate. Exclude the wings themselves.
                            Set<Position> seenByBothWings = Sets.intersection(seenBy(w1), seenBy(w2));
                            ImmutableSet<Position> targets = seenByBothWings.stream()
                                    .filter(px -> !px.equals(w1.getPosition()) && !px.equals(w2.getPosition()))
                                    .filter(HintUtils.isCandidate(grid, wingValue))
                                    .collect(toImmutableSet());
                            if (!targets.isEmpty()) {
                                return Optional.of(new YWing(grid, pivot.getPosition(), ImmutableSet.of(w1.getPosition(), w2.getPosition()), wingValue, targets));
                            }
                        }
                    }
                }
            }
            return Optional.empty();
        }
        
        @Nullable
        private BiValueCell lookAt(Position p) {
            Cell cell = grid.cellAt(p);
            if (cell.hasValue()) {
                return null;
            }
            ImmutableSet<Value> possibleValues = cell.getCenterMarks().getValues();
            return possibleValues.size() == 2
                    ? new BiValueCell(p, possibleValues)
                    : null;
        }
        
        private ImmutableList<BiValueCell> getPossibleWings(BiValueCell pivot, ImmutableList<BiValueCell> allCandidates) {
            return allCandidates.stream()
                    .filter(c -> (c != pivot) && pivot.sees(c) && pivot.isSharingSingleValue(c))
                    .collect(toImmutableList());
            
        }
        
        private static boolean isInSameRowOrColumn(BiValueCell pivot, BiValueCell w1, BiValueCell w2) {
            return (pivot.getPosition().getRow() == w1.getPosition().getRow() && pivot.getPosition().getRow() == w2.getPosition().getRow()) ||
                    (pivot.getPosition().getColumn() == w1.getPosition().getColumn() && pivot.getPosition().getColumn() == w2.getPosition().getColumn());
        }
        
        private static ImmutableSet<Position> seenBy(BiValueCell cell) {
            return cell.getPosition().seenBy().collect(toImmutableSet());
        }
    }
        
}
