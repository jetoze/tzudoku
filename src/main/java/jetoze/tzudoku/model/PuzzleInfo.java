package jetoze.tzudoku.model;

import static java.util.Objects.*;
import static tzeth.preconds.MorePreconditions.checkNotBlank;

public class PuzzleInfo {
    private final String name;
    private final PuzzleState state;
    
    public PuzzleInfo(String name, PuzzleState state) {
        this.name = checkNotBlank(name);
        this.state = requireNonNull(state);
    }

    public String getName() {
        return name;
    }

    public PuzzleState getState() {
        return state;
    }
    
    public String toString() {
        return String.format("%s [%s]", name, state);
    }
}
