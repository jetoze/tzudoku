package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;
import static tzeth.preconds.MorePreconditions.checkNotBlank;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;

public class PuzzleInfo {
    private final String name;
    private final PuzzleState state;
    @Nullable
    private final Date lastUpdated;
    
    public PuzzleInfo(String name, PuzzleState state, @Nullable Date lastUpdated) {
        this.name = checkNotBlank(name);
        this.state = requireNonNull(state);
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public PuzzleState getState() {
        return state;
    }
    
    public Optional<Date> lastUpdated() {
        return Optional.ofNullable(lastUpdated);
    }
    
    public String toString() {
        return String.format("%s [%s]", name, state);
    }
}
