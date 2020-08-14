package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class KillerCages {

    public static final KillerCages EMPTY = new KillerCages();
    
    private final ImmutableMap<ImmutableSet<Position>, KillerCage> cages;
    
    private KillerCages() {
        this.cages = ImmutableMap.of();
    }

    public KillerCages(Collection<KillerCage> cages) {
        checkNoIntersects(cages);
        this.cages = cages.stream().collect(toImmutableMap(KillerCage::getPositions, c -> c));
    }

    public KillerCages(Map<ImmutableSet<Position>, KillerCage> cages) {
        checkNoIntersects(cages.values());
        this.cages = ImmutableMap.copyOf(cages);
    }
    
    private static void checkNoIntersects(Collection<KillerCage> cages) {
        if (cages.isEmpty()) {
            return;
        }
        Iterator<KillerCage> it = cages.iterator();
        Set<Position> coveredPositions = new HashSet<>(it.next().getPositions());
        while (it.hasNext()) {
            Set<Position> cagePositions = it.next().getPositions();
            Set<Position> intersection = Sets.intersection(coveredPositions, cagePositions);
            checkArgument(intersection.isEmpty(), "The cages cannot intersect. They intersect in %s", intersection);
        }
    }
    
    public boolean isEmpty() {
        return cages.isEmpty();
    }

    public boolean contains(KillerCage cage) {
        return containsCageAt(cage.getPositions());
    }
    
    public boolean containsCageAt(ImmutableSet<Position> positions) {
        requireNonNull(positions);
        return cages.containsKey(positions);
    }
    
    public Optional<KillerCage> getCage(ImmutableSet<Position> positions) {
        return Optional.ofNullable(cages.get(requireNonNull(positions)));
    }
    
    public ImmutableCollection<KillerCage> getCages() {
        return cages.values();
    }
    
    /**
     * Checks if the given cage intersects with one or more of the cages
     * contained in this instance.
     */
    public boolean intersects(KillerCage cage) {
        return this.cages.values().stream()
                .anyMatch(c -> c.intersects(cage));
    }
    
    public boolean intersects(ImmutableSet<Position> positions) {
        return this.cages.values().stream()
                .anyMatch(c -> c.intersects(positions));
    }
    
    /**
     * Creates and returns a new KillerCages instance containing all the cages of
     * this instance plus the new cage that is added. This instance is not modified.
     */
    public KillerCages add(KillerCage cage) {
        checkArgument(!intersects(cage));
        return builder().addAll(getCages()).add(cage).build();
    }
    
    /**
     * Creates and returns a new KillerCages instance containing all the cages of
     * this instance minus the new cage that is removed. This instance is not modified.
     */
    public KillerCages remove(KillerCage cage) {
        checkArgument(containsCageAt(cage.getPositions()));
        if (cages.size() == 1) {
            return EMPTY;
        } else {
            Map<ImmutableSet<Position>, KillerCage> remaining = new HashMap<>(cages);
            remaining.remove(cage.getPositions());
            return new KillerCages(remaining);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    
    public static class Builder {
        private final List<KillerCage> cages = new ArrayList<KillerCage>();
        
        public Builder add(KillerCage cage) {
            requireNonNull(cage);
            checkArgument(!intersects(cage));
            this.cages.add(cage);
            return this;
        }
        
        public Builder addAll(Collection<KillerCage> cages) {
            checkArgument(cages.stream().allMatch(Objects::nonNull));
            if (!this.cages.isEmpty()) {
                checkArgument(cages.stream().noneMatch(this::intersects));
            }
            this.cages.addAll(cages);
            return this;
        }
        
        private boolean intersects(KillerCage cage) {
            return cages.stream().anyMatch(c -> c.intersects(cage));
        }
        
        public KillerCages build() {
            return new KillerCages(cages);
        }
    }
}
