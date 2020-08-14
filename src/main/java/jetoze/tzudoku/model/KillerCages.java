package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class KillerCages {

    public static final KillerCages EMPTY = new KillerCages();
    
    private final ImmutableMap<ImmutableSet<Position>, KillerCage> cages;
    
    private KillerCages() {
        this.cages = ImmutableMap.of();
    }

    public KillerCages(Collection<KillerCage> cages) {
        this.cages = cages.stream().collect(toImmutableMap(KillerCage::getPositions, c -> c));
    }

    public KillerCages(Map<ImmutableSet<Position>, KillerCage> cages) {
        this.cages = ImmutableMap.copyOf(cages);
    }
    
    public boolean isEmpty() {
        return cages.isEmpty();
    }
    
    public boolean containsCage(ImmutableSet<Position> positions) {
        requireNonNull(positions);
        return cages.containsKey(positions);
    }
    
    public Optional<KillerCage> getCage(ImmutableSet<Position> positions) {
        return Optional.ofNullable(cages.get(requireNonNull(positions)));
    }
    
    public ImmutableCollection<KillerCage> getCages() {
        return cages.values();
    }
    
    public KillerCages add(KillerCage cage) {
        return builder().addAll(getCages()).add(cage).build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    
    public static class Builder {
        private final List<KillerCage> cages = new ArrayList<KillerCage>();
        
        public Builder add(KillerCage cage) {
            requireNonNull(cage);
            this.cages.add(cage);
            return this;
        }
        
        public Builder addAll(Collection<KillerCage> cages) {
            checkArgument(cages.stream().allMatch(Objects::nonNull));
            this.cages.addAll(cages);
            return this;
        }
        
        public KillerCages build() {
            return new KillerCages(cages);
        }
    }
}
