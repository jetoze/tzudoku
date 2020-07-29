package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class XyWing {
    private final Position center;
    private final ImmutableSet<Position> wings;
    private final Value valueThatCanBeEliminated;
    private final ImmutableSet<Position> targets;

    private XyWing(Position center, 
                   ImmutableSet<Position> wings,
                   Value valueThatCanBeEliminated,
                   ImmutableSet<Position> targets) {
        this.center = requireNonNull(center);
        this.wings = requireNonNull(wings);
        checkArgument(!wings.contains(center));
        this.valueThatCanBeEliminated = requireNonNull(valueThatCanBeEliminated);
        this.targets = requireNonNull(targets);
        checkArgument(!targets.contains(center));
        checkArgument(Sets.intersection(wings, targets).isEmpty());
    }
    
    public Position getCenter() {
        return center;
    }
    
    public ImmutableSet<Position> getWings() {
        return wings;
    }

    public Value getValueThatCanBeEliminated() {
        return valueThatCanBeEliminated;
    }
    
    public ImmutableSet<Position> getTargets() {
        return targets;
    }

    public static Optional<XyWing> findNext(Grid grid) {
        return new Detector(grid).findNext();
    }

    
    private static class Detector {
        private final Grid grid;
        
        public Detector(Grid grid) {
            this.grid = requireNonNull(grid);
        }
        
        public Optional<XyWing> findNext() {
            // TODO: I need to be cleaned up a bit. And I should have unit tests!
            // Collect all remaining cells that have exactly two possible values, according
            // to their center pencil marks.
            ImmutableList<TwoValueCell> twoValueCells = Position.all()
                    .map(this::lookAt)
                    .filter(Objects::nonNull)
                    .collect(toImmutableList());
            if (twoValueCells.size() < 3) {
                // An xy-wing requires three cells.
                return Optional.empty();
            }
            for (TwoValueCell center : twoValueCells) {
                // See if we can find two matching wings for this center cell
                ImmutableList<TwoValueCell> possibleWings = getPossibleWings(center, twoValueCells);
                if (possibleWings.size() < 2) {
                    continue;
                }
                for (int i = 0; i < (possibleWings.size() - 1); ++i) {
                    TwoValueCell w1 = possibleWings.get(i);
                    Value sharedValue = center.getSharedValue(w1);
                    Value wingValue = Sets.difference(w1.values, Collections.singleton(sharedValue)).iterator().next();
                    Set<Value> otherWingValues = new HashSet<>();
                    otherWingValues.add(wingValue);
                    otherWingValues.add(center.getValueNotSharedWith(w1));
                    for (int j = i + 1; j < possibleWings.size(); ++j) {
                        TwoValueCell w2 = possibleWings.get(j);
                        if (!isInSameRowOrColumn(center, w1, w2) && w2.values.equals(otherWingValues)) {
                            // Now check if w1 and w2 are both seen by any cells that have wingValue as
                            // a candidate
                            Set<Position> seenByBothWings = Sets.intersection(w1.seenBy(), w2.seenBy());
                            ImmutableSet<Position> targets = seenByBothWings.stream()
                                    .filter(px -> {
                                        Cell cell = grid.cellAt(px);
                                        if (cell.hasValue()) {
                                            return false;
                                        }
                                        return cell.getCenterMarks().contains(wingValue);
                                    }).collect(toImmutableSet());
                            if (!targets.isEmpty()) {
                                return Optional.of(new XyWing(center.position, ImmutableSet.of(w1.position, w2.position), wingValue, targets));
                            }
                        }
                    }
                }
            }
            return Optional.empty();
        }
        
        @Nullable
        private TwoValueCell lookAt(Position p) {
            Cell cell = grid.cellAt(p);
            if (cell.hasValue()) {
                return null;
            }
            ImmutableSet<Value> possibleValues = cell.getCenterMarks().getValues();
            return possibleValues.size() == 2
                    ? new TwoValueCell(p, possibleValues)
                    : null;
        }
        
        private ImmutableList<TwoValueCell> getPossibleWings(TwoValueCell center, ImmutableList<TwoValueCell> allCandidates) {
            return allCandidates.stream()
                    .filter(c -> c != center)
                    .filter(c -> (c != center) && center.sees(c) && center.sharesExactlyOneValueWith(c))
                    .collect(toImmutableList());
            
        }
        
        private static boolean isInSameRowOrColumn(TwoValueCell center, TwoValueCell w1, TwoValueCell w2) {
            return (center.position.getRow() == w1.position.getRow() && center.position.getRow() == w2.position.getRow()) ||
                    (center.position.getColumn() == w1.position.getColumn() && center.position.getColumn() == w2.position.getColumn());
        }
        
        
        /**
         * An XY-wing candidate cell, having two possible values.
         */
        private static class TwoValueCell {
            public final Position position;
            public final ImmutableSet<Value> values;
            
            public TwoValueCell(Position position, ImmutableSet<Value> values) {
                this.position = requireNonNull(position);
                this.values = requireNonNull(values);
                checkArgument(values.size() == 2);
            }
            
            public ImmutableSet<Position> seenBy() {
                return position.seenBy().collect(toImmutableSet());
            }
            
            public boolean sees(TwoValueCell other) {
                return this.position.sees(other.position);
            }
            
            public boolean sharesExactlyOneValueWith(TwoValueCell other) {
                return Sets.intersection(this.values, other.values).size() == 1;
            }
            
            public Value getSharedValue(TwoValueCell other) {
                Set<Value> shared = Sets.intersection(this.values, other.values);
                checkArgument(shared.size() == 1);
                return shared.iterator().next();
            }
            
            public Value getValueNotSharedWith(TwoValueCell other) {
                Set<Value> difference = Sets.difference(this.values, other.values);
                checkArgument(difference.size() == 1);
                return difference.iterator().next();
            }
        }
    }
    
}
