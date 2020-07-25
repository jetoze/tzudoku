package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

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
            return Position.all()
                    .map(this::examine)
                    .filter(Objects::nonNull)
                    .findAny();
        }
        
        @Nullable
        private XyWing examine(Position p0) {
            // TODO: Clean me up! Also, I'm screaming for a unit test.
            Cell c0 = grid.cellAt(p0);
            if (c0.hasValue()) {
                return null;
            }
            ImmutableSet<Value> candidates0 = getCandidates(c0);
            if (candidates0.size() != 2) {
                return null;
            }
            // Collect all cells seen by c0 having exactly two candidates, exactly one of
            // which is shared by c0.
            List<Wing> wings = p0.seenBy().map(p -> {
                Cell cell = grid.cellAt(p);
                if (cell.hasValue()) {
                    return null;
                }
                ImmutableSet<Value> candidates = getCandidates(cell);
                return sharesExactlyOneElement(candidates0, candidates)
                        ? new Wing(p, candidates)
                        : null;
            }).filter(Objects::nonNull).collect(toList());
            if (wings.size() < 2) {
                return null;
            }
            for (int i = 0; i < (wings.size() - 1); ++i) {
                Wing w1 = wings.get(i);
                Value sharedValue = Sets.intersection(candidates0, w1.values).iterator().next();
                Value wingValue = Sets.difference(w1.values, Collections.singleton(sharedValue)).iterator().next();
                Set<Value> otherWingValues = new HashSet<>();
                otherWingValues.add(wingValue);
                otherWingValues.addAll(Sets.difference(candidates0, Collections.singleton(sharedValue)));
                for (int j = i + 1; j < wings.size(); ++j) {
                    Wing w2 = wings.get(j);
                    if (!isInSameRowOrColumn(p0, w1, w2) && w2.values.equals(otherWingValues)) {
                        // Now check if w1 and w2 are both seen by any cells that have wingValue as
                        // a candidate
                        Set<Position> seenByBothWings = Sets.intersection(w1.seenBy(), w2.seenBy());
                        ImmutableSet<Position> targets = seenByBothWings.stream()
                                .filter(px -> {
                                    Cell cell = grid.cellAt(px);
                                    if (cell.hasValue()) {
                                        return false;
                                    }
                                    Set<Value> candidates = getCandidates(cell);
                                    return candidates.contains(wingValue);
                                }).collect(toImmutableSet());
                        if (!targets.isEmpty()) {
                            return new XyWing(p0, ImmutableSet.of(w1.position, w2.position), wingValue, targets);
                        }
                    }
                }
            }
            return null;
        }
        
        private static ImmutableSet<Value> getCandidates(Cell c0) {
            return ImmutableSet.copyOf(c0.getPencilMarks().iterateOverCenterMarks());
        }
        
        private static boolean sharesExactlyOneElement(Set<Value> s1, Set<Value> s2) {
            return s1.size() == 2 && s2.size() == 2 && Sets.intersection(s1, s2).size() == 1;
        }
        
        private static boolean isInSameRowOrColumn(Position p0, Wing w1, Wing w2) {
            return (p0.getRow() == w1.position.getRow() && p0.getRow() == w2.position.getRow()) ||
                    (p0.getColumn() == w1.position.getColumn() && p0.getColumn() == w2.position.getColumn());
        }
        
        
        private static class Wing {
            public final Position position;
            public final ImmutableSet<Value> values;
            
            public Wing(Position position, ImmutableSet<Value> values) {
                this.position = requireNonNull(position);
                this.values = requireNonNull(values);
                checkArgument(values.size() == 2);
            }
            
            public ImmutableSet<Position> seenBy() {
                return position.seenBy().collect(toImmutableSet());
            }
        }
    }
    
}
